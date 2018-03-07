/*
 * Copyright 2015 Fizzed, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.fizzed.blaze.ssh.impl;

import com.fizzed.blaze.Context;
import com.fizzed.blaze.Contexts;
import com.fizzed.blaze.core.BlazeException;
import com.fizzed.blaze.util.MutableUri;
import com.fizzed.blaze.ssh.SshException;
import com.jcraft.jsch.ConfigRepository;
import com.jcraft.jsch.ConfigRepository.Config;
import com.jcraft.jsch.HostKey;
import com.jcraft.jsch.HostKeyRepository;
import com.jcraft.jsch.Identity;
import com.jcraft.jsch.IdentityRepository;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.UIKeyboardInteractive;
import com.jcraft.jsch.UserInfo;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.fizzed.blaze.ssh.SshConnect;
import com.fizzed.blaze.ssh.SshSession;
import com.fizzed.blaze.ssh.util.ProxyCommand;
import com.fizzed.blaze.ssh.util.SshCommand;
import com.fizzed.blaze.util.ObjectHelper;
import com.jcraft.jsch.Proxy;
import java.nio.file.attribute.PosixFileAttributeView;

public class JschConnect extends SshConnect {
    static private final Logger log = LoggerFactory.getLogger(JschConnect.class);

    private final MutableUri uri;
    private long connectTimeout;
    private long keepAliveInterval;
    // ~/.ssh/config, ~/.ssh/known_hosts, ~/.ssh/id_rsa
    private Path configFile;
    private Path knownHostsFile;
    private List<Path> identityFiles;
    private boolean hostChecking;
    private Proxy proxy;
    
    public JschConnect(Context context) {
        this(context, new MutableUri("ssh:/"));
    }
    
    public JschConnect(Context context, MutableUri uri) {
        super(context);
        this.uri = uri;
        this.connectTimeout = 20000L;
        this.keepAliveInterval = 10000L;
        this.configFile = context.withUserDir(".ssh/config");
        this.knownHostsFile = context.withUserDir(".ssh/known_hosts");
        this.identityFiles = new ArrayList<>();
        this.identityFiles.add(context.withUserDir(".ssh/id_rsa"));
        this.identityFiles.add(context.withUserDir(".ssh/id_dsa"));
        this.hostChecking = true;
    }
    
    // for setting up proxies with the exact same settings by default
    @Override
    public SshConnect newConnect(MutableUri uri) {
        JschConnect connect = new JschConnect(this.context, uri);
        connect.connectTimeout = this.connectTimeout;
        connect.keepAliveInterval = this.keepAliveInterval;
        connect.configFile = this.configFile;
        connect.knownHostsFile = this.knownHostsFile;
        connect.identityFiles = new ArrayList<>(this.identityFiles);
        connect.hostChecking = this.hostChecking;
        return connect;
    }

    @Override
    public SshConnect disableHostChecking() {
        return hostChecking(false);
    }
    
    @Override
    public SshConnect hostChecking(boolean hostChecking) {
        this.hostChecking = hostChecking;
        return this;
    }

    @Override
    public JschConnect keepAliveInterval(long keepAliveInterval) {
        this.keepAliveInterval = keepAliveInterval;
        return this;
    }
    
    @Override
    public JschConnect configFile(Path configFile) {
        if (!Files.exists(configFile)) {
            throw new com.fizzed.blaze.core.FileNotFoundException("SSH config file " + configFile + " does not exist."
                + " Did you know we'll try to load ~/.ssh/config by default?");
        }
        this.configFile = configFile;
        return this;
    }
    
    @Override
    public JschConnect knownHostsFile(Path knownHostsFile) {
        if (!Files.exists(knownHostsFile)) {
            throw new com.fizzed.blaze.core.FileNotFoundException("SSH known_hosts file " + knownHostsFile + " does not exist."
                + " Did you know we'll try to load ~/.ssh/known_hosts by default?");
        }
        this.knownHostsFile = knownHostsFile;
        return this;
    }
    
    @Override
    public JschConnect identityFile(Path identityFile) {
        if (!Files.exists(identityFile)) {
            throw new com.fizzed.blaze.core.FileNotFoundException("SSH identity file " + identityFile + " does not exist."
                + " Did you know we'll try to load ~/.ssh/id_rsa by default?");
        }
        // insert onto front (since that is likely what we want searched first)
        this.identityFiles.add(0, identityFile);
        return this;
    }
    
    @Override
    public MutableUri getUri() {
        return this.uri;
    }

    @Override
    public SshConnect proxy(SshSession session, boolean autoclose) {
        // NOTE: this defaults to using "nc %h %p"
        this.proxy = JschExecProxy.of(session, autoclose);
        return this;
    }
    
    @Override
    protected Result doRun() throws BlazeException {
        ObjectHelper.requireNonNull(uri, "uri cannot be null");
        ObjectHelper.requireNonNull(uri.getScheme(), "uri scheme is required for ssh (e.g. ssh://user@host)");
        ObjectHelper.requireNonNull(uri.getHost(), "uri host is required for ssh");
        
        /*
        if (!uri.getScheme().equals("ssh")) {
            throw new IllegalArgumentException("Uri scheme invalid (e.g. ssh://user@host) (actual = " + uri.getScheme() + ")");
        }
        */
        
        Integer port = (uri.getPort() != null ? uri.getPort() : 22);
        String username = (uri.getUsername() != null ? uri.getUsername() : System.getProperty("user.name"));
        
        JSch.setLogger(new BlazeJschLogger());
        
        Session jschSession = null;
        JSch jsch = new JSch();
        try {
            //
            // mimic openssh ~.ssh/config
            //
            try {
                log.debug("Using ssh config {}", configFile);
                
                ConfigRepository configRepository =
                    com.jcraft.jsch.OpenSSHConfig.parseFile(configFile.toAbsolutePath().toString());
            
                // is there a config for this host?
                System.out.println("Checking for config for given host "+uri.getHost());
                Config config = configRepository.getConfig(uri.getHost());
                
                jsch.setConfigRepository(configRepository);
            
                if (config != null) {
                    System.out.println("Config exists");
                    // has proxy command?
                    String proxyCommand = config.getValue("ProxyCommand");
                    if (proxyCommand != null) {
                        if (this.proxy != null) {
                            log.debug("Session proxy set but host has ProxyCommand");
                        } else {
                            // "ssh jump.example.com nc %h %p" -> a structured command
                            SshCommand command = ProxyCommand.parse(proxyCommand).getSshCommand();
                            SshConnect connect = this.newConnect(command.toUri());
                            try {
                                // connect and then use that session as our proxy
                                this.proxy = JschExecProxy.of(
                                    connect.run(), true, command.toCommand());
                            } catch (Exception e) {
                                throw e;
                            }
                        }
                    }
                    
                    // were we provided with an actual forced username?
                    if (uri.getUsername() != null) {
                        // TODO: seems like we are not able to override the username in the config repo
                        jschSession = jsch.getSession(uri.getUsername(), uri.getHost());
                    } else if (config.getUser() == null) {
                        jschSession = jsch.getSession(username, uri.getHost());
                    } else {
                        jschSession = jsch.getSession(uri.getHost());
                    }

                    if(config.getValue("IdentityFile") != null) {
                        System.out.println("IdentityFile found "+config.getValue("IdentityFile"));
                        identityFiles.add(Paths.get(config.getValue("IdentityFile")));
                    }

                }
            } catch (java.io.FileNotFoundException e) {
                // OpenSSH would fallback if it didn't exist so we will too
                log.debug("{} does not exist, will fallback to password auth", configFile);
            }
            
            if (jschSession == null) {
                jschSession = jsch.getSession(username, uri.getHost(), port);
            }
            
            // override port?
            if (uri.getPort() != null) {
                jschSession.setPort(uri.getPort());
            }
            
            jschSession.setDaemonThread(true);
            jschSession.setUserInfo(new BlazeJschUserInfo(jschSession));
            
            // configure way more ciphers by default????
            //jschSession.setConfig("cipher.s2c", "aes128-cbc,3des-cbc,blowfish-cbc");
            //jschSession.setConfig("cipher.c2s", "aes128-cbc,3des-cbc,blowfish-cbc");
            //jschSession.setConfig("CheckCiphers", "aes128-cbc"); 
            
            
            //
            // mimic openssh ~.ssh/known_hosts
            //
            
            if (!this.hostChecking) {
                // Setting this means the user wont' be prompted
                jschSession.setConfig("StrictHostKeyChecking", "no");
            } else {
                // create one if it doesn't yet exist?
                if (!Files.exists(knownHostsFile)) {
                    // does the parent directory exist?
                    Path parentDir = knownHostsFile.getParent();

                    if (!Files.exists(parentDir)) {
                        Files.createDirectory(parentDir);
                        
                        // try to set permissions (won't work on windows)
                        PosixFileAttributeView view = Files.getFileAttributeView(parentDir, PosixFileAttributeView.class);
                        if (view != null) {
                            view.setPermissions(PosixFilePermissions.fromString("rwx------"));
                        }
                    }
                    
                    Files.createFile(knownHostsFile);
                    
                    // try to set permissions (won't work on windows)
                    PosixFileAttributeView view = Files.getFileAttributeView(knownHostsFile, PosixFileAttributeView.class);
                    if (view != null) {
                        view.setPermissions(PosixFilePermissions.fromString("rw-------"));
                    }
                }

                if (true) {
                    String f = knownHostsFile.toAbsolutePath().toString();
                    log.debug("Setting ssh known_hosts to {}", f);
                    jsch.setKnownHosts(f);
                    // in addition to storing known hosts, hash them as well
                    JSch.setConfig("HashKnownHosts",  "yes");
                }
            }
            
            if (log.isDebugEnabled()) {
                HostKeyRepository hkr = jsch.getHostKeyRepository();
                HostKey[] hks = hkr.getHostKey();
                if (hks != null) {
                    log.debug("Host keys in {}", hkr.getKnownHostsRepositoryID());
                    for (HostKey hk : hks) {
                        log.trace("Loaded host key {} {} {}", hk.getHost(), hk.getType(), hk.getFingerPrint(jsch));
                    }
                }
            }
            

            jschSession.setServerAliveInterval((int)this.keepAliveInterval);
            
            
            //
            // mimic openssh ~/.ssh/id_dsa
            //
            
            // load identities
            if (this.identityFiles != null) {
                this.identityFiles.forEach((identityFile) -> {
                    if (Files.exists(identityFile)) {
                        String f = identityFile.toAbsolutePath().toString();
                        log.debug("Adding ssh identity to {}", f);
                        System.out.println("Adding ssh identity to "+f);
                        try {
                            jsch.addIdentity(f);
                        } catch (JSchException e) {
                            throw new BlazeException("Unable to add ssh identity", e);
                        }
                    }
                });
            }
            
            if (log.isDebugEnabled()) {
                IdentityRepository ir = jsch.getIdentityRepository();
                @SuppressWarnings("UseOfObsoleteCollectionType")
                java.util.Vector<Object> identities = ir.getIdentities();
                identities.stream()
                    .filter((identity) -> (identity instanceof Identity))
                    .map((identity) -> (Identity)identity)
                    .forEach((i) -> {
                        log.debug("Identity {} {}", i.getName(), i.getAlgName());
                    });
            }
            
            //jschSession.setConfig("PreferredAuthentications", "publickey,password");
            
            String proxyInfo = "";
            if (this.proxy != null) {
                jschSession.setProxy(this.proxy);
                // via hostname
                proxyInfo = " via " + this.proxy;
            }
            
            log.info("Open ssh://{}@{}:{}{}...",
                jschSession.getUserName(), jschSession.getHost(), jschSession.getPort(), proxyInfo);
            
            long start = System.currentTimeMillis();
            
            jschSession.connect((int)this.connectTimeout);
            
            // update uri with the connection info
            uri.username(jschSession.getUserName());
            uri.host(jschSession.getHost());
            uri.port(jschSession.getPort());
            
            long stop = System.currentTimeMillis();
            
            log.info("Connected ssh://{}@{}:{}{} in {} ms",
                jschSession.getUserName(), jschSession.getHost(), jschSession.getPort(), proxyInfo, (stop-start));
            
            return createResult(new JschSession(this.context, this.uri.toImmutableUri(), jsch, jschSession));
        } catch (JSchException e) {
            throw tryToUnwrap(e);
        } catch (IOException e) {
            throw new SshException(e.getMessage(), e);
        }
    }
    
    static public SshException tryToUnwrap(JSchException e) {
        Throwable cause = e.getCause();
        
        if (cause != null) {
            return new SshException(cause.getMessage(), cause);
        }
        
        return new SshException(e.getMessage(), e);
    }

    public class BlazeJschUserInfo implements UserInfo, UIKeyboardInteractive {

        final private Session jschSession;
        private boolean returnedPassword;

        public BlazeJschUserInfo(Session jschSession) {
            this.jschSession = jschSession;
            this.returnedPassword = false;
        }
        
        @Override
        public String getPassphrase() {
            String prompt = String.format("identity passphrase: ");
            char[] password = Contexts.passwordPrompt(prompt);
            // THIS IS UNFORTUNATE SINCE THIS STRING IS INTERNED...
            return new String(password);
        }
        
        @Override
        public boolean promptPassphrase(String string) {
            // create our own custom prompt in getPassphrase()
            return true;
        }

        @Override
        public String getPassword() {
            // pass along password if provided (but only one time!)
            if (JschConnect.this.uri.getPassword() != null) {
                if (this.returnedPassword) {
                    // prevent possible infinite loop!
                    throw new IllegalStateException("Password prompted too many times");
                }
                this.returnedPassword = true;
                return JschConnect.this.uri.getPassword();
            }
            
            // joelauer@hosts's password:
            String prompt = String.format("%1s@%2s's password: ", jschSession.getUserName(), jschSession.getHost());
            char[] password = Contexts.passwordPrompt(prompt);
            // THIS IS UNFORTUNATE SINCE THIS STRING IS INTERNED...
            return new String(password);
        }

        @Override
        public boolean promptPassword(String string) {
            // create our own custom prompt in getPassword()
            return true;
        }

        @Override
        public boolean promptYesNo(String prompt) {
            // fix prompt for host key
            if (prompt.contains("authenticity of host") && prompt.endsWith("connecting?")) {
                prompt = prompt.substring(0, prompt.length() - 1);
                prompt += " (yes/no)?";
            }
            
            String answer = Contexts.prompt(prompt + " ");
            return answer.equalsIgnoreCase("yes");
        }

        @Override
        public void showMessage(String string) {
            log.info(string);
        }

        @Override
        public String[] promptKeyboardInteractive(String destination, String name, String instruction, String[] prompt, boolean[] echo) {
            log.debug("promptKeyboardInteractive: {}, {}, {}, {}", destination, name, instruction, prompt);
            
            // some systems prompt for password via this method
            if (prompt != null && prompt.length == 1 && prompt[0].toLowerCase().contains("password for")) {
                String pw = getPassword();
                return new String[] { pw };
            }
            
            log.error("We do not support promptKeyboardInteractive in Blaze quite yet!");
            return null;
        }
        
    }
    
    public static class BlazeJschLogger implements com.jcraft.jsch.Logger {

        @Override
        public boolean isEnabled(int level) {
            switch (level) {
                case com.jcraft.jsch.Logger.INFO:
                case com.jcraft.jsch.Logger.DEBUG:
                    return log.isTraceEnabled();
                case com.jcraft.jsch.Logger.ERROR:
                    return log.isErrorEnabled();
                case com.jcraft.jsch.Logger.FATAL:
                    return log.isErrorEnabled();
                case com.jcraft.jsch.Logger.WARN:
                    return log.isWarnEnabled();
            }
            return false;
        }

        @Override
        public void log(int level, String message) {
            switch (level) {
                case com.jcraft.jsch.Logger.INFO:
                case com.jcraft.jsch.Logger.DEBUG:
                    log.trace(message);
                    break;
                case com.jcraft.jsch.Logger.ERROR:
                    log.error(message);
                    break;
                case com.jcraft.jsch.Logger.FATAL:
                    log.error(message);
                    break;
                case com.jcraft.jsch.Logger.WARN:
                    log.warn(message);
                    break;
            }
        }
        
    }
}

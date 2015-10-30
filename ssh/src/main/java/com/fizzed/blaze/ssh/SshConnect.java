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
package com.fizzed.blaze.ssh;

import com.fizzed.blaze.Context;
import com.fizzed.blaze.Contexts;
import com.fizzed.blaze.core.Action;
import com.fizzed.blaze.core.BlazeException;
import com.fizzed.blaze.util.MutableUri;
import com.fizzed.blaze.core.MutableUriSupport;
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
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Vector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author joelauer
 */
public class SshConnect extends Action<SshSession> implements MutableUriSupport<SshConnect> {
    static private final Logger log = LoggerFactory.getLogger(SshConnect.class);

    private final MutableUri uri;
    private long connectTimeout;
    private long keepAliveInterval;
    // ~/.ssh/config, ~/.ssh/known_hosts, ~/.ssh/id_rsa
    private Path configFile;
    private Path knownHostsFile;
    private List<Path> identityFiles;
    
    public SshConnect(Context context) {
        this(context, new MutableUri("ssh://"));
    }
    
    public SshConnect(Context context, MutableUri uri) {
        super(context);
        this.uri = uri;
        this.connectTimeout = 20000L;
        this.keepAliveInterval = 10000L;
        this.configFile = context.withUserDir(".ssh/config");
        this.knownHostsFile = context.withUserDir(".ssh/known_hosts");
        this.identityFiles = new ArrayList<>();
        this.identityFiles.add(context.withUserDir(".ssh/id_rsa"));
        this.identityFiles.add(context.withUserDir(".ssh/id_dsa"));
    }

    public SshConnect keepAliveInterval(long keepAliveInterval) {
        this.keepAliveInterval = keepAliveInterval;
        return this;
    }
    
    public SshConnect configFile(Path configFile) {
        if (!Files.exists(configFile)) {
            throw new com.fizzed.blaze.internal.FileNotFoundException("SSH config file " + configFile + " does not exist."
                + " Did you know we'll try to load ~/.ssh/config by default?");
        }
        this.configFile = configFile;
        return this;
    }
    
    public SshConnect knownHostsFile(Path knownHostsFile) {
        if (!Files.exists(knownHostsFile)) {
            throw new com.fizzed.blaze.internal.FileNotFoundException("SSH known_hosts file " + knownHostsFile + " does not exist."
                + " Did you know we'll try to load ~/.ssh/known_hosts by default?");
        }
        this.knownHostsFile = knownHostsFile;
        return this;
    }
    
    public SshConnect identityFile(Path identityFile) {
        if (!Files.exists(identityFile)) {
            throw new com.fizzed.blaze.internal.FileNotFoundException("SSH identity file " + identityFile + " does not exist."
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
    public SshSession doRun() throws BlazeException {
        Objects.requireNonNull(uri.getScheme(), "uri scheme is required for ssh (e.g. ssh://user@host)");
        Objects.requireNonNull(uri.getHost(), "uri host is required for ssh");
        
        if (!uri.getScheme().equals("ssh")) {
            throw new IllegalArgumentException("Only a uri with a schem of ssh is support (e.g. ssh://user@host)");
        }
        
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
                ConfigRepository configRepository =
                    com.jcraft.jsch.OpenSSHConfig.parseFile(configFile.toAbsolutePath().toString());

                jsch.setConfigRepository(configRepository);
            
                // is there a config for this host?
                Config config = configRepository.getConfig(uri.getHost());
                
                if (config != null) {
                    // were we provided with an actual forced username?
                    if (uri.getUsername() != null) {
                        // TODO: seems like we are not able to override the username in the config repo
                        jschSession = jsch.getSession(uri.getUsername(), uri.getHost());
                    } else if (config.getUser() == null) {
                        jschSession = jsch.getSession(username, uri.getHost());
                    } else {
                        jschSession = jsch.getSession(uri.getHost());
                    }
                }
            } catch (java.io.FileNotFoundException e) {
                // OpenSSH would fallback if it didn't exist so we will too
                log.debug("{} does not exist, will fallback to password auth", configFile);
            }
            
            if (jschSession == null) {
                jschSession = jsch.getSession(username, uri.getHost(), port);
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
            
            // create one if it doesn't yet exist
            if (!Files.exists(knownHostsFile)) {
                // match what OpenSSH does (it would create this file on-demand)
                Files.createFile(knownHostsFile, PosixFilePermissions.asFileAttribute(
                    PosixFilePermissions.fromString("rw-r--r--")));
            }
            
            if (true) {
                String f = knownHostsFile.toAbsolutePath().toString();
                log.debug("Setting ssh known_hosts to {}", f);
                jsch.setKnownHosts(f);
                // in addition to storing known hosts, hash them as well
                JSch.setConfig("HashKnownHosts",  "yes");
            }

            if (log.isDebugEnabled()) {
                HostKeyRepository hkr = jsch.getHostKeyRepository();
                HostKey[] hks = hkr.getHostKey();
                if (hks != null) {
                    log.debug("Host keys in {}", hkr.getKnownHostsRepositoryID());
                    for (HostKey hk : hks) {
                        log.debug("Loaded host key {} {} {}", hk.getHost(), hk.getType(), hk.getFingerPrint(jsch));
                    }
                }
            }
            
            // Setting this means the user wont' be prompted
            //jschSession.setConfig("StrictHostKeyChecking", "no");
            
            jschSession.setServerAliveInterval((int)this.keepAliveInterval);
            
            // pass along password if provided
            if (this.uri.getPassword() != null) {
                // use bytes, not strings
                jschSession.setPassword(this.uri.getPassword().getBytes(StandardCharsets.UTF_8));
            }
            
            //
            // mimic openssh ~/.ssh/id_dsa
            //
            
            // load identities
            if (this.identityFiles != null) {
                this.identityFiles.forEach((identityFile) -> {
                    if (Files.exists(identityFile)) {
                        String f = identityFile.toAbsolutePath().toString();
                        log.debug("Adding ssh identity to {}", f);
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
                Vector identities = ir.getIdentities();
                for (Object identity : identities) {
                    if (identity instanceof Identity) {
                        Identity i = (Identity)identity;
                        log.debug("Identity {} {}", i.getName(), i.getAlgName());
                    }
                }
            }
            
            //jschSession.setConfig("PreferredAuthentications", "publickey,password");
            
            log.info("Trying to open ssh session to {}@{}:{}...",
                    jschSession.getUserName(), jschSession.getHost(), jschSession.getPort());
            
            jschSession.connect((int)this.connectTimeout);
            
            HostKey hk = jschSession.getHostKey();
            
            // update uri with the connection info
            uri.username(jschSession.getUserName());
            uri.host(jschSession.getHost());
            uri.port(jschSession.getPort());
            
            log.info("Connected ssh session to {}@{}:{}!",
                    jschSession.getUserName(), jschSession.getHost(), jschSession.getPort());
            
            return new SshSessionImpl(this.uri.toImmutableUri(), jsch, jschSession);
        } catch (JSchException | IOException e) {
            // TODO: any specific exceptions worth doing something with?
            // JSchException: timeout in wating for rekeying process this happens when we need to accept the host key...
            throw new BlazeException(e.getMessage(), e);
        }
    }

    public class BlazeJschUserInfo implements UserInfo, UIKeyboardInteractive {

        final private Session jschSession;

        public BlazeJschUserInfo(Session jschSession) {
            this.jschSession = jschSession;
        }
        
        @Override
        public String getPassphrase() {
            String prompt = String.format("identity passphrase: ");
            char[] password = Contexts.consolePasswordPrompt(prompt);
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
            // joelauer@hosts's password:
            String prompt = String.format("%1s@%2s's password: ", jschSession.getUserName(), jschSession.getHost());
            char[] password = Contexts.consolePasswordPrompt(prompt);
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
            
            String answer = Contexts.consolePrompt(prompt + " ");
            return answer.equalsIgnoreCase("yes");
        }

        @Override
        public void showMessage(String string) {
            log.info(string);
        }

        @Override
        public String[] promptKeyboardInteractive(String destination, String name, String instruction, String[] prompt, boolean[] echo) {
            log.info("promptKeyboardInteractive: {}, {}, {}, {}", destination, name, instruction, prompt);
            log.error("We do not support this yet in Blaze!");
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

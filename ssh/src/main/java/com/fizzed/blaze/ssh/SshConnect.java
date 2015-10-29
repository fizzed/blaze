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
import com.fizzed.blaze.core.MutableUri;
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
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileAttribute;
import java.nio.file.attribute.PosixFilePermissions;
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
    
    public SshConnect(Context context) {
        this(context, new MutableUri("ssh://"));
    }
    
    public SshConnect(Context context, MutableUri uri) {
        super(context);
        this.uri = uri;
        this.connectTimeout = 20000L;
        this.keepAliveInterval = 10000L;
    }

    public SshConnect keepAliveInterval(long keepAliveInterval) {
        this.keepAliveInterval = keepAliveInterval;
        return this;
    }
    
    
    
    @Override
    public MutableUri getUri() {
        return this.uri;
    }
    
    @Override
    public SshSession doRun() throws BlazeException {
        //Objects.requireNonNull(uri.getUsername(), "username is required for ssh");
        Objects.requireNonNull(uri.getHost(), "host is required for ssh");
        
        Integer port = (uri.getPort() != null ? uri.getPort() : 22);
        String username = (uri.getUsername() != null ? uri.getUsername() : System.getProperty("user.name"));
        
        Session jschSession = null;
        JSch jsch = new JSch();
        try {
            
            //
            // mimic openssh ~.ssh/config
            //
            try {
                ConfigRepository configRepository =
                    com.jcraft.jsch.OpenSSHConfig.parseFile("~/.ssh/config");

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
            } catch (FileNotFoundException e) {
                // OpenSSH would fallback if it didn't exist so we will too
                log.debug("~/.ssh/config does not exist, will fallback to password auth");
            }
            
            if (jschSession == null) {
                jschSession = jsch.getSession(username, uri.getHost(), port);
            }
            
            
            jschSession.setDaemonThread(true);
            jschSession.setUserInfo(new BlazeUserInfo(jschSession));
            
            
            
            // configure way more ciphers by default????
            //jschSession.setConfig("cipher.s2c", "aes128-cbc,3des-cbc,blowfish-cbc");
            //jschSession.setConfig("cipher.c2s", "aes128-cbc,3des-cbc,blowfish-cbc");
            //jschSession.setConfig("CheckCiphers", "aes128-cbc");
            
            //jschSession.setConfig("PreferredAuthentications", "userauth.publickey");
            
            
            //
            // mimic openssh ~.ssh/known_hosts
            //
            // try to use user's known_hosts file
            Path knownHostsFile = context.withUserDir(".ssh/known_hosts");
            
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

            HostKeyRepository hkr = jsch.getHostKeyRepository();
            HostKey[] hks = hkr.getHostKey();
            if (hks != null) {
                log.debug("Host keys in {}", hkr.getKnownHostsRepositoryID());
                for (HostKey hk : hks) {
                    log.debug("Loaded host key {} {} {}", hk.getHost(), hk.getType(), hk.getFingerPrint(jsch));
                }
            }
            
            // Setting this means the user wont' be prompted
            //jschSession.setConfig("StrictHostKeyChecking", "yes");
            
            jschSession.setServerAliveInterval((int)this.keepAliveInterval);
            
            // pass along password if provided
            if (this.uri.getPassword() != null) {
                jschSession.setPassword(this.uri.getPassword());
            }
            
            
            //
            // mimic openssh ~/.ssh/id_dsa
            //
            // try to use user's known_hosts file
            Path identityPrivateFile = context.withUserDir(".ssh/id_rsa");
            //Path identityPublicFile = context.withUserDir(".ssh/id_rsa.pub");
            
            if (Files.exists(identityPrivateFile)) {
                String f = identityPrivateFile.toAbsolutePath().toString();
                log.debug("Setting ssh identity to {}", f);
                jsch.addIdentity(f);
            }
            
            IdentityRepository ir = jsch.getIdentityRepository();
            @SuppressWarnings("UseOfObsoleteCollectionType")
            Vector identities = ir.getIdentities();
            for (Object identity : identities) {
                if (identity instanceof Identity) {
                    Identity i = (Identity)identity;
                    log.info("Identity {} {}", i.getName(), i.getAlgName());
                }
            }
            
            
            
            log.info("Opening SSH session to {}@{}:{}...", jschSession.getUserName(), jschSession.getHost(), jschSession.getPort());
            
            jschSession.connect((int)this.connectTimeout);
            
            HostKey hk = jschSession.getHostKey();
            
            log.debug("SSH host key: {} {} {}", hk.getHost(), hk.getType(), hk.getFingerPrint(jsch));
            
            return new SshSession(this.uri, jsch, jschSession);
        } catch (JSchException | IOException e) {
            // JSchException: timeout in wating for rekeying process
            // this happens when we need to accept the host key...
            
            throw new BlazeException(e.getMessage(), e);
        }
    }

    public class BlazeUserInfo implements UserInfo, UIKeyboardInteractive {

        final private Session jschSession;

        public BlazeUserInfo(Session jschSession) {
            this.jschSession = jschSession;
        }
        
        @Override
        public String getPassphrase() {
            log.info("getPassphrase");
            return SshConnect.this.uri.getPassword();
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
            //log.info("Password prompt: " + string);
            // create our own custom prompt in getPassword()
            return true;
        }

        @Override
        public boolean promptPassphrase(String string) {
            log.info("Phassphrase prompt: " + string);
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
            log.info("showMessage: " + string);
        }

        @Override
        public String[] promptKeyboardInteractive(String destination, String name, String instruction, String[] prompt, boolean[] echo) {
            log.info("promptKeyboardInteractive: {}, {}, {}, {}", destination, name, instruction, prompt);
            return null;
        }
        
    }
}

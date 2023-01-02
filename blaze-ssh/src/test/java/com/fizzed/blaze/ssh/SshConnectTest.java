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

import com.fizzed.blaze.ssh.impl.JschConnect;
import java.net.ConnectException;
import java.net.UnknownHostException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.PublicKey;
import org.apache.sshd.common.config.keys.KeyUtils;
import org.apache.sshd.server.session.ServerSession;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertThat;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author joelauer
 */
public class SshConnectTest extends SshBaseTest {
    static private final Logger log = LoggerFactory.getLogger(SshConnectTest.class);
    
    @Test
    public void badHost() throws Exception {
        try {
            new JschConnect(context)
                .host("thishostdoesnotexist")
                .run();
        } catch (SshException e) {
            assertThat(e.getMessage(), containsString("thishostdoesnotexist"));
            assertThat(e.getCause(), instanceOf(UnknownHostException.class));
        }
    }
    
    @Test
    public void badPort() throws Exception {
        try {
            new JschConnect(context)
                .host("localhost")
                .port(1)
                .run();
        } catch (SshException e) {
            assertThat(e.getMessage(), containsString("Connection refused"));
            assertThat(e.getCause(), instanceOf(ConnectException.class));
        }
    }
    
    @Test
    public void unknownHostPrompted() throws Exception {
        contextWithEmptyUserDir();
        
        sshd.start();
        
        // schedule a "no" to host key prompt
        prompter.add("no");
        
        try {
            SshSession session
                = new JschConnect(context)
                    .host(sshd.getHost())
                    .port(sshd.getPort())
                    .username("doesnotexist")
                    .run();
        } catch (Exception e) {
            log.error("", e);
            assertThat(e.getMessage(), containsString("reject HostKey"));
        }
    }
    
    @Test
    public void disableHostChecking() throws Exception {
        contextWithEmptyUserDir();
        
        sshd.start();
        
        try {
            SshSession session
                = new JschConnect(context)
                    .host(sshd.getHost())
                    .port(sshd.getPort())
                    .username("doesnotexist")
                    .disableHostChecking()
                    .run();
        } catch (IllegalStateException e) {
            // verify the prompt for the password occurred
            assertThat(e.getMessage(), containsString("password:"));
        }
    }
    
    @Test
    public void unknownHostSavedOnYes() throws Exception {
        contextWithEmptyUserDir();
        
        Path knownHostsFile = context.withUserDir(".ssh/known_hosts");

        sshd.start();
        
        // schedule a "yes" answer to unknown host prompt
        prompter.add("yes");
        
        assertThat(Files.exists(knownHostsFile), is(false));
        
        try {
            SshSession session
                = new JschConnect(context)
                    .host(sshd.getHost())
                    .port(sshd.getPort())
                    .username("doesnotexist")
                    .run();
        } catch (IllegalStateException e) {
            // verify the prompt for the password occurred
            assertThat(e.getMessage(), containsString("password:"));
        }
        
        assertThat(prompter.answers(), hasSize(0));
        
        // verify known_hosts was created
        assertThat(Files.exists(knownHostsFile), is(true));
        
        
        // try again, but don't schedule an answer to a prompt
        try {
            SshSession session
                = new JschConnect(context)
                    .host(sshd.getHost())
                    .port(sshd.getPort())
                    .username("doesnotexist")
                    .run();
        } catch (IllegalStateException e) {
            // verify the prompt for the password occurred
            assertThat(e.getMessage(), containsString("password:"));
        }
    }
    
    @Test
    public void passwordAuthViaPrompt() throws Exception {
        contextWithEmptyUserDir();

        sshd.start();
        
        // schedule a "test" password answer to second prompt
        prompter.add("test");
 
        SshSession session
            = new JschConnect(context)
                .host(sshd.getHost())
                .port(sshd.getPort())
                .username("blaze")
                .disableHostChecking()
                .run();
        
        assertThat(session, is(not(nullValue())));
        assertThat(session.uri().getHost(), is("localhost"));
        assertThat(session.uri().getUsername(), is("blaze"));
        assertThat(prompter.answers(), hasSize(0));
    }
    
    @Test
    public void passwordAuthViaProperty() throws Exception {
        contextWithEmptyUserDir();

        sshd.start();
 
        SshSession session
            = new JschConnect(context)
                .host(sshd.getHost())
                .port(sshd.getPort())
                .username("blaze")
                .password("test")
                .disableHostChecking()
                .run();
        
        assertThat(session, is(not(nullValue())));
        assertThat(session.uri().getHost(), is("localhost"));
        assertThat(session.uri().getUsername(), is("blaze"));
        assertThat(prompter.answers(), hasSize(0));
    }
    
    @Test @Ignore
    public void userPublicKeyAuth() throws Exception {
        Path publicKeyUserDir = context.userDir().resolve("public_key");
        context.userDir(publicKeyUserDir);

        final String fingerprint = SshUtils.fingerprint(publicKeyUserDir.resolve(".ssh/id_rsa"));
        log.info("Fingerprint: {}", fingerprint);
        
        sshd.setPasswordAuthenticator(null);
        sshd.setPublickeyAuthenticator((String username, PublicKey pk, ServerSession ss) -> {
            log.info("Auth with public key? {}", username);
            if (!"blaze".equals(username)) {
                return false;
            }
            String fp = KeyUtils.getFingerPrint(pk);
            log.info("Auth fingerprint: {}", fp);
            return fp.equals(fingerprint);
        });
        
        sshd.start();
        
        SshSession session
            = new JschConnect(context)
                .host(sshd.getHost())
                .port(sshd.getPort())
                .username("blaze")
                .disableHostChecking()
                .run();
        
        assertThat(session, is(not(nullValue())));
        assertThat(session.uri().getHost(), is("localhost"));
        assertThat(session.uri().getUsername(), is("blaze"));
        assertThat(prompter.answers(), hasSize(0));
    }
    
    @Test @Ignore
    public void suppliedPublicKeyAuth() throws Exception {
        contextWithEmptyUserDir();
        
        Path publicKeyFile = context.userDir().resolve("../public_key/.ssh/id_rsa");
        
        final String fingerprint = SshUtils.fingerprint(publicKeyFile);

        sshd.setPasswordAuthenticator(null);
        sshd.setPublickeyAuthenticator((String username, PublicKey pk, ServerSession ss) -> {
            if (!"blaze".equals(username)) {
                return false;
            }
            String fp = KeyUtils.getFingerPrint(pk);
            return fp.equals(fingerprint);
        });
        
        sshd.start();
        
        SshSession session
            = new JschConnect(context)
                .host(sshd.getHost())
                .port(sshd.getPort())
                .username("blaze")
                .identityFile(publicKeyFile)
                .disableHostChecking()
                .run();
        
        assertThat(session, is(not(nullValue())));
        assertThat(session.uri().getHost(), is("localhost"));
        assertThat(session.uri().getUsername(), is("blaze"));
        assertThat(prompter.answers(), hasSize(0));
    }
    
    @Test
    public void sshConfig() throws Exception {
        contextWithEmptyUserDir();
        
        Path configFile = context.userDir().resolve("../.ssh/config");
        
        sshd.start();
        
        SshSession session
            = new JschConnect(context)
                .host("thishostnowexists")
                .port(sshd.getPort())
                .disableHostChecking()
                .configFile(configFile)
                .password("test")
                .run();
        
        assertThat(session, is(not(nullValue())));
        assertThat(session.uri().getHost(), is("localhost"));
        assertThat(session.uri().getUsername(), is("blaze"));
        assertThat(prompter.answers(), hasSize(0));
    }
    
}

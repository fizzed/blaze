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

import com.fizzed.blaze.Config;
import com.fizzed.blaze.core.ContextHolder;
import com.fizzed.blaze.internal.ContextImpl;
import com.fizzed.blaze.internal.ConfigHelper;
import com.fizzed.blaze.internal.ExpectPrompter;
import com.fizzed.blaze.internal.FileHelper;
import com.fizzed.blaze.ssh.impl.JschConnect;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.apache.commons.io.FileUtils;
import org.apache.sshd.server.SshServer;
import org.apache.sshd.server.channel.ChannelSession;
import org.apache.sshd.server.keyprovider.SimpleGeneratorHostKeyProvider;
import org.apache.sshd.server.session.ServerSession;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.nullValue;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.spy;

/**
 *
 * @author joelauer
 */
public class SshBaseTest {
    static private final Logger log = LoggerFactory.getLogger(SshBaseTest.class);
    
    Config config;
    ContextImpl context;
    ExpectPrompter prompter;
    SshServer sshd;
    SshCommandHandler commandHandler;
    
    @BeforeEach
    public void setup() throws Exception {
        File sshdTestTxtFile = FileHelper.resourceAsFile("/sshd/ssh-test.txt");
        File sshdTestDir = sshdTestTxtFile.getParentFile();
        
        config = ConfigHelper.createEmpty();
        context = spy(new ContextImpl(null, sshdTestDir.toPath(), null, config));
        prompter = new ExpectPrompter();
        context.prompter(prompter);
        
        ContextHolder.set(context);
        
        sshd = SshServer.setUpDefaultServer();
        sshd.setHost("localhost");
        sshd.setPort(0);        // auto
        sshd.setKeyPairProvider(new SimpleGeneratorHostKeyProvider());
        sshd.setPasswordAuthenticator((String username, String password, ServerSession ss) -> {
            log.info("Authenticate {} with {}", username, password);
            return "blaze".equals(username) && "test".equals(password);
        });
//        sshd.setPublickeyAuthenticator((String string, PublicKey pk, ServerSession ss) -> {
//            log.info("Auth with public key? {}", string);
//            return true;
//        });
//        sshd.setCommandFactory((String line) -> {
//            return new SshCommand(commandHandler, line);
//        });
        sshd.setCommandFactory((ChannelSession cs, String line) -> {
            return new SshCommand(commandHandler, line);
        });
    }
    
    @AfterEach
    public void teardown() throws Exception {
        try {
            sshd.stop(true);
            sshd = null;
        } catch (Exception e) {
            // do nothing
        }
    }
    
    public void contextWithEmptyUserDir() throws IOException {
        Path emptyUserDir = context.userDir().resolve("empty");
        FileUtils.deleteQuietly(emptyUserDir.toFile());
        Files.createDirectories(emptyUserDir);
        // build empty config file
        Path sshDir = emptyUserDir.resolve(".ssh");
        Files.createDirectories(sshDir);
        Files.createFile(sshDir.resolve("config"));
        context.userDir(emptyUserDir);
    }
    
    public SshSession startAndConnect() throws Exception  {
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
        
        return session;
    }
    
}

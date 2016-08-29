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
import com.fizzed.blaze.Context;
import com.fizzed.blaze.SecureShells;
import com.fizzed.blaze.core.ContextHolder;
import com.fizzed.blaze.internal.ConfigHelper;
import com.fizzed.blaze.internal.ContextImpl;
import com.fizzed.blaze.util.MutableUri;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collection;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import static com.fizzed.blaze.SecureShells.sshConnect;
import static com.fizzed.blaze.SecureShells.sshExec;
import com.fizzed.blaze.internal.FileHelper;
import com.fizzed.blaze.ssh.impl.JschExec;
import com.fizzed.blaze.util.CaptureOutput;
import com.fizzed.blaze.util.Streamables;
import java.io.BufferedWriter;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import org.apache.commons.io.FileUtils;
import static org.hamcrest.CoreMatchers.anyOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.CoreMatchers.startsWith;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.junit.Assume.assumeTrue;
import org.junit.Ignore;

/**
 * Real tests against actual hosts via ssh.
 * 
 * @author joelauer
 */
@RunWith(Parameterized.class)
public class SshIntegrationTest {
    static private final Logger log = LoggerFactory.getLogger(SshIntegrationTest.class);

    private final String host;
    private final MutableUri uri;
    private final Context context;
    private Path sshConfigFile;
    
    @Parameters(name = "{index}: vagrant={0}")
    public static Collection<String> data() {
        return Arrays.asList("ubuntu14", "ubuntu16", "debian8", "centos7", "centos6", "freebsd102", "openbsd58");
    }
    
    @Before
    public void onlyIfAllVagrantMachinesRunning() {
        assumeTrue("Is vagrant host running?",
            TestHelper.VAGRANT_CLIENT.machinesRunning().contains(host));
        this.sshConfigFile = TestHelper.VAGRANT_CLIENT.sshConfig(host);
    }
    
    public SshIntegrationTest(String host) {
        this.host = host;
        this.uri = MutableUri.of("ssh://{}", host);
        // required before any blaze methods called...
        Config config = ConfigHelper.create(null);
        this.context = new ContextImpl(null, null, null, config);
        ContextHolder.set(this.context);
    }
    
    @Test
    public void proxy() throws Exception {
        try (SshSession proxy = sshConnect(uri).configFile(sshConfigFile).run()) {
            // if the proxy works then this very simple command should work
            // we have to manually set the username & password since vagrants
            // default ssh confit won't have "localhost" defined as a matching host
            SshConnect connect
                = sshConnect("ssh://localhost")
                    .username("vagrant")
                    .password("vagrant")
                    .configFile(sshConfigFile)
                    .hostChecking(false)
                    .proxy(proxy);
            try (SshSession session = connect.run()) {
                // when directly connected, env var is SSH_CLIENT=10.0.2.2 33224 22
                // when connected via localhost, env var is SSH_CLIENT=::1 52566 22 and SSH_TTY=/dev/pts/1
                CaptureOutput capture = Streamables.captureOutput();
        
                Integer exitValue
                    = new JschExec(context, session)
                        .command("echo $SSH_CLIENT")
                        .pipeOutput(capture)
                        .run();
                
                String sshClientEnvVar = capture.toString();
                
                assertThat(sshClientEnvVar, anyOf(startsWith("::1"), startsWith("127.0.0.1")));
            }
        }
    }
    
    @Test
    public void proxyViaProxyCommand() throws Exception {
        // build a new custom ssh config
        Path sshConfigFileWithProxyCommand = Files.createTempFile("vagrant.", ".ssh-config");
        Files.copy(sshConfigFile, sshConfigFileWithProxyCommand, StandardCopyOption.REPLACE_EXISTING);
        
        // extra config
        String extraConfig = new StringBuilder()
            .append("\r\n")
            .append("Host ").append(host).append("-lh").append("\r\n")
            .append(" HostName localhost\r\n")
            .append(" Port 22\r\n")
            .append(" User vagrant\r\n")
            .append(" ProxyCommand ssh ").append(host).append(" nc %h %p\r\n")
            .toString();
        
        Files.write(sshConfigFileWithProxyCommand, extraConfig.getBytes(StandardCharsets.UTF_8), StandardOpenOption.APPEND);
        
        // NOTE: to make it easier by not having to extract the identityfile
        // we'll just include the password here for auth
        SshConnect connect
            = sshConnect("ssh://" + host + "-lh")
                .configFile(sshConfigFileWithProxyCommand)
                .hostChecking(false)
                .password("vagrant");
        
        try (SshSession session = connect.run()) {
            // when directly connected, env var is SSH_CLIENT=10.0.2.2 33224 22
            // when connected via localhost, env var is SSH_CLIENT=::1 52566 22 and SSH_TTY=/dev/pts/1
            CaptureOutput capture = Streamables.captureOutput();

            Integer exitValue
                = new JschExec(context, session)
                    .command("echo $SSH_CLIENT")
                    .pipeOutput(capture)
                    .run();

            String sshClientEnvVar = capture.toString();

            assertThat(sshClientEnvVar, anyOf(startsWith("::1"), startsWith("127.0.0.1")));
        }
    }
    
    @Test
    public void sftpPutAndGet() throws Exception {
        Path exampleFile = FileHelper.resourceAsPath("/example/test1.txt");
        
        try (SshSession ssh = sshConnect(uri).configFile(sshConfigFile).run()) {
            try (SshSftpSession sftp = SecureShells.sshSftp(ssh).run()) {
                // make sure file does not exist on remote system
                sshExec(ssh, "rm", "-f", "test1.txt").run();
                
                sftp.put()
                    .source(exampleFile)
                    .target(exampleFile.getFileName())
                    .run();
                
                File tempFile = File.createTempFile("blaze.", ".sshtest");
                tempFile.deleteOnExit();
                
                sftp.get()
                    .source(exampleFile.getFileName())
                    .target(tempFile)
                    .run();
                
                // files match?
                assertTrue("The files differ!", FileUtils.contentEquals(tempFile, exampleFile.toFile()));
            }
        }
    }
    
    @Test
    public void sftpPutTwiceOverwrites() throws Exception {
        Path exampleFile = FileHelper.resourceAsPath("/example/test1.txt");
        
        try (SshSession ssh = sshConnect(uri).configFile(sshConfigFile).run()) {
            try (SshSftpSession sftp = SecureShells.sshSftp(ssh).run()) {
                // make sure file does not exist on remote system
                sshExec(ssh, "rm", "-f", "test1.txt").run();
                
                sftp.put()
                    .source(exampleFile)
                    .target(exampleFile.getFileName())
                    .run();
                
                sftp.put()
                    .source(exampleFile)
                    .target(exampleFile.getFileName())
                    .run();
            }
        }
    }
    
    @Test
    public void lstat() throws Exception {
        try (SshSession ssh = sshConnect(uri).configFile(sshConfigFile).run()) {
            try (SshSftpSession sftp = SecureShells.sshSftp(ssh).run()) {
                // regular file
                sshExec(ssh, "touch", "temp.txt").run();
                try {
                    SshFileAttributes attrs = sftp.lstat("temp.txt");
                    assertThat(attrs.isDirectory(), is(false));
                    assertThat(attrs.isRegularFile(), is(true));
                } finally {
                    sshExec(ssh, "rm", "-f", "temp.txt").run();
                }
                
                // non-existent file throws specific exception
                try {
                    SshFileAttributes attrs = sftp.lstat("file_does_not_exist.txt");
                    fail();
                } catch (SshSftpNoSuchFileException e) {
                    // expected specific exception
                }
                
                // lstat safely!
                SshFileAttributes attrs = sftp.lstatSafely("file_does_not_exist.txt");
                assertThat(attrs, is(nullValue()));
            }
        }
    }
    
}

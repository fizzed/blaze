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
import java.io.File;
import org.apache.commons.io.FileUtils;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.junit.Assume.assumeTrue;

/**
 * Real tests against actual hosts via ssh.
 * 
 * @author joelauer
 */
@RunWith(Parameterized.class)
public class SshIntegrationTest {
    static private final Logger log = LoggerFactory.getLogger(SshIntegrationTest.class);

    private final MutableUri uri;
    private final Context context;
    private final Path sshConfigFile;
    
    @Parameters(name = "{index}: vagrant={0}")
    public static Collection<String> data() {
        return Arrays.asList("freebsd102", "jessie64", "centos7");
    }
    
    @Before
    public void onlyIfAllVagrantMachinesRunning() {
        assumeTrue("Is vagrant running?", TestHelper.VAGRANT.areAllMachinesRunning());
    }
    
    public SshIntegrationTest(String hostname) {
        this.uri = MutableUri.of("ssh://{}", hostname);
        Config config = ConfigHelper.create(null);
        this.context = new ContextImpl(null, null, null, config);
        ContextHolder.set(this.context);
        this.sshConfigFile = TestHelper.VAGRANT.fetchSshConfig().toPath();
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

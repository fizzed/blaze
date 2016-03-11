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
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assume.assumeTrue;
import org.junit.BeforeClass;
import static org.mockito.Mockito.spy;


public class SshSftpTest {
    static private final Logger log = LoggerFactory.getLogger(SshSftpTest.class);

    static private Context context;
    
    @BeforeClass
    static public void initBlaze() {
        Config config = ConfigHelper.create(null);
        context = new ContextImpl(null, null, null, config);
        ContextHolder.set(context);
    }
    
    @Before
    public void onlyIfAllVagrantMachinesRunning() {
        boolean areAllMachinesRunning = TestHelper.VAGRANT.areAllMachinesRunning();
        log.info("vagrant running? {}", areAllMachinesRunning);
        assumeTrue("vagrant is running", areAllMachinesRunning);
    }
    
    @Test
    public void put() throws Exception {
        try (SshSession ssh = SecureShells.sshConnect("ssh://jessie64")
                .configFile(TestHelper.VAGRANT.fetchSshConfig().toPath())
                .run()) {
            try (SshSftpSession sftp = SecureShells.sshSftp(ssh).run()) {
                sftp.put().source("pom.xml").target("pom.xml").run();
            }
        }
        
        assertThat(true, is(true));
        
    }
    
}

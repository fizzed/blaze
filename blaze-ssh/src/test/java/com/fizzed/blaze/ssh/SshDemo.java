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

import com.fizzed.blaze.logging.LogLevel;
import com.fizzed.blaze.logging.LoggerConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.*;

import static com.fizzed.blaze.SecureShells.*;

public class SshDemo {
    static private final Logger log = LoggerFactory.getLogger(SshDemo.class);

    static public void main(String[] args) throws Exception {
        LoggerConfig.setDefaultLogLevel(LogLevel.TRACE);

        // what paths works
        // linux -> windows (works with / paths, root dir is /C:/path/as/you/would)

        // sftp demo
        try (SshSession sshSession = sshConnect("ssh://bmh-build-x64-win11-1").run()) {
            try (SshSftpSession sftp = sshSftp(sshSession).run()) {

                log.debug("pwd: {}", sftp.pwd());

                for (SshFile f : sftp.ls("/C:/Users/builder/remote-build/jne")) {
                    log.debug("ls: {}", f.path());
                }
/*
                sftp.cd("Downloads");

                sftp.get()
                    .progress()
                    .source("ubuntu-24.10-desktop-amd64.iso")
                    .target(Paths.get("test.deb"))
                    .run();

                sftp.put()
                    .source(Paths.get("test.deb"))
                    .target("test.deb")
                    .run();*/
            }
        }


        /*try (SshSession sshSession = sshConnect("ssh://bmh-jjlauer-4").run()) {
            try (SshSftpSession sftp = sshSftp(sshSession).run()) {
                for (SshFile f : sftp.ls("/home/jjlauer/Downloads")) {
                    log.debug("ls: {}", f.path());
                }
*//*
                sftp.cd("Downloads");

                sftp.get()
                    .progress()
                    .source("ubuntu-24.10-desktop-amd64.iso")
                    .target(Paths.get("test.deb"))
                    .run();

                sftp.put()
                    .source(Paths.get("test.deb"))
                    .target("test.deb")
                    .run();*//*
            }
        }*/

        // shell demo of whether ssh-agent is working
        // ssh to a machine using a specific identity that won't exist on the target machine
        /*try (SshSession sshSession = sshConnect("ssh://bmh-build-x64-ubuntu20-1").run()) {
            // now to test ssh'ing from that machine somewhere else
            sshExec(sshSession, "ssh", "-v", "-o", "StrictHostKeyChecking=no", "sshagentdemo@bmh-build-x64-ubuntu24-1", "hostname").run();
        }*/
    }

}
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
import com.fizzed.blaze.internal.FileHelper;
import com.fizzed.blaze.ssh.impl.JschExec;
import com.fizzed.blaze.util.CaptureOutput;
import com.fizzed.blaze.util.MutableUri;
import com.fizzed.blaze.util.Streamables;
import org.apache.commons.io.FileUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.Arrays;
import java.util.Collection;

import static com.fizzed.blaze.SecureShells.*;
import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;
import static org.junit.Assume.assumeTrue;

public class SshDemo {
    static private final Logger log = LoggerFactory.getLogger(SshDemo.class);

    static public void main(String[] args) throws Exception {
        try (SshSession sshSession = sshConnect("ssh://bmh-jjlauer-4").run()) {
            try (SshSftpSession sftp = sshSftp(sshSession).run()) {
                for (SshFile f : sftp.ls("/home/jjlauer/Downloads")) {
                    log.debug("ls: {}", f.path());
                }

                sftp.cd("Downloads");

                sftp.get()
                    .progress()
                    .source("ubuntu-24.10-desktop-amd64.iso")
                    .target(Paths.get("test.deb"))
                    .run();

                /*sftp.put()
                    .source(Paths.get("test.deb"))
                    .target("test.deb")
                    .run();*/
            }
        }
    }

}
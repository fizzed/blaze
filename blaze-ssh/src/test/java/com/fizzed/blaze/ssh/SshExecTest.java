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

import com.fizzed.blaze.core.UnexpectedExitValueException;
import com.fizzed.blaze.util.CaptureOutput;
import com.fizzed.blaze.util.Streamables;
import com.fizzed.blaze.util.WrappedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.atomic.AtomicBoolean;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.contains;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author joelauer
 */
public class SshExecTest extends SshBaseTest {
    static private final Logger log = LoggerFactory.getLogger(SshExecTest.class);

    @Test
    public void exitValue() throws Exception {
        // what happens when a command received over ssh
        commandHandler = (SshCommand command) -> {
            if (command.line.equals("hello")) {
                command.out.println("Hello World!");
                command.exit.onExit(0);
            } else {
                command.exit.onExit(1);
            }
        };
        
        SshSession session = startAndConnect();

        Integer exitValue
            = new SshExec(context, session)
                .command("hello")
                .run();
        
        assertThat(exitValue, is(0));
    }
    
    @Test
    public void unexpectedExitValue() throws Exception {
        // what happens when a command received over ssh
        commandHandler = (SshCommand command) -> {
            command.exit.onExit(1);
        };
        
        SshSession session = startAndConnect();

        try {
            new SshExec(context, session)
                .command("hello")
                .run();
            fail();
        } catch (UnexpectedExitValueException e) {
            assertThat(e.getActual(), is(1));
            assertThat(e.getExpected(), contains(0));
        }
    }
    
    @Test
    public void captureOutput() throws Exception {
        // what happens when a command received over ssh
        commandHandler = (SshCommand command) -> {
            if (command.line.equals("hello")) {
                command.outMessage("Hello World!");
                command.exit.onExit(0);
            } else {
                command.exit.onExit(1);
            }
        };
        
        SshSession session = startAndConnect();

        CaptureOutput capture = Streamables.captureOutput();
        
        Integer exitValue
            = new SshExec(context, session)
                .command("hello")
                .pipeOutput(capture)
                .run();
        
        assertThat(exitValue, is(0));
        assertThat(capture.toString(), is("Hello World!"));
    }
    
    @Test
    public void pipeErrorToOutput() throws Exception {
        // what happens when a command received over ssh
        commandHandler = (SshCommand command) -> {
            if (command.line.equals("error")) {
                command.errMessage("Hello World!");
                command.exit.onExit(0);
            } else {
                command.exit.onExit(1);
            }
        };
        
        SshSession session = startAndConnect();

        CaptureOutput capture = Streamables.captureOutput();
        
        Integer exitValue
            = new SshExec(context, session)
                .command("error")
                .pipeOutput(capture)
                .pipeErrorToOutput()
                .run();
        
        assertThat(exitValue, is(0));
        assertThat(capture.toString(), is("Hello World!"));
    }
    
    @Test
    public void environment() throws Exception {
        // what happens when a command received over ssh
        commandHandler = (SshCommand command) -> {
            if (command.line.equals("echo")) {
                command.env.getEnv().forEach((key, value) -> {
                    command.outMessageLn(key + " -> " + value);
                });
                command.exit.onExit(0);
            } else {
                command.exit.onExit(1);
            }
        };
        
        SshSession session = startAndConnect();

        CaptureOutput capture = Streamables.captureOutput();
        
        Integer exitValue
            = new SshExec(context, session)
                .command("echo")
                .env("JAVA_HOME", "/usr/java/default")
                .pipeOutput(capture)
                .run();
        
        assertThat(exitValue, is(0));
        assertThat(capture.toString(), containsString("JAVA_HOME -> /usr/java/default"));
    }
    
    @Test
    public void arguments() throws Exception {
        // what happens when a command received over ssh
        commandHandler = (SshCommand command) -> {
            if (command.line.equals("arg a=1 b=2")) {
                command.exit.onExit(0);
            } else {
                command.exit.onExit(1);
            }
        };
        
        SshSession session = startAndConnect();

        Integer exitValue
            = new SshExec(context, session)
                .command("arg")
                .arg("a=1")
                .args("b=2")
                .run();
        
        assertThat(exitValue, is(0));
    }
    
    @Test
    public void commandRetainsSlashes() throws Exception {
        // what happens when a command received over ssh
        commandHandler = (SshCommand command) -> {
            if (command.line.equals("/path/to/exec a=1 b=2")) {
                command.exit.onExit(0);
            } else {
                command.exit.onExit(1);
            }
        };
        
        SshSession session = startAndConnect();

        Integer exitValue
            = new SshExec(context, session)
                .command("/path/to/exec")
                .arg("a=1")
                .args("b=2")
                .run();
        
        assertThat(exitValue, is(0));
    }
    
    @Test
    public void verifyInputStreamNotBlockingJschExecThreadFromExiting() throws Exception {
        // what happens when a command received over ssh
        commandHandler = (SshCommand command) -> {
            if (command.line.equals("hello")) {
                command.out.println("Hello World!");
                command.exit.onExit(0);
            } else {
                command.exit.onExit(1);
            }
        };
        
        // due to an issue with JSCH, if you request the input
        // stream to not be closed, it results in exec threads sticking around
        // a hack to verify if the input stream is still in the "read" blocking
        // call even after the sshexec command has finished.  if it is then there
        // will definitely be a thread hanging around
        final AtomicBoolean inRead = new AtomicBoolean();
        InputStream is = new WrappedInputStream(System.in) {
            @Override
            public int read(byte[] b, int off, int len) throws IOException {
                try {
                    inRead.set(true);
                    return input.read(b, off, len);
                } finally {
                    inRead.set(false);
                }
            }
        };
        
        SshSession session = startAndConnect();

        Integer exitValue
            = new SshExec(context, session)
                .command("hello")
                .pipeInput(is)
                .run();
        
        assertThat(exitValue, is(0));
        assertThat(inRead.get(), is(false));
    }
}

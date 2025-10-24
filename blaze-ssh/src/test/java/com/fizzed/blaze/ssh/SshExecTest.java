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
import com.fizzed.blaze.ssh.impl.JschExec;
import com.fizzed.blaze.util.CaptureOutput;
import com.fizzed.blaze.util.StreamableInput;
import com.fizzed.blaze.util.StreamableOutput;
import com.fizzed.blaze.util.Streamables;
import com.fizzed.blaze.util.WrappedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.fizzed.blaze.util.IntRange.intRange;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.contains;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import org.junit.Test;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
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
            = new JschExec(context, session)
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
            new JschExec(context, session)
                .command("hello")
                .run();
            fail();
        } catch (UnexpectedExitValueException e) {
            assertThat(e.getActual(), is(1));
            assertThat(e.getExpected(), contains(intRange(0, 0)));
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
            = new JschExec(context, session)
                .command("hello")
                .pipeOutput(capture)
                .run();
        
        assertThat(exitValue, is(0));
        assertThat(capture.toString(), is("Hello World!"));
    }
    
    @Test
    public void runCaptureOutput() throws Exception {
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

        String output
            = new JschExec(context, session)
                .command("hello")
                .runCaptureOutput()
                .toString();

        assertThat(output, is("Hello World!"));
    }
    
    @Test
    public void disableInputAndOutputs() throws Exception {
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

        String output
            = new JschExec(context, session)
                .command("hello")
                .disablePipeInput()
                .disablePipeError()
                .runCaptureOutput()
                .toString();

        assertThat(output, is("Hello World!"));
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
            = new JschExec(context, session)
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
            = new JschExec(context, session)
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
            = new JschExec(context, session)
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
            = new JschExec(context, session)
                .command("/path/to/exec")
                .arg("a=1")
                .args("b=2")
                .run();
        
        assertThat(exitValue, is(0));
    }
    
    @Test
    public void nullInputAndOutputs() throws Exception {
        // what happens when a command received over ssh
        commandHandler = (SshCommand command) -> {
            command.exit.onExit(1);
        };
        
        SshSession session = startAndConnect();
        
        Integer exitValue
            = new JschExec(context, session)
                .command("hello")
                .exitValues(1)
                .pipeInput((StreamableInput)null)
                .pipeOutput((StreamableOutput)null)
                .pipeError((StreamableOutput)null)
                .run();
        
        assertThat(exitValue, is(1));
    }
    
    @Test
    public void standardInputAndOutputs() throws Exception {
        // what happens when a command received over ssh
        commandHandler = (SshCommand command) -> {
            command.exit.onExit(0);
        };
        
        // mimic standard input and outputs
        InputStream in = spy(new ByteArrayInputStream(new byte[0]));
        OutputStream out = spy(new ByteArrayOutputStream());
        OutputStream err = spy(new ByteArrayOutputStream());
        
        SshSession session = startAndConnect();
        
        Integer exitValue
            = new JschExec(context, session)
                .command("hello")
                .pipeInput(in)
                .pipeOutput(out)
                .pipeError(err)
                .run();
        
        assertThat(exitValue, is(0));
        
        // verify all streams were closed
//        verify(in).close();
        verify(out).close();
        verify(err).close();
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
            = new JschExec(context, session)
                .command("hello")
                .pipeInput(is)
                .run();
        
        assertThat(exitValue, is(0));
        assertThat(inRead.get(), is(false));
    }
}

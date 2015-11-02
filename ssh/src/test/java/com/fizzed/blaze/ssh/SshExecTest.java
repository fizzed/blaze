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

        SshExecResult result
            = new SshExec(context, session)
                .command("hello")
                .run();
        
        assertThat(result.exitValue(), is(0));
    }
    
    @Test
    public void unexpectedExitValue() throws Exception {
        // what happens when a command received over ssh
        commandHandler = (SshCommand command) -> {
            command.exit.onExit(1);
        };
        
        SshSession session = startAndConnect();

        try {
            SshExecResult result
                = new SshExec(context, session)
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

        SshExecResult result
            = new SshExec(context, session)
                .command("hello")
                .captureOutput()
                .run();
        
        assertThat(result.exitValue(), is(0));
        assertThat(result.output(), is("Hello World!"));
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

        SshExecResult result
            = new SshExec(context, session)
                .command("error")
                .captureOutput()
                .pipeErrorToOutput()
                .run();
        
        assertThat(result.exitValue(), is(0));
        assertThat(result.output(), is("Hello World!"));
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

        SshExecResult result
            = new SshExec(context, session)
                .command("echo")
                .env("JAVA_HOME", "/usr/java/default")
                .captureOutput()
                .run();
        
        assertThat(result.exitValue(), is(0));
        assertThat(result.output(), containsString("JAVA_HOME -> /usr/java/default"));
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

        SshExecResult result
            = new SshExec(context, session)
                .command("arg")
                .arg("a=1")
                .args("b=2")
                .run();
        
        assertThat(result.exitValue(), is(0));
    }
}

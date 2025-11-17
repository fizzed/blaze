/*
 * Copyright 2020 Fizzed, Inc.
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
package com.fizzed.blaze.ssh.impl;

import com.fizzed.blaze.ssh.*;
import com.fizzed.blaze.Context;
import com.fizzed.blaze.core.UnexpectedExitValueException;
import com.fizzed.blaze.core.BlazeException;
import com.fizzed.blaze.ssh.util.SshArguments;
import com.fizzed.blaze.system.Exec;
import com.fizzed.blaze.util.ObjectHelper;
import com.fizzed.blaze.util.WrappedOutputStream;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import com.fizzed.blaze.util.InterruptibleInputStream;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.io.output.NullOutputStream;

public class JschExec extends SshExec {
    
    public JschExec(Context context, SshSession session) {
        super(context, session);
    }
    
    @Override
    public List<Path> getPaths() {
        throw new UnsupportedOperationException("getPaths() not supported in SSH exec");
    }
    
    @Override
    protected Exec.Result doRun() throws BlazeException {
        Session jschSession = ((JschSession)session).getJschSession();
        
        ObjectHelper.requireNonNull(jschSession, "ssh session must be established first");
        ObjectHelper.requireNonNull(command, "ssh command cannot be null");
        
        ChannelExec channel = null;
        try {
            channel = (ChannelExec)jschSession.openChannel("exec");
            
            if (this.pty) {
                channel.setPty(true);
            }

            // setup environment
            if (this.environment != null) {
                for (Map.Entry<String,String> entry : this.environment.entrySet()) {
                    log.debug("Adding env {}={}", entry.getKey(), entry.getValue());
                    channel.setEnv(entry.getKey(), entry.getValue());
                }
            }
            
            // NOTE: In order for JSCH to pump the inputstream to its outputstream
            // it starts an "exec thread" for every channel-exec it creates
            // that also includes non-null inputstream. The problem is that
            // JSCH's exec thread will block forever on the inputstream.read()
            // call unless the inputstream is actually closed.  Since we don't
            // want that behavior, we'll sneakily introduce a wrapped inputstream
            // that will supply an interruptible read() call. This is done in
            // 2 steps:  1) our read() method will use a combo of Thread.sleep()
            // and available() to provide a non-blocking read() that will also
            // response to a Thread.interrupt() call.  and 2) we'll capture
            // a reference to JSCH's exec thread by saving it when it actually
            // enters the read() method.
            
            // setup in/out streams
            final InputStream is = (pipeInput != null ? new InterruptibleInputStream(pipeInput.stream()) : null);
            final OutputStream os = (pipeOutput != null ? pipeOutput.stream() : new NullOutputStream());
            final OutputStream es = (pipeErrorToOutput ? os : (pipeError != null ? pipeError.stream() : new NullOutputStream()));
            
            if (is != null) {
                channel.setInputStream(is, true);
            }
            
            // both streams closing signals exec is finished
            final CountDownLatch outputStreamClosedSignal = new CountDownLatch(1);
            final CountDownLatch errorStreamClosedSignal = new CountDownLatch(1);
            
            // determine final ouput and then wrap to monitor for close events
            if (os != null) {
                channel.setOutputStream(new WrappedOutputStream(os) {
                    @Override
                    public void close() throws IOException {
                        try {
                            super.close();
                        } finally {
                            outputStreamClosedSignal.countDown();
                        }
                    }
                }, false);
            } else {
                outputStreamClosedSignal.countDown();
            }
            
            // determine final error and then wrap to monitor for close events
            if (es != null) {
                channel.setErrStream(new WrappedOutputStream(es) {
                    @Override
                    public void close() throws IOException {
                        try {
                            super.close();
                        } finally {
                            errorStreamClosedSignal.countDown();
                        }
                    }
                }, false);
            } else {
                errorStreamClosedSignal.countDown();
            }
            
            
            // build command line arguments
            final List<String> c = new ArrayList<>();
            
            if (this.sudo) {
//            if (commands.containsKey("doas")) {
//                arguments.add("doas");
//            } else {
                // man sudo
                // -S  The -S (stdin) option causes sudo to read the password from the
                //     standard input instead of the terminal device.
                c.add("sudo");
                c.add("-S");
//            }
            }

            if (this.shell) {
                c.add("sh");
                c.add("-c");
            }
            
            c.add(command.toString());
            c.addAll(this.arguments);

            final String finalCommand = SshArguments.buildEscapedCommand(c);
            
            /*// building the command may be a little tricky, not sure about spaces...
            final StringBuilder sb = new StringBuilder();
            
            c.stream().forEach((arg) -> {
                if (sb.length() > 0) {
                    sb.append(" ");
                }
                if (arg.contains(" ")) {
                    sb.append("'");
                    sb.append(arg);
                    sb.append("'");
                } else {
                    sb.append(arg);
                }
            });
            
            String finalCommand = sb.toString();*/

            if (log.isVerbose()) {
                String workingDir = "";
                String env = "";
                if (this.workingDirectory != null) {
                    workingDir = " in working dir [" + this.workingDirectory + "]";
                }
                if (!this.environment.isEmpty()) {
                    env = " with env " + this.environment;
                }
                log.verbose("SshExec [{}]{}{}", finalCommand, workingDir, env);
            }
            
            channel.setCommand(finalCommand);
            
            // this connects and sends command
            channel.connect();
            
            // wait for both streams to be closed
            outputStreamClosedSignal.await();
            errorStreamClosedSignal.await();
            
            final Integer exitValue = channel.getExitStatus();

            // check the exit value (or skip checking if none are defined)
            UnexpectedExitValueException.checkExitValue(this.exitValues, exitValue);
            
            return new Exec.Result(this, exitValue);
        } catch (JSchException | InterruptedException e) {
            throw new SshException(e.getMessage(), e);
        } finally {
            if (channel != null) {
                channel.disconnect();
            }
        }
    }
    
}
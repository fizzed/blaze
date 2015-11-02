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
package com.fizzed.blaze.ssh.impl;

import com.fizzed.blaze.ssh.impl.JschSession;
import com.fizzed.blaze.Context;
import com.fizzed.blaze.core.Action;
import com.fizzed.blaze.core.UnexpectedExitValueException;
import com.fizzed.blaze.core.BlazeException;
import com.fizzed.blaze.ssh.SshSession;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Path;
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author joelauer
 */
public class SshScpBETA extends Action<Void> {
    static private final Logger log = LoggerFactory.getLogger(SshScpBETA.class);

    final private SshSession session;
    private InputStream source;
    private Path target;
    
    /**
    private String command;
    final private List<String> arguments;
    private InputStream pipeInput;
    private OutputStream pipeOutput;
    private OutputStream pipeError;
    private boolean pipeErrorToOutput;
    private ByteArrayOutputStream captureOutputStream;
    private Map<String,String> environment;
    private long timeout;
    final private List<Integer> exitValues;
    */
    
    public SshScpBETA(Context context, SshSession session) {
        super(context);
        this.session = session;
    }
    
    public SshScpBETA source(InputStream source) {
        this.source = source;
        return this;
    }
    
    public SshScpBETA target(Path target) {
        this.target = target;
        return this;
    }

    @Override
    protected Void doRun() throws BlazeException {
        Session jschSession = ((JschSession)session).getJschSession();
        Objects.requireNonNull(jschSession, "ssh session must be established first");
        Objects.requireNonNull(source, "scp source cannot be null");
        Objects.requireNonNull(target, "scp target cannot be null");
        
        ChannelExec channel = null;
        try {
             boolean ptimestamp = true;

            channel = (ChannelExec)jschSession.openChannel("exec");
            
            // exec 'scp -t rfile' remotely
            String command = "scp " + (ptimestamp ? "-p" : "") + " -t " + target;
            channel.setCommand(command);
            
            // YIKES - JSCH IS LOW LEVEL FOR SCP!!!
            
            /**
            // do not close input
            channel.setInputStream(this.pipeInput, true);
            
            // both streams closing signals exec is finished
            CountDownLatch outputStreamClosedSignal = new CountDownLatch(1);
            CountDownLatch errorStreamClosedSignal = new CountDownLatch(1);
            
            // determine final ouput and then wrap to monitor for close events
            OutputStream finalPipeOutput = (this.captureOutputStream != null ? this.captureOutputStream : this.pipeOutput);
            
            channel.setOutputStream(new WrappedOutputStream(finalPipeOutput) {
                @Override
                public void close() throws IOException {
                    outputStreamClosedSignal.countDown();
                }
            }, false);
            
            // determine final error and then wrap to monitor for close events
            OutputStream finalPipeError = (this.pipeErrorToOutput ? finalPipeOutput : this.pipeError);
            
            channel.setErrStream(new WrappedOutputStream(finalPipeError) {
                @Override
                public void close() throws IOException {
                    errorStreamClosedSignal.countDown();
                }
            }, false);
            
            // building the command may be a little tricky, not sure about spaces...
            final StringBuilder sb = new StringBuilder(command);
            
            arguments.stream().forEach((arg) -> {
                sb.append(" ");
                // TODO: should we actually escape instead such as " " -> "\ "???
                if (arg.contains(" ")) {
                    sb.append("'");
                    sb.append(arg);
                    sb.append("'");
                } else {
                    sb.append(arg);
                }
            });
            
            String finalCommand = sb.toString();
            
            log.debug("Sending command via ssh session: {}", finalCommand);
            */
            
            // get I/O streams for remote scp
            OutputStream out = channel.getOutputStream();
            InputStream in = channel.getInputStream();
            
            // this connects and sends command
            channel.connect();
            
            checkAck(in);
            
            /**
            // wait for both streams to be closed
            outputStreamClosedSignal.await();
            errorStreamClosedSignal.await();
            
            Integer exitValue = channel.getExitStatus();
            
            if (!this.exitValues.contains(exitValue)) {
                throw new UnexpectedExitValueException("Process exited with unexpected value", this.exitValues, exitValue);
            }
            
            // success!
            return new SshExecResult(exitValue, captureOutputStream);
            */
            
            return null;
        } catch (JSchException | IOException e) {
            throw new BlazeException(e.getMessage(), e);
        } finally {
            if (channel != null) {
                channel.disconnect();
            }
        }
    }
    
    static int checkAck(InputStream in) throws IOException {
        int b = in.read();
        // b may be 0 for success,
        //          1 for error,
        //          2 for fatal error,
        //          -1
        if (b == 0) {
            return b;
        }
        if (b == -1) {
            return b;
        }

        if (b == 1 || b == 2) {
            StringBuilder sb = new StringBuilder();
            int c;
            do {
                c = in.read();
                sb.append((char) c);
            } while (c != '\n');
            if (b == 1) { // error
                throw new IOException(sb.toString());
            }
            if (b == 2) { // fatal error
                throw new IOException(sb.toString());
            }
        }
        return b;
    }
    
}

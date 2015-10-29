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

import com.fizzed.blaze.Context;
import com.fizzed.blaze.core.Action;
import com.fizzed.blaze.core.BlazeException;
import com.fizzed.blaze.internal.ObjectHelper;
import com.fizzed.blaze.system.ExecSupport;
import com.fizzed.blaze.util.WrappedOutputStream;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CountDownLatch;
import java.util.logging.Level;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author joelauer
 */
public class SshExec extends Action<Void> implements ExecSupport<SshExec> {
    static private final Logger log = LoggerFactory.getLogger(SshExec.class);

    final private SshSession session;
    private String command;
    final private List<String> arguments;
    private ByteArrayOutputStream captureOutputStream;
    private Map<String,String> environment;
    private long timeout;
    
    public SshExec(Context context, SshSession session) {
        super(context);
        this.session = session;
        this.arguments = new ArrayList<>();
    }
    
    public SshExec command(String command) {
        this.command = command;
        return this;
    }
    
    @Override
    public SshExec command(String command, Object... arguments) {
        this.command(command);
        this.args(arguments);
        return this;
    }
    
    @Override
    public SshExec arg(Object... arguments) {
        this.arguments.addAll(ObjectHelper.toStringList(arguments));
        return this;
    }

    @Override
    public SshExec args(Object... arguments) {
        this.arguments.clear();
        this.arguments.addAll(ObjectHelper.toStringList(arguments));
        return this;
    }

    @Override
    public SshExec captureOutput() {
        if (this.captureOutputStream == null) {
            this.captureOutputStream = new ByteArrayOutputStream();
        }
        return this;
    }

    @Override
    public SshExec env(String name, String value) {
        if (this.environment == null) {
            this.environment = new HashMap<>();
        }
        this.environment.put(name, value);
        return this;
    }

    @Override
    public SshExec timeout(long timeoutInMillis) {
        this.timeout = timeoutInMillis;
        return this;
    }

    @Override
    protected Void doRun() throws BlazeException {
        Session jschSession = session.getJschSession();
        Objects.requireNonNull(jschSession, "ssh session must be established first");
        
        ChannelExec channel = null;
        try {
            channel = (ChannelExec)jschSession.openChannel("exec");
            
            // setup environment
            if (this.environment != null) {
                for (Map.Entry<String,String> entry : this.environment.entrySet()) {
                    channel.setEnv(entry.getKey(), entry.getValue());
                }
            }
            
            // do not close input
            channel.setInputStream(System.in, true);
            
            // both streams closing signals exec is finished
            CountDownLatch outputStreamClosedSignal = new CountDownLatch(1);
            CountDownLatch errorStreamClosedSignal = new CountDownLatch(1);
            
            channel.setOutputStream(new WrappedOutputStream(System.out) {
                @Override
                public void close() throws IOException {
                    outputStreamClosedSignal.countDown();
                }
            }, false);
            
            channel.setErrStream(new WrappedOutputStream(System.err) {
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
            
            channel.setCommand(finalCommand);
            
            // this connects and sends command
            channel.connect();
            
            // wait for both streams to be closed
            outputStreamClosedSignal.await();
            errorStreamClosedSignal.await();
            
            log.info("exit status: {}", channel.getExitStatus());
            
        } catch (JSchException e) {
            throw new BlazeException(e.getMessage(), e);
        } catch (InterruptedException ex) {
            java.util.logging.Logger.getLogger(SshExec.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            if (channel != null) {
                channel.disconnect();
            }
        }
        
        return null;
    }
    
}

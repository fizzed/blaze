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

import com.fizzed.blaze.util.Streamables;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import org.apache.sshd.server.command.Command;
import org.apache.sshd.server.Environment;
import org.apache.sshd.server.ExitCallback;
import org.apache.sshd.server.channel.ChannelSession;

/**
 *
 * @author joelauer
 */
public class SshCommand implements Command {
    final SshCommandHandler handler;
    final String line;
    InputStream in;
    PrintStream out;
    PrintStream err;
    ExitCallback exit;
    Environment env;

    public SshCommand(SshCommandHandler handler, String line) {
        this.handler = handler;
        this.line = line;
    }

    public void outMessage(String message) {
        this.out.print(message);
        this.out.flush();
    }
    
    public void outMessageLn(String message) {
        this.out.println(message);
        this.out.flush();
    }
    
    public void errMessage(String message) {
        this.err.print(message);
        this.err.flush();
    }
    
    public void errMessageLn(String message) {
        this.err.println(message);
        this.err.flush();
    }
    
    @Override
    public void setInputStream(InputStream in) {
        this.in = in;
    }

    @Override
    public void setOutputStream(OutputStream out) {
        this.out = new PrintStream(out);
    }

    @Override
    public void setErrorStream(OutputStream err) {
        this.err = new PrintStream(err);
    }

    @Override
    public void setExitCallback(ExitCallback ec) {
        this.exit = ec;
    }
//
//    public void start(Environment e) throws IOException {
//        this.env = e;
//        this.handler.handle(this);
//    }
//
//    public void destroy() {
//        Streamables.close(in);
//        Streamables.close(out);
//        Streamables.close(err);
//    }

    @Override
    public void start(ChannelSession cs, Environment e) throws IOException {
        this.env = e;
        this.handler.handle(this);
    }

    @Override
    public void destroy(ChannelSession cs) throws Exception {
        Streamables.close(in);
        Streamables.close(out);
        Streamables.close(err);
    }
    
}

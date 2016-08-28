/*
 * Copyright 2016 Fizzed, Inc.
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

import com.fizzed.blaze.ssh.SshSession;
import com.jcraft.jsch.ChannelDirectTCPIP;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.Proxy;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SocketFactory;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.net.Socket;
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JschExecProxy implements Proxy {
    static private final Logger log = LoggerFactory.getLogger(JschExecProxy.class);

    private final SshSession session;
    private final Session jschSession;
    private final boolean autoclose;
    private final String command;
    private ChannelExec channel;
    
    static public JschExecProxy of(SshSession session, boolean autoclose) {
        // default command is netcat
        return of(session, autoclose, null);
    }
    
    static public JschExecProxy of(SshSession session, boolean autoclose, String command) {
        Objects.requireNonNull(session);
        if (session instanceof JschSession) {
            // unwrap jsch session
            return new JschExecProxy(session, ((JschSession)session).getJschSession(), autoclose,
                (command != null ? command : "nc %h %p"));
        }
        throw new IllegalArgumentException("SshSession was not an instanceof " + JschSession.class.getCanonicalName()
            + " (actual = " + session.getClass().getCanonicalName() + ")");
    }
    
    private JschExecProxy(SshSession session, Session jschSession, boolean autoclose, String command) {
        Objects.requireNonNull(session);
        Objects.requireNonNull(jschSession);
        this.session = session;
        this.jschSession = jschSession;
        this.autoclose = autoclose;
        this.command = command;
    }

    @Override
    public void connect(final SocketFactory socketFactory, final String host, final int port, final int timeout) throws Exception {
        log.debug("ssh proxy connect(host={}, port={}, timeout={})", host, port, timeout);
        
        // replace command with values
        String finalCommand = command
            .replace("%h", host)
            .replace("%p", Integer.toString(port));
        
        log.debug("ssh proxy will exec({})", finalCommand);
        
        channel = (ChannelExec)jschSession.openChannel("exec");
        channel.setCommand(finalCommand);
        channel.connect(timeout);
    }

    @Override
    public InputStream getInputStream() {
        log.debug("ssh proxy getInputStream()");
        try {
            return channel.getInputStream();
        } catch (IOException e) {
            throw new UncheckedIOException("IOException getting the SSH proxy inputstream", e);
        }
    }

    @Override
    public OutputStream getOutputStream() {
        log.debug("ssh proxy getOutputStream()");
        try {
            return channel.getOutputStream();
        } catch (IOException e) {
            throw new UncheckedIOException("IOException getting the SSH proxy outputstream", e);
        }
    }

    @Override
    public Socket getSocket() {
        log.debug("ssh proxy getSocket()");
        return null;
    }

    @Override
    public void close() {
        log.debug("ssh proxy close()");
        if (channel != null) {
            channel.disconnect();
        }
        if (autoclose) {
            try {
                this.session.close();
            } catch (IOException e) {
                log.error("Unable to cleanly close proxy ssh session", e);
            }
        }
    }
    
    @Override
    public String toString() {
        return this.jschSession.getHost();
    }
}
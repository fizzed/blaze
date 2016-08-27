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

public class JschProxy implements Proxy {
    static private final Logger log = LoggerFactory.getLogger(JschProxy.class);

    private final SshSession session;
    private final Session jschSession;
    private final boolean autoclose;
    private ChannelExec channel;
    

    static public JschProxy of(SshSession session, boolean autoclose) {
        Objects.requireNonNull(session);
        if (session instanceof JschSession) {
            // unwrap jsch session
            return new JschProxy(session, ((JschSession)session).getJschSession(), autoclose);
        }
        throw new IllegalArgumentException("SshSession was not an instanceof " + JschSession.class.getCanonicalName()
            + " (actual = " + session.getClass().getCanonicalName() + ")");
    }
    
    private JschProxy(SshSession session, Session jschSession,boolean autoclose) {
        Objects.requireNonNull(session);
        Objects.requireNonNull(jschSession);
        this.session = session;
        this.jschSession = jschSession;
        this.autoclose = autoclose;
    }

    @Override
    public void connect(final SocketFactory socketFactory, final String host, final int port, final int timeout) throws Exception {
        log.debug("ssh proxy connect(host={}, port={}, timeout={})", host, port, timeout);
        channel = (ChannelExec)jschSession.openChannel("exec");
        String command = String.format("nc %s %d", host, port);
        channel.setCommand(command);
        //log.info("Executing command on proxy host: {}", command);
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
        try {
            this.session.close();
        } catch (IOException e) {
            log.error("Unable to cleanly close proxy ssh session", e);
        }
    }
    
    @Override
    public String toString() {
        return this.jschSession.getHost();
    }
}
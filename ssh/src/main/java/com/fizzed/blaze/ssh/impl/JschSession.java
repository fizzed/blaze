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

import com.fizzed.blaze.Context;
import com.fizzed.blaze.ssh.SshSession;
import com.fizzed.blaze.util.ImmutableUri;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;
import java.io.IOException;
import java.util.Objects;

/**
 *
 * @author joelauer
 */
public class JschSession implements SshSession {
    
    final private Context context;
    final private ImmutableUri uri;
    final private JSch jsch;
    final private Session jschSession;
    private boolean closed;

    public JschSession(Context context, ImmutableUri uri, JSch jsch, Session jschSession) {
        Objects.requireNonNull(uri, "uri cannot be null");
        Objects.requireNonNull(jsch, "jsch cannot be null");
        Objects.requireNonNull(jschSession, "jsch session cannot be null");
        this.context = context;
        this.uri = uri;
        this.jsch = jsch;
        this.jschSession = jschSession;
        this.closed = false;
    }
    
    @Override
    public Context context() {
        return this.context;
    }

    @Override
    public ImmutableUri uri() {
        return uri;
    }

    @Override
    public boolean closed() {
        return this.closed;
    }
    
    @Override
    public void close() throws IOException {
        if (!this.closed && this.jschSession != null) {
             try {
                 this.jschSession.disconnect();
             } catch (Exception e) {
                 // not sure this matters
             }
             this.closed = true;
        }
    }

    public JSch getJsch() {
        return jsch;
    }

    public Session getJschSession() {
        return jschSession;
    }
    
}

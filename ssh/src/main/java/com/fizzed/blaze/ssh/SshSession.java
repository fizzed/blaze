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

import com.fizzed.blaze.core.MutableUri;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;

/**
 *
 * @author joelauer
 */
public class SshSession {
    
    final private MutableUri uri;
    final private JSch jsch;
    final private Session jschSession;

    public SshSession(MutableUri uri, JSch jsch, Session jschSession) {
        this.uri = uri;
        this.jsch = jsch;
        this.jschSession = jschSession;
    }

    public MutableUri getUri() {
        return uri;
    }

    public JSch getJsch() {
        return jsch;
    }

    public Session getJschSession() {
        return jschSession;
    }
    
}

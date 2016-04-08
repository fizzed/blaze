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
package com.fizzed.blaze.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.atomic.AtomicBoolean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Guards against a close, but still supports returning -1 or IOExceptions as
 * if the underlying stream was closed.
 */
public class CloseGuardedInputStream extends WrappedInputStream {
    private static final Logger log = LoggerFactory.getLogger(CloseGuardedInputStream.class);
    
    private AtomicBoolean closed;
        
    public CloseGuardedInputStream(InputStream input) {
        super(input);
        this.closed = new AtomicBoolean(false);
    }

    @Override
    public int read() throws IOException {
        if (closed.get()) return -1;
        return super.read();
    }

    @Override
    public int read(byte[] b) throws IOException {
        if (closed.get()) return -1;
        return super.read(b);
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        if (closed.get()) return -1;
        return super.read(b, off, len);
    }

    @Override
    public int available() throws IOException {
        if (closed.get()) throw new IOException("stream closed");
        return super.available();
    }

    @Override
    public void close() throws IOException {
        log.trace("Closing guarded inputstream");
        // do not close underlying stream, but mark us as closed
        this.closed.compareAndSet(false, true);
    }
    
}

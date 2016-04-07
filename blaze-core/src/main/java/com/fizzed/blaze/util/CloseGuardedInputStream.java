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

/**
 * Guards against a close, but still supports returning -1 or IOExceptions as
 * if the underlying stream was closed.
 */
public class CloseGuardedInputStream extends WrappedInputStream {
    
    private boolean closed;
        
    public CloseGuardedInputStream(InputStream input) {
        super(input);
        this.closed = false;
    }

    @Override
    public int read() throws IOException {
        if (closed) return -1;
        return super.read();
    }

    @Override
    public int read(byte[] b) throws IOException {
        if (closed) return -1;
        return super.read(b);
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        if (closed) return -1;
        return super.read(b, off, len);
    }

    @Override
    public int available() throws IOException {
        if (closed) throw new IOException("stream closed");
        return super.available();
    }

    @Override
    public void close() throws IOException {
        // do not close underlying stream, but mark us as closed
        this.closed = true;
    }
    
}

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
import java.io.OutputStream;

/**
 * Guards against an actual close of an output stream.
 */
public class CloseGuardedOutputStream extends WrappedOutputStream {
    
    private boolean closed;
    
    public CloseGuardedOutputStream(OutputStream output) {
        super(output);
        this.closed = false;
    }

    @Override
    public void write(int b) throws IOException {
        if (closed) throw new IOException("Stream closed");
        super.write(b);
    }

    @Override
    public void write(byte[] b) throws IOException {
        if (closed) throw new IOException("Stream closed");
        super.write(b);
    }

    @Override
    public void write(byte[] b, int off, int len) throws IOException {
        if (closed) throw new IOException("Stream closed");
        super.write(b, off, len);
    }

    @Override
    public void flush() throws IOException {
        if (closed) throw new IOException("Stream closed");
        super.flush();
    }

    @Override
    public void close() throws IOException {
        this.closed = true;
    }

}

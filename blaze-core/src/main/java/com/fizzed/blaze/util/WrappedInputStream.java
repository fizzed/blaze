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
package com.fizzed.blaze.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;

/**
 * Wraps an input stream.  Useful for subclassing and overriding methods
 * to either listen for events or modify behavior slightly (e.g. not allowing
 * a stream to actually be closed).
 * 
 * @author joelauer
 */
public class WrappedInputStream extends InputStream {
 
    final protected InputStream input;
    
    public WrappedInputStream(InputStream input) {
        Objects.requireNonNull(input, "input cannot be null");
        this.input = input;
    }

    public InputStream wrappedStream() {
        return this.input;
    }
    
    @Override
    public boolean markSupported() {
        return input.markSupported();
    }

    @Override
    public synchronized void reset() throws IOException {
        input.reset();
    }

    @Override
    public synchronized void mark(int readlimit) {
        input.mark(readlimit);
    }

    @Override
    public void close() throws IOException {
        input.close();
    }

    @Override
    public int available() throws IOException {
        return input.available();
    }

    @Override
    public long skip(long n) throws IOException {
        return input.skip(n);
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        return input.read(b, off, len);
    }

    @Override
    public int read(byte[] b) throws IOException {
        return input.read(b);
    }

    @Override
    public int read() throws IOException {
        return input.read();
    }
    
}
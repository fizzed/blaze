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
import java.io.OutputStream;
import java.util.Objects;

/**
 * Wraps an output stream.  Useful for subclassing and overriding methods
 * to either listen for events or modify behavior slightly (e.g. not allowing
 * a stream to actually be closed).
 * 
 * @author joelauer
 */
public class WrappedOutputStream extends OutputStream {
 
    final protected OutputStream output;
    
    public WrappedOutputStream(OutputStream output) {
        Objects.requireNonNull(output, "output cannot be null");
        this.output = output;
    }

    @Override
    public void close() throws IOException {
        output.close();
    }

    @Override
    public void flush() throws IOException {
        output.flush();
    }

    @Override
    public void write(byte[] b, int off, int len) throws IOException {
        output.write(b, off, len);
    }

    @Override
    public void write(byte[] b) throws IOException {
        output.write(b);
    }

    @Override
    public void write(int b) throws IOException {
        output.write(b);
    }
}
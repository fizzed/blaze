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

import com.fizzed.blaze.internal.FileNotFoundException;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;

/**
 * Defer opening a File as an InputStream until the first attempt to access it.
 * 
 * @author joelauer
 */
public class DeferredFileInputStream extends InputStream {

    private final File file;
    private InputStream input;
    
    public DeferredFileInputStream(File file) {
        if (!file.exists()) {
            throw new FileNotFoundException("File " + file + " not found");
        }
        this.file = file;
    }
    
    public DeferredFileInputStream(Path path) throws FileNotFoundException {
        this(path.toFile());
    }
    
    public void open() {
        if (this.input == null) {
            try {
                this.input = new FileInputStream(file);
            } catch (Exception e) {
                throw new FileNotFoundException(e.getMessage(), e);
            }
        }
    }
    
    @Override
    public int read() throws IOException {
        open();
        return this.input.read();
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        open();
        return this.input.read(b, off, len);
    }

    @Override
    public int read(byte[] b) throws IOException {
        open();
        return this.input.read(b);
    }
    
    @Override
    public boolean markSupported() {
        open();
        return this.input.markSupported();
    }

    @Override
    public synchronized void reset() throws IOException {
        open();
        this.input.reset();
    }

    @Override
    public synchronized void mark(int readlimit) {
        open();
        this.input.mark(readlimit);
    }

    @Override
    public void close() throws IOException {
        if (this.input != null) {
            this.input.close();
            this.input = null;
        }
    }

    @Override
    public int available() throws IOException {
        open();
        return this.input.available();
    }

    @Override
    public long skip(long n) throws IOException {
        open();
        return this.input.skip(n);
    }

}

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

import com.fizzed.blaze.core.FileNotFoundException;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.util.Objects;

/**
 * Defer opening a File as an OutputStream until the first attempt to access it.
 * 
 * @author joelauer
 */
public class DeferredFileOutputStream extends OutputStream {

    private final Path file;
    private final Path temporaryFile;
    private OutputStream output;
    
    public DeferredFileOutputStream(Path file, boolean useTemporaryFile) {
        Objects.requireNonNull(file, "file cannot be null");
        this.file = file;
        if (useTemporaryFile) {
            this.temporaryFile = file.resolveSibling(file.getFileName() + ".tmp");
        } else {
            this.temporaryFile = null;
        }
    }
    
    public void open() {
        if (this.output == null) {
            try {
                final Path fileToOpen = this.temporaryFile != null ? this.temporaryFile : this.file;
                this.output = Files.newOutputStream(fileToOpen, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
            } catch (Exception e) {
                throw new FileNotFoundException(e.getMessage(), e);
            }
        }
    }

    @Override
    public void close() throws IOException {
        if (this.output != null) {
            this.output.close();
            this.output = null;
            // if using a temporary file, now if the time to swap it to the final file
            if (this.temporaryFile != null) {
                Files.move(this.temporaryFile, this.file, StandardCopyOption.REPLACE_EXISTING);
            }
        }
    }

    @Override
    public void flush() throws IOException {
        open();
        output.flush();
    }

    @Override
    public void write(byte[] b, int off, int len) throws IOException {
        open();
        output.write(b, off, len);
    }

    @Override
    public void write(byte[] b) throws IOException {
        open();
        output.write(b);
    }

    @Override
    public void write(int b) throws IOException {
        open();
        output.write(b);
    }
}
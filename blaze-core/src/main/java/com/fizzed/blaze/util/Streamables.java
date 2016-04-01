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

import com.fizzed.blaze.core.BlazeException;
import com.fizzed.blaze.core.FileNotFoundException;
import com.fizzed.blaze.core.WrappedBlazeException;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;
import org.apache.commons.io.input.NullInputStream;
import org.apache.commons.io.output.NullOutputStream;

public class Streamables {

    static public StreamableInput nullInput() {
        return new StreamableInput(new NullInputStream(0, true, true), "<null>", null, null, false);
    }
    
    static public StreamableInput standardInput() {
        return new StreamableInput(System.in, "<stdin>", null, null, false);
    }
    
    static public StreamableInput input(InputStream stream) {
        return input(stream, null);
    }
    
    static public StreamableInput input(InputStream stream, String name) {
        return input(stream, name, true);
    }
    
    static public StreamableInput input(InputStream stream, String name, boolean closeable) {
        Objects.requireNonNull(stream, "stream cannot be null");
        return new StreamableInput(stream, (name != null ? name : "<stream"), null, null, closeable);
    }
    
    static public StreamableInput input(File file) {
        Objects.requireNonNull(file, "file cannot be null");
        return input(file.toPath());
    }
    
    static public StreamableInput input(Path path) {
        Objects.requireNonNull(path, "path cannot be null");
        
        if (!Files.exists(path)) {
            throw new FileNotFoundException("Path " + path + " not found");
        }
        
        long size;
        try {
            size = Files.size(path);
        } catch (IOException e) {
            throw new BlazeException(e.getMessage(), e);
        }
        
        return new StreamableInput(new DeferredFileInputStream(path), path.getFileName().toString(), path, size, true);
    }
    
    static public StreamableOutput nullOutput() {
        return new StreamableOutput(new NullOutputStream(), "<null>", null, null, false, false);
    }
    
    static public StreamableOutput standardOutput() {
        return new StreamableOutput(System.out, "<stdout>", null, null, false, true);
    }
    
    static public StreamableOutput standardError() {
        return new StreamableOutput(System.err, "<stderr>", null, null, false, true);
    }
    
    static public StreamableOutput output(OutputStream stream) {
        return output(stream, null);
    }
    
    static public StreamableOutput output(OutputStream stream, String name) {
        return output(stream, name, true);
    }
    
    static public StreamableOutput output(OutputStream stream, String name, boolean closeable) {
        return output(stream, name, closeable, true);
    }
    
    static public StreamableOutput output(OutputStream stream, String name, boolean closeable, boolean flushable) {
        Objects.requireNonNull(stream, "stream cannot be null");
        return new StreamableOutput(stream, (name != null ? name : "<stream>"), null, null, closeable, flushable);
    }
    
    static public StreamableOutput output(File file) {
        Objects.requireNonNull(file, "file cannot be null");
        return output(file.toPath());
    }
    
    static public StreamableOutput output(Path path) {
        Objects.requireNonNull(path, "path cannot be null");
        return new StreamableOutput(new DeferredFileOutputStream(path), path.getFileName().toString(), path, null, true, true);
    }
    
    static public CaptureOutput captureOutput() {
        return new CaptureOutput();
    }
    
    static public StreamableOutput lineOutput(LineOutputStream.Processor processor) {
        return lineOutput(processor, null);
    }
    
    static public StreamableOutput lineOutput(LineOutputStream.Processor processor, Charset charset) {
        Objects.requireNonNull(processor, "processor cannot be null");
        return new StreamableOutput(new LineOutputStream(processor, charset), "<lines>", null, null, true, true);
    }
    
    static public void close(StreamableInput input) throws BlazeException {
        if (input != null) {
            try {
                input.close();
            } catch (IOException e) {
                throw new WrappedBlazeException(e);
            }
        }
    }
    
    static public void closeQuietly(StreamableInput input) {
        try {
            close(input);
        } catch (BlazeException e) {
            // do nothing
        }
    }
    
    static public void close(StreamableOutput output) throws BlazeException {
        if (output != null) {
            try {
                output.close();
            } catch (IOException e) {
                throw new WrappedBlazeException(e);
            }
        }
    }
    
    static public void closeQuietly(StreamableOutput output) {
        try {
            close(output);
        } catch (BlazeException e) {
            // do nothing
        }
    }

    static public void copy(StreamableInput input, StreamableOutput output) throws IOException {
        copy(input, output, 16384);
    }
    
    static public void copy(StreamableInput input, StreamableOutput output, int bufferSize) throws IOException {
        copy(input.stream(), output.stream(), bufferSize);
    }
    
    static private void copy(InputStream input, OutputStream output) throws IOException {
        copy(input, output, 16384);
    }
    
    static private void copy(InputStream input, OutputStream output, int bufferSize) throws IOException {
        int read;
        byte[] buffer = new byte[bufferSize];
        while ((read = input.read(buffer)) > -1) {
            output.write(buffer, 0, read);
        }
        output.flush();
    }
    
}

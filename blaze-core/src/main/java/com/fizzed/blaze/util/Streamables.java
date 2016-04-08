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
import static com.fizzed.blaze.internal.ConfigHelper.path;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Objects;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import org.apache.commons.io.input.NullInputStream;
import org.apache.commons.io.output.NullOutputStream;

public class Streamables {

    static public StreamableInput nullInput() {
        return new StreamableInput(new NullInputStream(0, true, true), "<null>", null, null);
    }
    
    static public StreamableInput standardInput() {
        InputStream is = new CloseGuardedInputStream(System.in);
        return new StreamableInput(is, "<stdin>", null, null);
    }
    
    static public StreamableInput input(InputStream stream) {
        return input(stream, null);
    }
    
    static public StreamableInput input(InputStream stream, String name) {
        Objects.requireNonNull(stream, "stream cannot be null");
        return new StreamableInput(stream, (name != null ? name : "<stream"), null, null);
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
        
        return new StreamableInput(new DeferredFileInputStream(path), path.getFileName().toString(), path, size);
    }
    
    static private Runnable asUncheckedRunnable(Closeable c) {
        return () -> {
            try {
                c.close();
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        };
    }
    
    static public Stream<String> lines(StreamableInput input) {
        BufferedReader br = new BufferedReader(new InputStreamReader(input.stream));
        try {
            return br.lines().onClose(asUncheckedRunnable(br));
        } catch (Error|RuntimeException e) {
            try {
                br.close();
            } catch (IOException ex) {
                try {
                    e.addSuppressed(ex);
                } catch (Throwable ignore) {}
            }
            throw e;
        }
    }
    
    static public Stream<String> matchedLines(StreamableInput input, String pattern) throws IOException {
        return matchedLines(input, Pattern.compile(pattern), null);
    }
    
    static public Stream<String> matchedLines(StreamableInput input, String pattern, Function<Matcher,String> mapper) throws IOException {
        return matchedLines(input, Pattern.compile(pattern), mapper);
    }
    
    static public Stream<String> matchedLines(StreamableInput input, Pattern pattern) throws IOException {
        return matchedLines(input, pattern, null);
    }
    
    static public Stream<String> matchedLines(StreamableInput input, Pattern pattern, Function<Matcher,String> mapper) throws IOException {
        return lines(input)
            .map((line) -> {
                Matcher matcher = pattern.matcher(line);
                if (matcher.matches()) {
                    if (mapper == null) {
                        return line;
                    } else {
                        return mapper.apply(matcher);
                    }
                } else {
                    return null;
                }
            })
            .filter((line) -> line != null);
    }
    
    static public StreamableOutput nullOutput() {
        return new StreamableOutput(new NullOutputStream(), "<null>", null, null);
    }
    
    static public StreamableOutput standardOutput() {
        OutputStream os = new CloseGuardedOutputStream(System.out);
        return new StreamableOutput(os, "<stdout>", null, null);
    }
    
    static public StreamableOutput standardError() {
        OutputStream os = new CloseGuardedOutputStream(System.err);
        return new StreamableOutput(os, "<stderr>", null, null);
    }
    
    static public StreamableOutput output(OutputStream stream) {
        return output(stream, null);
    }
    
    static public StreamableOutput output(OutputStream stream, String name) {
        Objects.requireNonNull(stream, "stream cannot be null");
        return new StreamableOutput(stream, (name != null ? name : "<stream>"), null, null);
    }
    
    static public StreamableOutput output(File file) {
        Objects.requireNonNull(file, "file cannot be null");
        return output(file.toPath());
    }
    
    static public StreamableOutput output(Path path) {
        Objects.requireNonNull(path, "path cannot be null");
        return new StreamableOutput(new DeferredFileOutputStream(path), path.getFileName().toString(), path, null);
    }
    
    static public CaptureOutput captureOutput() {
        return new CaptureOutput();
    }
    
    static public StreamableOutput lineOutput(LineOutputStream.Processor processor) {
        return lineOutput(processor, null);
    }
    
    static public StreamableOutput lineOutput(LineOutputStream.Processor processor, Charset charset) {
        Objects.requireNonNull(processor, "processor cannot be null");
        return new StreamableOutput(new LineOutputStream(processor, charset), "<lines>", null, null);
    }
    
    static public void close(Closeable stream) throws BlazeException {
        if (stream != null) {
            try {
                stream.close();
            } catch (IOException e) {
                throw new WrappedBlazeException(e);
            }
        }
    }
    
    static public void closeQuietly(Closeable stream) {
        try {
            close(stream);
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

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
import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;
import org.apache.commons.io.input.CloseShieldInputStream;
import org.apache.commons.io.input.NullInputStream;
import org.apache.commons.io.output.CloseShieldOutputStream;
import org.apache.commons.io.output.NullOutputStream;

/**
 *
 * @author joelauer
 */
public class NamedStream<T extends Closeable> implements Closeable {
    
    private final T stream;
    private final String name;
    private final Path path;
    private final Long size;
    private final boolean closeable;
    
    public NamedStream(T stream, String name, Path path, Long size, boolean closeable) {
        Objects.requireNonNull(stream, "stream cannot be null");
        Objects.requireNonNull(name, "name cannot be null");
        this.stream = stream;
        this.name = name;
        this.path = path;
        this.size = size;
        this.closeable = closeable;
    }

    public T stream() {
        return stream;
    }
    
    public String name() {
        return name;
    }
    
    public Path path() {
        return path;
    }
    
    public Long size() {
        return size;
    }
    
    public boolean closeable() {
        return closeable;
    }
    
    @Override
    public void close() throws IOException {
        if (closeable()) {
            stream.close();
        }
    }
    
    static public NamedStream<InputStream> STDIN
            = new NamedStream<>(System.in, "<stdin>", null, -1L, false);
    
    static public NamedStream<OutputStream> STDOUT
            = new NamedStream<>(System.out, "<stdout>", null, -1L, false);
    
    static public NamedStream<OutputStream> STDERR
            = new NamedStream<>(System.err, "<stderr>", null, -1L, false);
    
    static public NamedStream<InputStream> NULLIN
            = new NamedStream<>(new NullInputStream(0, true, true), "</dev/null>", null, -1L, false);
    
    static public NamedStream<OutputStream> NULLOUT
            = new NamedStream<>(new NullOutputStream(), "</dev/null>", null, -1L, false);
    
    static public <T extends Closeable> NamedStream of(T stream) {
        return of(stream, "<stream>", false);
    }
    
    static public <T extends Closeable> NamedStream of(T stream, String name, boolean closeable) {
        return new NamedStream(stream, name, null, null, closeable);
    }

    static public NamedStream<InputStream> input(InputStream stream) {
        Objects.requireNonNull(stream, "stream cannot be null");
        return of(stream);
    }
    
    static public NamedStream<InputStream> input(File file) {
        Objects.requireNonNull(file, "file cannot be null");
        return input(file.toPath());
    }
    
    static public NamedStream<InputStream> input(Path path) {
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
        
        return new NamedStream<>(new DeferredFileInputStream(path), path.getFileName().toString(), path, size, true);
    }
    
    static public NamedStream<OutputStream> output(OutputStream stream) {
        Objects.requireNonNull(stream, "stream cannot be null");
        return of(stream);
    }
    
    static public NamedStream<OutputStream> output(File file) {
        Objects.requireNonNull(file, "file cannot be null");
        return output(file.toPath());
    }
    
    static public NamedStream<OutputStream> output(Path path) {
        Objects.requireNonNull(path, "path cannot be null");
        return new NamedStream<>(new DeferredFileOutputStream(path), path.getFileName().toString(), path, null, true);
    }
    
}

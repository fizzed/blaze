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
import java.nio.file.Paths;
import java.util.Objects;

/**
 *
 * @author joelauer
 */
public class NamedStream<T extends Closeable> implements Closeable {
    
    private final Path path;
    private final String name;
    private final T stream;
    private final boolean closeable;

    public NamedStream(Path path, T stream) {
        this(path, stream, false);
    }
    
    public NamedStream(Path path, T stream, boolean closeable) {
        Objects.requireNonNull(path, "path cannot be null");
        Objects.requireNonNull(stream, "stream cannot be null");
        this.path = path;
        this.name = path.getFileName().toString();
        this.stream = stream;
        this.closeable = closeable;
    }

    public Path path() {
        return path;
    }
    
    public String name() {
        return name;
    }

    public T stream() {
        return stream;
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
    
    static public <T extends Closeable> NamedStream of(T stream) {
        return new NamedStream(Paths.get("<stream>"), stream);
    }
    
    static public NamedStream<OutputStream> output(File file) {
        Objects.requireNonNull(file, "file cannot be null");
        
        /** overwrite?
        if (!file.exists()) {
            throw new FileNotFoundException("File " + file + " not found");
        }
        */
        
        return new NamedStream<>(file.toPath(), new DeferredFileOutputStream(file), true);
    }
    
    static public NamedStream<OutputStream> output(Path path) {
        Objects.requireNonNull(path, "path cannot be null");

        /** overwrite?
        if (!file.exists()) {
            throw new FileNotFoundException("File " + file + " not found");
        }
        */
        
        return new NamedStream<>(path, new DeferredFileOutputStream(path), true);
    }
    
}

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
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;

/**
 *
 * @author joelauer
 */
public class SizedStream<T extends Closeable> extends NamedStream<T> {
    
    private final long size;

    public SizedStream(Path path, T stream, long size) {
        super(path, stream);
        this.size = size;
    }

    public SizedStream(Path path, T stream, long size, boolean closeable) {
        super(path, stream, closeable);
        this.size = size;
    }
    
    public long size() {
        return size;
    }
    
    static public long maybeSize(NamedStream stream) {
        if (stream instanceof SizedStream) {
            return ((SizedStream)stream).size();
        }
        return -1;
    }
    
    static public SizedStream<InputStream> input(File file) {
        Objects.requireNonNull(file, "file cannot be null");
        
        if (!file.exists()) {
            throw new FileNotFoundException("File " + file + " not found");
        }
        
        long size = file.lastModified();
        InputStream input = new DeferredFileInputStream(file);
        
        return new SizedStream(file.toPath(), input, size, true);
    }
    
    static public SizedStream<InputStream> input(Path path) {
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
        
        InputStream input = new DeferredFileInputStream(path);
        
        return new SizedStream(path, input, size, true);
    }
    
}

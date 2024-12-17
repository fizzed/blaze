package com.fizzed.blaze.util;

import java.io.Closeable;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Objects;

public abstract class Streamable<T extends Closeable> implements Closeable {
    
    protected final T stream;
    protected final String name;
    protected final Path path;
    protected final Long size;
    
    public Streamable(T stream, String name, Path path, Long size) {
        Objects.requireNonNull(stream, "stream cannot be null");
        Objects.requireNonNull(name, "name cannot be null");
        this.stream = stream;
        this.name = name;
        this.path = path;
        this.size = size;
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
    
    @Override
    public void close() throws IOException {
        stream.close();
    }
    
}

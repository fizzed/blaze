package com.fizzed.blaze.internal;

import java.nio.file.Path;

public class ConfigPaths {

    private final Path primaryFile;
    private final Path localFile;

    public ConfigPaths(Path primaryFile, Path localFile) {
        this.primaryFile = primaryFile;
        this.localFile = localFile;
    }

    public Path getPrimaryFile() {
        return primaryFile;
    }

    public Path getLocalFile() {
        return localFile;
    }

}

package com.fizzed.blaze.vfs;

import java.nio.file.Path;

public class LocalVirtualVolume implements VirtualVolume {

    private final Path path;

    public LocalVirtualVolume(Path path) {
        this.path = path;
    }

    @Override
    public String getPath() {
        return this.path.toString();
    }

    @Override
    public VirtualFileSystem openFileSystem() {
        return LocalVirtualFileSystem.open();
    }

    @Override
    public String toString() {
        return this.path.toString();
    }

    static public LocalVirtualVolume localVolume(Path path) {
        return new LocalVirtualVolume(path);
    }

}
package com.fizzed.blaze.vfs;

public interface VirtualVolume {

    String getPath();

    VirtualFileSystem openFileSystem();

}
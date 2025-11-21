package com.fizzed.blaze.vfs;

import com.fizzed.blaze.util.StreamableInput;

import java.io.IOException;
import java.util.List;

public interface VirtualFileSystem {

    boolean isRemote();

    VirtualPath pwd() throws IOException;

    VirtualPath stat(VirtualPath path) throws IOException;

    List<VirtualPath> ls(VirtualPath path) throws IOException;

    void mkdir(String path) throws IOException;

    default void mkdir(VirtualPath path) throws IOException {
        this.mkdir(path.toFullPath());
    }

    void rm(String path) throws IOException;

    default void rm(VirtualPath path) throws IOException {
        this.rm(path.toFullPath());
    }

    void rmdir(String path) throws IOException;

    default void rmdir(VirtualPath path) throws IOException {
        this.rmdir(path.toFullPath());
    }

    StreamableInput readFile(VirtualPath path) throws IOException;

    void writeFile(StreamableInput input, VirtualPath path) throws IOException;

    void cksums(List<VirtualPath> paths) throws IOException;

}
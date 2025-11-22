package com.fizzed.blaze.vfs;

import com.fizzed.blaze.jsync.Checksum;
import com.fizzed.blaze.util.StreamableInput;

import java.io.IOException;
import java.util.List;

public interface VirtualFileSystem {

    String getName();

    boolean isRemote();

    boolean isCaseSensitive();

    default boolean areFileNamesEqual(String name1, String name2) {
        if (this.isCaseSensitive()) {
            return name1.equals(name2);
        } else {
            return name1.equalsIgnoreCase(name2);
        }
    }

    VirtualPath pwd();

    VirtualPath stat(VirtualPath path) throws IOException;

    List<VirtualPath> ls(VirtualPath path) throws IOException;

    void mkdir(VirtualPath path) throws IOException;

    void rm(VirtualPath path) throws IOException;

    void rmdir(VirtualPath path) throws IOException;

    StreamableInput readFile(VirtualPath path, boolean progress) throws IOException;

    void writeFile(StreamableInput input, VirtualPath path, boolean progress) throws IOException;

    boolean isSupported(Checksum checksum) throws IOException;

    default void checksums(Checksum checksum, List<VirtualPath> paths) throws IOException {
        switch (checksum) {
            case CK:
                this.cksums(paths);
                break;
            case MD5:
                this.md5sums(paths);
                break;
            case SHA1:
                this.sha1sums(paths);
                break;
        }
    }

    void cksums(List<VirtualPath> paths) throws IOException;

    void md5sums(List<VirtualPath> paths) throws IOException;

    void sha1sums(List<VirtualPath> paths) throws IOException;

}
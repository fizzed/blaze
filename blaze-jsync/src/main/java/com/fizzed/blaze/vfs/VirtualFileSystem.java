package com.fizzed.blaze.vfs;

import com.fizzed.blaze.jsync.Checksum;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.NoSuchFileException;
import java.util.List;

public interface VirtualFileSystem extends AutoCloseable {

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

    /**
     * Checks if the specified virtual path exists in the virtual file system.
     * If the path exists, it returns the enriched {@link VirtualPath} object with metadata.
     * If the path does not exist, it returns null.
     *
     * @param path the virtual path to check for existence.
     * @return a {@link VirtualPath} object if the path exists, or null if the path does not exist.
     * @throws IOException if an I/O error occurs while attempting to check the path.
     */
    default VirtualPath exists(VirtualPath path) throws IOException {
        try {
            return this.stat(path);
        } catch (NoSuchFileException e) {
            return null;
        }
    }

    /**
     * Retrieves the metadata (statistics) for the specified virtual path. The metadata includes information such as
     * whether the path is a directory, size, modified time, and optionally checksum.
     *
     * @param path the virtual path whose metadata is to be retrieved.
     * @return a {@link VirtualPath} object representing the specified path, enriched with its metadata.
     * @throws IOException if an I/O error occurs while retrieving metadata
     * @throws java.nio.file.NoSuchFileException if the path does not exist
     */
    VirtualPath stat(VirtualPath path) throws IOException;

    void updateStat(VirtualPath path, VirtualFileStat stats) throws IOException;

    List<VirtualPath> ls(VirtualPath path) throws IOException;

    void mkdir(VirtualPath path) throws IOException;

    void rm(VirtualPath path) throws IOException;

    void rmdir(VirtualPath path) throws IOException;

    InputStream readFile(VirtualPath path) throws IOException;

    void writeFile(InputStream input, VirtualPath path) throws IOException;

    OutputStream writeStream(VirtualPath path) throws IOException;

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
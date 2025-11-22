package com.fizzed.blaze.vfs;

import com.fizzed.blaze.jsync.Checksum;
import com.fizzed.blaze.util.StreamableInput;
import com.fizzed.blaze.util.Streamables;
import com.fizzed.blaze.vfs.util.Checksums;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Stream;

public class LocalVirtualFileSystem extends AbstractVirtualFileSystem {
    static private final Logger log = LoggerFactory.getLogger(LocalVirtualFileSystem.class);

    public LocalVirtualFileSystem(VirtualPath pwd, boolean caseSensitive) {
        super("<local>", pwd, caseSensitive);
    }

    static public LocalVirtualFileSystem open() {
        // current working directory is our "pwd"
        final Path currentWorkingDir = Paths.get(".").toAbsolutePath().normalize();

        log.debug("Local pwd: {}", currentWorkingDir);

        final VirtualPath pwd = VirtualPath.parse(currentWorkingDir.toString(), true);

        // everything is case-sensitive except windows
        final boolean caseSensitive = !System.getProperty("os.name").toLowerCase().contains("windows");

        return new LocalVirtualFileSystem(pwd, caseSensitive);
    }

    private VirtualPath toVirtualPathWithStats(VirtualPath path) throws IOException {
        Path p = Paths.get(path.toString());
        boolean isDirectory = Files.isDirectory(p);
        VirtualStats stats = null;
        if (!isDirectory) {
            // create stats
            final long size = Files.size(p);
            final long modifiedTime = Files.getLastModifiedTime(p).toMillis();
            stats = new VirtualStats(size, modifiedTime);
        }
        return new VirtualPath(path.getParentPath(), path.getName(), isDirectory, stats);
    }

    @Override
    public boolean isRemote() {
        return false;
    }

    @Override
    public VirtualPath stat(VirtualPath path) throws IOException {
        return this.toVirtualPathWithStats(path);
    }

    @Override
    public List<VirtualPath> ls(VirtualPath path) throws IOException {
        final Path p = Paths.get(path.toString());
        if (Files.isDirectory(p)) {
            List<VirtualPath> childPaths = new ArrayList<>();
            try (Stream<Path> files = Files.list(p)) {
                for (Iterator<Path> it = files.iterator(); it.hasNext(); ) {
                    Path file = it.next();
                    // TDOO: should we skip handling symlinks??
                    if (Files.isSymbolicLink(file)) {
                        log.warn("Skipping symlink {} (unsupported at this time)", file);
                        continue;
                    }

                    // dir true/false doesn't matter, stats call next will correct it
                    VirtualPath childPathWithoutStats = path.resolve(file.getFileName().toString(), false);
                    VirtualPath childPath = this.toVirtualPathWithStats(childPathWithoutStats);
                    childPaths.add(childPath);
                }
            }
            return childPaths;
        } else {
            throw new IOException("Not a directory: " + path);
        }
    }

    @Override
    public void mkdir(String path) throws IOException {
        final Path _path = Paths.get(path);
        Files.createDirectory(_path);
    }

    @Override
    public void rm(String path) throws IOException {
        final Path _path = Paths.get(path);
        Files.delete(_path);
    }

    @Override
    public void rmdir(String path) throws IOException {
        // TODO: this needs to support recursive
        final Path _path = Paths.get(path);
        Files.delete(_path);
    }

    @Override
    public StreamableInput readFile(VirtualPath path, boolean progress) throws IOException {
        return Streamables.input(Paths.get(path.toString()));
    }

    @Override
    public void writeFile(StreamableInput input, VirtualPath path, boolean progress) throws IOException {
        Files.copy(input.stream(), Paths.get(path.toString()));
    }

    @Override
    public boolean isSupported(Checksum checksum) throws IOException {
        // all are supported!
        return true;
    }

    @Override
    public void cksums(List<VirtualPath> paths) throws IOException {
        for (VirtualPath path : paths) {
            try (StreamableInput input = this.readFile(path, false)) {
                long cksum = Checksums.cksum(input.stream());
                path.getStats().setCksum(cksum);
            }
        }
    }

    @Override
    public void md5sums(List<VirtualPath> paths) throws IOException {
        this.hashFiles("MD5", paths);
    }

    @Override
    public void sha1sums(List<VirtualPath> paths) throws IOException {
        this.hashFiles("SHA1", paths);
    }

    protected void hashFiles(String algorithm, List<VirtualPath> paths) throws IOException {
        for (VirtualPath path : paths) {
            try (StreamableInput input = this.readFile(path, false)) {
                String digest = Checksums.hash(algorithm, input.stream());
                if ("MD5".equals(algorithm)) {
                    path.getStats().setMd5(digest);
                } else if ("SHA1".equals(algorithm)) {
                    path.getStats().setSha1(digest);
                }
            }
        }
    }

}

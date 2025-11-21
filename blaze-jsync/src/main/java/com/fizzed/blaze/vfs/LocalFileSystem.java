package com.fizzed.blaze.vfs;

import com.fizzed.blaze.util.StreamableInput;
import com.fizzed.blaze.util.Streamables;
import com.fizzed.blaze.vfs.util.Cksums;
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

public class LocalFileSystem implements VirtualFileSystem {
    static private final Logger log = LoggerFactory.getLogger(LocalFileSystem.class);

    private final VirtualPath pwd;

    public LocalFileSystem(VirtualPath pwd) {
        this.pwd = pwd;
    }

    static public LocalFileSystem open() {
        // current working directory is our "pwd"
        final Path currentWorkingDir = Paths.get(".").toAbsolutePath().normalize();

        log.debug("Local pwd: {}", currentWorkingDir);

        final VirtualPath pwd = VirtualPath.parse(currentWorkingDir.toString(), true);

        return new LocalFileSystem(pwd);
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
    public VirtualPath pwd() throws IOException {
        return this.pwd;
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
    public StreamableInput readFile(VirtualPath path) throws IOException {
        return Streamables.input(Paths.get(path.toString()));
    }

    @Override
    public void writeFile(StreamableInput input, VirtualPath path) throws IOException {
        Files.copy(input.stream(), Paths.get(path.toString()));
    }

    @Override
    public void cksums(List<VirtualPath> paths) throws IOException {
        for (VirtualPath path : paths) {
            try (StreamableInput input = this.readFile(path)) {
                long cksum = Cksums.cksum(input.stream());
                path.getStats().setCksum(cksum);
            }
        }
    }

}

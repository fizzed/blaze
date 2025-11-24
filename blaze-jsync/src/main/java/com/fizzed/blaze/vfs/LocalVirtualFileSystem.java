package com.fizzed.blaze.vfs;

import com.fizzed.blaze.jsync.Checksum;
import com.fizzed.blaze.vfs.util.Checksums;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributeView;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Stream;

public class LocalVirtualFileSystem extends AbstractVirtualFileSystem {
    static private final Logger log = LoggerFactory.getLogger(LocalVirtualFileSystem.class);

    public LocalVirtualFileSystem(String name, VirtualPath pwd, boolean caseSensitive) {
        super(name, pwd, caseSensitive);
    }

    static public LocalVirtualFileSystem open() {
        return open(Paths.get("."));
    }

    static public LocalVirtualFileSystem open(Path workingDir) {
        final String name = "<local>";

        log.info("Opening filesystem {}...", name);

        // current working directory is our "pwd"
        final Path currentWorkingDir = workingDir.toAbsolutePath().normalize();

        final VirtualPath pwd = VirtualPath.parse(currentWorkingDir.toString(), true);

        log.debug("Detected filesystem {} has pwd {}", name, pwd);

        // everything is case-sensitive except windows
        final boolean caseSensitive = !System.getProperty("os.name").toLowerCase().contains("windows");

        log.debug("Detected filesystem {} is case-sensitive={}", name, caseSensitive);

        return new LocalVirtualFileSystem(name, pwd, caseSensitive);
    }

    @Override
    public void close() throws Exception {
        // nothing to do
    }

    protected Path toNativePath(VirtualPath path) {
        return Paths.get(path.toString());
    }

    protected VirtualPath toVirtualPathWithStat(VirtualPath path) throws IOException {
        final Path nativePath = this.toNativePath(path);

        // Fetch all attributes in ONE operation (and don't follow symlinks, we need to know the type)
        final BasicFileAttributes attrs = Files.readAttributes(nativePath, BasicFileAttributes.class, LinkOption.NOFOLLOW_LINKS);
        // TODO: if we're on posix, we can also do this
        // fetches size, times, PLUS owner, group, and permissions
        // PosixFileAttributes attrs = Files.readAttributes(path, PosixFileAttributes.class);
        final long size = attrs.size();
        final long modifiedTime = attrs.lastModifiedTime().toMillis();
        final long accessedTime = attrs.lastAccessTime().toMillis();

        // map file type
        final VirtualFileType type;
        if (attrs.isDirectory()) {
            type = VirtualFileType.DIR;
        } else if (attrs.isRegularFile()) {
            type = VirtualFileType.FILE;
        } else if (attrs.isSymbolicLink()) {
            type = VirtualFileType.SYMLINK;
        } else if (attrs.isOther()) {
            type = VirtualFileType.OTHER;
        } else {
            // should be all the types, but just in case
            log.warn("Unknown file type mapping for path: {} (defaulting to other)", path);
            type = VirtualFileType.OTHER;
        }

        final VirtualFileStat stat = new VirtualFileStat(type, size, modifiedTime, accessedTime);

        return new VirtualPath(path.getParentPath(), path.getName(), type == VirtualFileType.DIR, stat);
    }

    @Override
    public boolean isRemote() {
        return false;
    }

    @Override
    public VirtualPath stat(VirtualPath path) throws IOException {
        return this.toVirtualPathWithStat(path);
    }

    @Override
    public void updateStat(VirtualPath path, VirtualFileStat stat) throws IOException {
        final Path nativePath = this.toNativePath(path);

        //  Get the "View" (This is a lightweight handle to the attributes)
        BasicFileAttributeView view = Files.getFileAttributeView(nativePath, BasicFileAttributeView.class);

        // 2. Prepare the times
        FileTime newModifiedTime = FileTime.fromMillis(stat.getModifiedTime());
        FileTime newAccessedTime = FileTime.fromMillis(stat.getAccessedTime());

        // 3. Update all three in ONE operation
        // Signature: setTimes(lastModified, lastAccess, createTime)
        // Pass 'null' if you want to leave a specific timestamp unchanged.
        view.setTimes(newModifiedTime, newAccessedTime, null);
    }

    @Override
    public List<VirtualPath> ls(VirtualPath path) throws IOException {
        final Path nativePath = this.toNativePath(path);
        if (Files.isDirectory(nativePath)) {
            // TODO: apparently walkFileTree is way faster on windows as it includes BasicFileAttributes, negating the need for a 2nd kernel system call
            List<VirtualPath> childPaths = new ArrayList<>();
            try (Stream<Path> files = Files.list(nativePath)) {
                for (Iterator<Path> it = files.iterator(); it.hasNext(); ) {
                    Path nativeChildPath = it.next();

                    // dir true/false doesn't matter, stats call next will correct it
                    VirtualPath childPathWithoutStats = path.resolve(nativeChildPath.getFileName().toString(), false);

                    VirtualPath childPath = this.toVirtualPathWithStat(childPathWithoutStats);
                    childPaths.add(childPath);
                }
            }
            return childPaths;
        } else {
            throw new IOException("Not a directory: " + path);
        }
    }

    @Override
    public void mkdir(VirtualPath path) throws IOException {
        final Path nativePath = this.toNativePath(path);
        // to mirror what sftp provides, this should NOT make parents automatically
        Files.createDirectory(nativePath);
    }

    @Override
    public void rm(VirtualPath path) throws IOException {
        final Path nativePath = this.toNativePath(path);
        Files.delete(nativePath);
    }

    @Override
    public void rmdir(VirtualPath path) throws IOException {
        final Path nativePath = this.toNativePath(path);
        // to mirror what sftp provides, this should NOT make parents automatically
        Files.delete(nativePath);
    }

    @Override
    public InputStream readFile(VirtualPath path) throws IOException {
        final Path nativePath = this.toNativePath(path);
        return Files.newInputStream(nativePath);
    }

    @Override
    public void writeFile(InputStream input, VirtualPath path) throws IOException {
        final Path nativePath = this.toNativePath(path);
        // its important we allow replacing existing files
        Files.copy(input, nativePath, StandardCopyOption.REPLACE_EXISTING);
    }

    @Override
    public OutputStream writeStream(VirtualPath path) throws IOException {
        final Path nativePath = this.toNativePath(path);
        // it's important we allow replacing existing files
        return Files.newOutputStream(nativePath, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
    }

    @Override
    public boolean isSupported(Checksum checksum) throws IOException {
        // all are supported!
        return true;
    }

    @Override
    public void cksums(List<VirtualPath> paths) throws IOException {
        for (VirtualPath path : paths) {
            try (InputStream input = this.readFile(path)) {
                long cksum = Checksums.cksum(input);
                path.getStat().setCksum(cksum);
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
            try (InputStream input = this.readFile(path)) {
                String digest = Checksums.hash(algorithm, input);
                if ("MD5".equals(algorithm)) {
                    path.getStat().setMd5(digest);
                } else if ("SHA1".equals(algorithm)) {
                    path.getStat().setSha1(digest);
                }
            }
        }
    }

}

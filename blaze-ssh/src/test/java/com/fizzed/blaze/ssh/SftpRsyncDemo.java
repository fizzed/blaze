/*
 * Copyright 2015 Fizzed, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.fizzed.blaze.ssh;

import com.fizzed.blaze.logging.LogLevel;
import com.fizzed.blaze.logging.LoggerConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Stream;

import static com.fizzed.blaze.SecureShells.sshConnect;
import static com.fizzed.blaze.SecureShells.sshSftp;
import static java.util.Optional.ofNullable;

public class SftpRsyncDemo {
    static private final Logger log = LoggerFactory.getLogger(SftpRsyncDemo.class);

    static public void main(String[] args) throws Exception {
        LoggerConfig.setDefaultLogLevel(LogLevel.INFO);

        final Path sourceDir = Paths.get("/home/jjlauer/test-sync");
        final String targetDir = "test-sync";
        final boolean delete = true;


        final VirtualFileSystem sourceFS = new LocalFileSystem();

        final SshSession sshSession = sshConnect("ssh://bmh-build-x64-ubuntu24-1").run();
        final SshSftpSession sftp = sshSftp(sshSession).run();
        final VirtualFileSystem targetFS = new SftpFileSystem(sshSession, sftp);

        // source MUST exist
        final VirtualPath sourcePath = sourceFS.stat(sourceDir.toString());

        // target MAY or MAY NOT exist
        VirtualPath targetPath;
        try {
            targetPath = targetFS.stat(targetDir);
        } catch (FileNotFoundException e) {
            log.info("Target dir not found, creating {}", targetDir);
            targetFS.mkdir(targetDir);
            targetPath = targetFS.stat(targetDir);
        }


        log.info("Syncing directory {} -> {} (delete={})", sourcePath, targetPath, delete);

        syncDirectory(sourceFS, sourcePath, targetFS, targetPath, delete);
    }

    static public void syncDirectory(VirtualFileSystem sourceFS, VirtualPath sourcePath, VirtualFileSystem targetFS, VirtualPath targetPath, boolean delete) throws IOException {
        // we need a list of files in both directories, so we can see what to add/delete
        final List<VirtualPath> sourceChildPaths = sourceFS.ls(sourcePath);
        final List<VirtualPath> targetChildPaths = targetFS.ls(targetPath);

        sortPaths(sourceChildPaths);
        sortPaths(targetChildPaths);

        // calculate paths new / changed / same
        for (VirtualPath sourceChildPath : sourceChildPaths) {
            // find a matching target path entirely by name
            VirtualPath targetChildPath = targetChildPaths.stream()
                .filter(p -> p.getName().equals(sourceChildPath.getName()))
                .findFirst()
                .orElse(null);

            if (targetChildPath == null) {
                if (sourceChildPath.isDirectory()) {
                    targetChildPath = targetPath.resolve(sourceChildPath.getName(), true, null);
                    targetFS.mkdir(targetChildPath);
                    log.info("Created dir: {}", targetChildPath);
                } else {
                    targetChildPath = targetPath.resolve(sourceChildPath.getName(), false, null);
                    syncFile(sourceFS, sourceChildPath, targetFS, targetChildPath);
                }
            } else {
                // target path exists, we need to check if it needs to be updated
                // is there a file/dir mismatch?
                if (sourceChildPath.isDirectory() && !targetChildPath.isDirectory()) {
                    log.info("Source is a directory, target is a file! (target {})", targetChildPath);
                    // TODO: delete the target file and re-crate it as a directory
                    log.error("TODO: delete the target file, then create it as a directory");
                } else if (!sourceChildPath.isDirectory() && targetChildPath.isDirectory()) {
                    log.info("Source is a file, target is a directory! (target {})", targetChildPath);
                    // TODO: delete the target file and re-crate it as a directory
                    log.error("TODO: delete the target dir, and transfer the file");
                } else if (sourceChildPath.isDirectory() && targetChildPath.isDirectory()) {
                    // both are directories, nothing for us to do (will sync them later in this method)
                } else {
                    // both are files, do they need synced?
                    // assume we need to update it
                    syncFile(sourceFS, sourceChildPath, targetFS, targetChildPath);
                }
            }

            if (sourceChildPath.isDirectory()) {
                syncDirectory(sourceFS, sourceChildPath, targetFS, targetChildPath, delete);
            }
        }

        if (delete) {
            // calculate paths deleted
            for (VirtualPath targetChildPath : targetChildPaths) {
                // find a matching source path entirely by name
                final VirtualPath sourceChildPath = sourceChildPaths.stream()
                    .filter(p -> p.getName().equals(targetChildPath.getName()))
                    .findFirst()
                    .orElse(null);

                if (sourceChildPath == null) {
                    if (targetChildPath.isDirectory()) {
                        deleteDirectory(targetFS, targetChildPath);
                    } else {
                        targetFS.rm(targetChildPath);
                        log.info("Deleted file: {}", targetChildPath);
                    }
                }
            }
        }
    }

    static public void syncFile(VirtualFileSystem sourceFS, VirtualPath sourceFile, VirtualFileSystem targetFS, VirtualPath targetFile) throws IOException {
        if (sourceFile.getStats() != null && targetFile.getStats() != null) {
            if (sourceFile.getStats().getSize() == targetFile.getStats().getSize()
                    && sourceFile.getStats().getModifiedTime() < targetFile.getStats().getModifiedTime()) {     // this is kinda flaky...
                log.debug("Skipping file {} (sizes match, source modified time < target modified time)", targetFile);
                return;
            }
        }

        if (targetFile.getStats() == null) {
            log.info("Create file: {}", targetFile);
        } else {
            log.info("Update file: {}", targetFile);
        }

        // transfer the file
        try (InputStream input = sourceFS.readFile(sourceFile)) {
            targetFS.writeFile(input, targetFile);
        }
    }

    static public void deleteDirectory(VirtualFileSystem fs, VirtualPath path) throws IOException {
        // we need a list of files in both directories, since we'll need to recurse thru dirs
        final List<VirtualPath> childPaths = fs.ls(path);
        sortPaths(childPaths);

        for (VirtualPath childPath : childPaths) {
            if (childPath.isDirectory()) {
                deleteDirectory(fs, childPath);     // do not log this, that will happen in the below statement via recursion
            } else {
                fs.rm(childPath);
                log.info("Deleted file: {}", childPath);
            }
        }

        // finally we can delete the directory
        fs.rmdir(path);
        log.info("Deleted dir: {}", path);
    }

    static public void sortPaths(List<VirtualPath> paths) {
        // sort, where directories come before files
        paths.sort((p1, p2) -> {
            if (p1.isDirectory() && !p2.isDirectory()) {
                return -1;
            } else if (!p1.isDirectory() && p2.isDirectory()) {
                return 1;
            } else {
                return p1.getName().compareTo(p2.getName());
            }
        });
    }

    //
     // helpers

    //

    // The standard POSIX CRC polynomial (0x04C11DB7)
    private static final int[] CRC_TABLE = new int[256];
    static {
        // Generate the table at class loading time
        for (int i = 0; i < 256; ++i) {
            int entry = i << 24;
            for (int j = 0; j < 8; ++j) {
                if ((entry & 0x80000000) != 0) {
                    entry = (entry << 1) ^ 0x04C11DB7;
                } else {
                    entry = entry << 1;
                }
            }
            CRC_TABLE[i] = entry;
        }
    }

    /**
     * Calculates the POSIX standard 'cksum' (CRC32 + Length)
     *
     * @param data The input stream of data to checksum
     * @return The long value representing the unsigned 32-bit checksum
     */
    static public long cksum(InputStream data) throws IOException {
        int crc = 0;
        long length = 0;
        int b;

        // 1. Process all file bytes
        while ((b = data.read()) != -1) {
            crc = (crc << 8) ^ CRC_TABLE[((crc >>> 24) ^ b) & 0xFF];
            length++;
        }

        // 2. POSIX requirement: Append the length of the file to the stream.
        // We append the length in Little Endian byte order, one byte at a time,
        // stripping high-order null bytes, but ensuring at least one byte is written.
        long tempLength = length;
        do {
            int byteVal = (int) (tempLength & 0xFF);
            crc = (crc << 8) ^ CRC_TABLE[((crc >>> 24) ^ byteVal) & 0xFF];
            tempLength >>>= 8;
        } while (tempLength > 0);

        // 3. Final bit inversion
        return (~crc) & 0xFFFFFFFFL; // Mask to return as unsigned 32-bit integer
    }

    public interface VirtualFileSystem {

        VirtualPath stat(String path) throws IOException;

        default VirtualPath stat(VirtualPath path) throws IOException {
            return this.stat(path.toFullPath());
        }

        List<VirtualPath> ls(String path) throws IOException;

        default List<VirtualPath> ls(VirtualPath path) throws IOException {
            return this.ls(path.toFullPath());
        }

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

        InputStream readFile(String path) throws IOException;

        default InputStream readFile(VirtualPath path) throws IOException {
            return this.readFile(path.toFullPath());
        }

        void writeFile(InputStream is, String path) throws IOException;

        default void writeFile(InputStream is, VirtualPath path) throws IOException {
            this.writeFile(is, path.toFullPath());
        }

        void cksums(List<VirtualPath> paths) throws IOException;

    }

    static public class LocalFileSystem implements VirtualFileSystem {

        private VirtualPath toVirtualPath(Path path) throws IOException {
            boolean isDirectory = Files.isDirectory(path);
            VirtualStats stats = null;
            if (!isDirectory) {
                // create stats
                final long size = Files.size(path);
                final long modifiedTime = Files.getLastModifiedTime(path).toMillis();
                stats = new VirtualStats(size, modifiedTime);
            }
            return new VirtualPath(path.getParent().toString(), path.getFileName().toString(), isDirectory, stats);
        }

        @Override
        public VirtualPath stat(String path) throws IOException {
            return this.toVirtualPath(Paths.get(path));
        }

        @Override
        public void mkdir(String path) throws IOException {
            final Path _path = Paths.get(path);
            Files.createDirectory(_path);
        }

        @Override
        public List<VirtualPath> ls(String path) throws IOException {
            final Path _path = Paths.get(path);
            if (Files.isDirectory(_path)) {
                List<VirtualPath> paths = new ArrayList<>();
                try (Stream<Path> files = Files.list(_path)) {
                    for (Iterator<Path> it = files.iterator(); it.hasNext(); ) {
                        Path file = it.next();
                        VirtualPath vp = this.toVirtualPath(file);
                        paths.add(vp);
                    }
                }
                return paths;
            } else {
                throw new IOException("Not a directory: " + path);
            }
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
        public InputStream readFile(String path) throws IOException {
            return Files.newInputStream(Paths.get(path));
        }

        @Override
        public void writeFile(InputStream is, String path) throws IOException {
            Files.copy(is, Paths.get(path));
        }

        @Override
        public void cksums(List<VirtualPath> paths) throws IOException {
            for (VirtualPath path : paths) {
                try (InputStream is = this.readFile(path)) {
                    long v = cksum(is);
                    path.getStats().setCksum(v);
                }
            }
        }

    }

    static public class SftpFileSystem implements VirtualFileSystem {
        private final SshSession ssh;
        private final SshSftpSession sftp;

        public SftpFileSystem(SshSession ssh, SshSftpSession sftp) {
            this.ssh = ssh;
            this.sftp = sftp;
        }

        private VirtualPath toVirtualPath(String path, String name, SshFileAttributes attributes) throws IOException {
            boolean isDirectory = attributes.isDirectory();
            VirtualStats stats = null;
            if (!isDirectory) {
                long size = attributes.size();
                long modifiedTime = attributes.lastModifiedTime().toMillis();
                stats = new VirtualStats(size, modifiedTime);
            }
            return new VirtualPath(path, name, isDirectory, stats);
        }

        @Override
        public VirtualPath stat(String path) throws IOException {
            try {
                final Path _path = Paths.get(path);
                final SshFileAttributes file = this.sftp.lstat(path);
                return this.toVirtualPath(ofNullable(_path.getParent()).map(v -> v.toString()).orElse("."), _path.getFileName().toString(), file);
            } catch (SshSftpNoSuchFileException e) {
                throw new FileNotFoundException();
            }
        }

        @Override
        public void mkdir(String path) throws IOException {
            this.sftp.mkdir(path);
        }

        @Override
        public List<VirtualPath> ls(String path) throws IOException {
            final List<SshFile> files = this.sftp.ls(path);
            final List<VirtualPath> paths = new ArrayList<>();
            for (SshFile file : files) {
                VirtualPath vp = this.toVirtualPath(path, file.fileName(), file.attributes());
                paths.add(vp);
            }
            return paths;
        }

        @Override
        public void rm(String path) throws IOException {
            final Path _path = Paths.get(path);
            this.sftp.rm(path);
        }

        @Override
        public void rmdir(String path) throws IOException {
            // TODO: this needs to support recursive
            this.sftp.rmdir(path);
        }

        @Override
        public InputStream readFile(String path) throws IOException {
            throw new UnsupportedOperationException("readFile");
        }

        @Override
        public void writeFile(InputStream is, String path) throws IOException {
            this.sftp.put()
                .source(is)
                .target(path)
                .run();
        }

        @Override
        public void cksums(List<VirtualPath> paths) throws IOException {
            // we need to be smart about how many files we request in bulk, as the command line can only be so long
            // we can leverage the "workingDir" so that the paths stay shorter, if we're simply in the same dir

            String workingDir = null;

            for (int i = 0; i < paths.size(); i ++) {
                final VirtualPath path = paths.get(i);
                if (workingDir == null) {
                    workingDir
                }
            }





            // we will leverage the "cksum" binary that's more than likely available on the other end
            ssh.newExec().command("cksum")
                .ar
                .run();


            for (VirtualPath path : paths) {
                try (InputStream is = this.readFile(path)) {
                    long v = cksum(is);
                    path.getStats().setCksum(v);
                }
            }
        }
    }

    static public class VirtualStats {

        final private long size;
        final private long modifiedTime;
        // there are values that can be populated later
        private long cksum;

        public VirtualStats(long size, long modifiedTime) {
            this.size = size;
            this.modifiedTime = modifiedTime;
        }

        public long getSize() {
            return size;
        }

        public long getModifiedTime() {
            return modifiedTime;
        }

        public long getCksum() {
            return cksum;
        }

        public VirtualStats setCksum(long cksum) {
            this.cksum = cksum;
            return this;
        }

    }

    static public class VirtualPath {

        private final String parentPath;
        private final String name;
        private final boolean directory;
        private final VirtualStats stats;

        public VirtualPath(String parentPath, String name, boolean directory, VirtualStats stats) {
            this.parentPath = parentPath;
            this.name = name;
            this.directory = directory;
            this.stats = stats;
        }

        public String toFullPath() {
            if (this.parentPath.isEmpty() || this.parentPath.equals(".")) {
                return this.name;
            }
            // otherwise, return the full name
            return this.parentPath + "/" + this.name;
        }

        public String getParentPath() {
            return this.parentPath;
        }

        public VirtualPath resolve(String name, boolean directory, VirtualStats stats) {
            return new VirtualPath(this.toFullPath(), name, directory, stats);
        }

        public String getName() {
            return this.name;
        }

        public boolean isDirectory() {
            return this.directory;
        }

        public VirtualStats getStats() {
            return this.stats;
        }

        @Override
        public String toString() {
            return this.toFullPath();
        }

    }

}
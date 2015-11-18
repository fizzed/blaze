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
package com.fizzed.blaze.ssh.impl;

import com.fizzed.blaze.ssh.SshException;
import com.fizzed.blaze.ssh.SshFile;
import com.fizzed.blaze.ssh.SshFileAttributes;
import com.fizzed.blaze.ssh.SshSession;
import com.fizzed.blaze.ssh.SshSftpGet;
import com.fizzed.blaze.ssh.SshSftpPut;
import com.fizzed.blaze.ssh.SshSftpSession;
import com.fizzed.blaze.util.Streamable;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.ChannelSftp.LsEntry;
import com.jcraft.jsch.SftpATTRS;
import com.jcraft.jsch.SftpException;
import com.jcraft.jsch.SftpProgressMonitor;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.FileTime;
import java.nio.file.attribute.PosixFilePermission;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author joelauer
 */
public class JschSftpSession implements SshSftpSession, SshSftpSupport {
    static private final Logger log = LoggerFactory.getLogger(JschSftpSession.class);

    private final SshSession session;
    private final ChannelSftp channel;
    private boolean closed;
    private Path workingDir;

    public JschSftpSession(SshSession session, ChannelSftp channel) {
        Objects.requireNonNull(session, "session cannot be null");
        Objects.requireNonNull(channel, "channel cannot be null");
        this.session = session;
        this.channel = channel;
        // somewhat unorthodox but we want the current working directory
        this.pwd();
    }
    
    @Override
    public SshSession session() {
        return this.session;
    }
    
    @Override
    public boolean closed() {
        return this.closed;
    }
    
    @Override
    public void close() throws IOException {
        if (!this.closed) {
             try {
                 this.channel.disconnect();
             } catch (Exception e) {
                 // not sure this matters
             }
             this.closed = true;
        }
    }
    
    // sub-actions?
    
    @Override
    public final Path pwd() {
        try {
            this.workingDir = Paths.get(this.channel.pwd());
            return this.workingDir;
        } catch (SftpException e) {
            throw new SshException(e.getMessage(), e);
        }
    }
    
    @Override
    public void cd(String path) {
        cd(Paths.get(path));
    }
    
    @Override
    public void cd(Path path) throws SshException {
        try {
            this.channel.cd(PathHelper.toString(path));
            this.workingDir = path;
        } catch (SftpException e) {
            // 2: no such file
            // 4: Can't change directory: /bin/ls
            throw new SshException(e.getMessage(), e);
        }
    }
    
    @Override
    public SshFileAttributes lstat(String path) {
        return lstat(Paths.get(path));
    }
    
    @Override
    public SshFileAttributes lstat(Path path) throws SshException {
        try {
            String p = PathHelper.toString(path);
            log.debug("lstat {}", p);
            
            SftpATTRS attrs = this.channel.lstat(p);
            
            return new JschFileAttributes(attrs);
        } catch (SftpException e) {
            // 2: no such file
            // 4: Can't change directory: /bin/ls
            throw new SshException(e.getMessage(), e);
        }
    }
    
    @Override
    public List<SshFile> ls(String path) {
        return ls(Paths.get(path));
    }
    
    @Override
    public List<SshFile> ls(Path path) throws SshException {
        try {
            @SuppressWarnings("UseOfObsoleteCollectionType")
            java.util.Vector<Object> fileObjects = this.channel.ls(PathHelper.toString(path));
            
            List<SshFile> files = new ArrayList<>();
            
            for (Object fileObject : fileObjects) {
                LsEntry entry = (LsEntry)fileObject;
                
                // seems like filtering these out is useful
                if (entry.getFilename().equals(".") || entry.getFilename().equals("..")) {
                    continue;
                }
                
                // workingDir + path + fileName
                Path entryPath = path.resolve(entry.getFilename()).normalize();
                
                files.add(new SshFile(entryPath, new JschFileAttributes(entry.getAttrs())));
            }
            
            return files;
        } catch (SftpException e) {
            // 2: no such file
            // 4: Can't change directory: /bin/ls
            throw new SshException(e.getMessage(), e);
        }
    }
    
    @Override
    public SshSftpGet get() throws SshException {
        return new SshSftpGet(this);
    }
    
    @Override
    public void get(Path source, Streamable<OutputStream> target) throws SshException {
        try {
            // TODO: how can we log w/o requring it be a line???
            if (log.isInfoEnabled()) {
                System.out.print("[INFO] Downloading " + source + " -> " + target.path());
            }
            
            try {
                this.channel.get(source.toString(), target.stream(), new DefaultProgressMonitor(), ChannelSftp.OVERWRITE, 0);
            } finally {
                IOUtils.closeQuietly(target);
            }  
        } catch (SftpException e) {
            // 2: no such file
            // 4: Can't change directory: /bin/ls
            throw new SshException(e.getMessage(), e);
        }
    }
    
    @Override
    public SshSftpPut put() throws SshException {
        return new SshSftpPut(this);
    }
    
    @Override
    public void put(Streamable<InputStream> source, Path target) throws SshException {
        try {
            // TODO: how can we log w/o requring it be a complete line?
            if (log.isInfoEnabled()) {
                if (source.size() != null) {
                    System.out.print("[INFO] Uploading " + source.path() + " -> " + target + " (" + source.size() + " bytes)");
                } else {
                    System.out.print("[INFO] Uploading " + source.path() + " -> " + target);
                }
            }
                    
            OutputStream output
                = this.channel.put(target.toString(), new DefaultProgressMonitor(), ChannelSftp.OVERWRITE, 0);
            
            try {
                // copy streams input -> output
                IOUtils.copy(source.stream(), output);
            } finally {
                IOUtils.closeQuietly(output);
                IOUtils.closeQuietly(source);
            }
        } catch (SftpException | IOException e) {
            // 2: no such file
            // 4: Can't change directory: /bin/ls
            throw new SshException(e.getMessage(), e);
        }
    }
    
    @Override
    public void chgrp(String path, int gid) {
        chgrp(Paths.get(path), gid);
    }
    
    @Override
    public void chgrp(Path path, int gid) {
        try {
            this.channel.chgrp(gid, PathHelper.toString(path));
        } catch (SftpException e) {
            throw new SshException(e.getMessage(), e);
        }
    }
    
    @Override
    public void chown(String path, int uid) {
        chown(Paths.get(path), uid);
    }
    
    @Override
    public void chown(Path path, int uid) {
        try {
            this.channel.chown(uid, PathHelper.toString(path));
        } catch (SftpException e) {
            throw new SshException(e.getMessage(), e);
        }
    }
    
    @Override
    public void chown(String path, int uid, int gid) {
        chown(Paths.get(path), uid, gid);
    }
    
    @Override
    public void chown(Path path, int uid, int gid) {
        chown(path, uid);
        chgrp(path, gid);
    }
    
    @Override
    public void mkdir(String path) {
        mkdir(Paths.get(path));
    }
    
    @Override
    public void mkdir(Path path) {
        try {
            this.channel.mkdir(PathHelper.toString(path));
        } catch (SftpException e) {
            throw new SshException(e.getMessage(), e);
        }
    }
    
    @Override
    public void rm(String path) {
        rm(Paths.get(path));
    }
    
    @Override
    public void rm(Path path) {
        try {
            this.channel.rm(PathHelper.toString(path));
        } catch (SftpException e) {
            throw new SshException(e.getMessage(), e);
        }
    }
    
    @Override
    public void rmdir(String path) {
        rmdir(Paths.get(path));
    }
    
    @Override
    public void rmdir(Path path) {
        try {
            this.channel.rmdir(PathHelper.toString(path));
        } catch (SftpException e) {
            throw new SshException(e.getMessage(), e);
        }
    }
    
    @Override
    public void mv(String source, String target) {
        mv(Paths.get(source), Paths.get(target));
    }
    
    @Override
    public void mv(Path source, Path target) {
        try {
            this.channel.rename(source.toString(), target.toString());
        } catch (SftpException e) {
            throw new SshException(e.getMessage(), e);
        }
    }
    
    @Override
    public void symlink(String target, String link) {
        symlink(Paths.get(target), Paths.get(link));
    }    
    
    @Override
    public void symlink(Path target, Path link) {
        try {
            this.channel.symlink(target.toString(), link.toString());
        } catch (SftpException e) {
            throw new SshException(e.getMessage(), e);
        }
    }

    static public class DefaultProgressMonitor implements SftpProgressMonitor {
        @Override
        public void init(int op, String src, String dest, long max) {
            System.out.print(" ");
        }

        @Override
        public boolean count(long count) {
            System.out.print(".");
            return true;
        }

        @Override
        public void end() {
            System.out.println("!");
        }
    }
    
    
    static public class JschFileAttributes implements SshFileAttributes {
        
        private final SftpATTRS attrs;

        public JschFileAttributes(SftpATTRS attrs) {
            this.attrs = attrs;
        }

        @Override
        public FileTime lastModifiedTime() {
            return FileTime.fromMillis(attrs.getMTime()*1000L);
        }

        @Override
        public FileTime lastAccessTime() {
            return FileTime.fromMillis(attrs.getATime()*1000L);
        }

        @Override
        public FileTime creationTime() {
            // is this really not supported via sftp???
            //return FileTime.fromMillis(attrs.*1000L);
            return lastModifiedTime();
        }

        @Override
        public boolean isRegularFile() {
            return attrs.isReg();
        }

        @Override
        public boolean isDirectory() {
            return attrs.isDir();
        }

        @Override
        public boolean isSymbolicLink() {
            return attrs.isLink();
        }

        @Override
        public boolean isOther() {
            return attrs.isBlk() || attrs.isChr() || attrs.isFifo() || attrs.isSock();
        }

        @Override
        public long size() {
            return attrs.getSize();
        }

        @Override
        public Object fileKey() {
            // not available per specs
            return null;
        }

        @Override
        public int gid() {
            return attrs.getGId();
        }

        @Override
        public int uid() {
            return attrs.getUId();
        }

        @Override
        public Set<PosixFilePermission> permissions() {
            Set<PosixFilePermission> permissions = new HashSet<>();
            
            String s = attrs.getPermissionsString();
            
            // drwxrwxrwx
            
            if (s.charAt(1) == 'r') {
                permissions.add(PosixFilePermission.OWNER_READ);
            }
            
            if (s.charAt(2) == 'w') {
                permissions.add(PosixFilePermission.OWNER_WRITE);
            }
            
            if (s.charAt(3) == 'x' || s.charAt(3) == 's') {
                permissions.add(PosixFilePermission.OWNER_EXECUTE);
            }
            
            if (s.charAt(4) == 'r') {
                permissions.add(PosixFilePermission.GROUP_READ);
            }
            
            if (s.charAt(5) == 'w') {
                permissions.add(PosixFilePermission.GROUP_WRITE);
            }
            
            if (s.charAt(6) == 'x' || s.charAt(3) == 's') {
                permissions.add(PosixFilePermission.GROUP_EXECUTE);
            }
            
            if (s.charAt(7) == 'r') {
                permissions.add(PosixFilePermission.OTHERS_READ);
            }
            
            if (s.charAt(8) == 'w') {
                permissions.add(PosixFilePermission.OTHERS_WRITE);
            }
            
            if (s.charAt(9) == 'x' || s.charAt(3) == 's') {
                permissions.add(PosixFilePermission.OTHERS_EXECUTE);
            }
            
            return permissions;
        }
    }
    
}

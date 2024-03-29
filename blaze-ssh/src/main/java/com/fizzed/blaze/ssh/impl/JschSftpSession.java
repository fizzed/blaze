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
import com.fizzed.blaze.ssh.SshSftpException;
import com.fizzed.blaze.ssh.SshSftpGet;
import com.fizzed.blaze.ssh.SshSftpNoSuchFileException;
import com.fizzed.blaze.ssh.SshSftpPut;
import com.fizzed.blaze.ssh.SshSftpSession;
import com.fizzed.blaze.util.DurationFormatter;
import com.fizzed.blaze.util.HumanReadables;
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
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author joelauer
 */
public class JschSftpSession extends SshSftpSession implements SshSftpSupport {
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
            throw convertSftpException(e);
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
            throw convertSftpException(e);
        }
    }
    
    @Override
    public SshFileAttributes lstat(String path) throws SshSftpException {
        return lstat(Paths.get(path));
    }
    
    @Override
    public SshFileAttributes lstat(Path path) throws SshSftpException {
        try {
            String p = PathHelper.toString(path);
            log.debug("lstat {}", p);
            
            SftpATTRS attrs = this.channel.lstat(p);
            
            return new JschFileAttributes(attrs);
        } catch (SftpException e) {
            throw convertSftpException(e);
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
            java.util.Vector<LsEntry> fileObjects = this.channel.ls(PathHelper.toString(path));
            
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
            throw convertSftpException(e);
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
                this.channel.get(source.toString(), target.stream(), new TermProgressMonitor(null), ChannelSftp.OVERWRITE, 0);
            } finally {
                IOUtils.closeQuietly(target);
            }  
        } catch (SftpException e) {
            throw convertSftpException(e);
        }
    }
    
    @Override
    public SshSftpPut put() throws SshException {
        return new SshSftpPut(this);
    }
    
    @Override
    public void put(Streamable<InputStream> source, String target) throws SshException {
        try {
            // target potentially 
            
            // TODO: how can we log w/o requring it be a complete line?
            if (log.isInfoEnabled()) {
                if (source.size() != null) {
                    System.out.print("[INFO] Uploading " + source.path() + " -> " + target + " (" + source.size() + " bytes)");
                } else {
                    System.out.print("[INFO] Uploading " + source.path() + " -> " + target);
                }
            }

            OutputStream output
                = this.channel.put(target, new TermProgressMonitor(source.size()), ChannelSftp.OVERWRITE, 0);
            
            try {
                // copy streams input -> output
                IOUtils.copy(source.stream(), output);
            } finally {
                IOUtils.closeQuietly(output);
                IOUtils.closeQuietly(source);
            }
        } catch (IOException e) {
            throw new SshException(e.getMessage(), e);
        } catch (SftpException e) {
            throw convertSftpException(e);
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
            throw convertSftpException(e);
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
            throw convertSftpException(e);
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
            throw convertSftpException(e);
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
            throw convertSftpException(e);
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
            throw convertSftpException(e);
        }
    }
    
    @Override
    public void mv(String source, String target) {
        mv(Paths.get(source), Paths.get(target));
    }
    
    @Override
    public void mv(Path source, Path target) {
        try {
            this.channel.rename(PathHelper.toString(source), PathHelper.toString(target));
        } catch (SftpException e) {
            throw convertSftpException(e);
        }
    }
    
    @Override
    public void symlink(String target, String link) {
        symlink(Paths.get(target), Paths.get(link));
    }    
    
    @Override
    public void symlink(Path target, Path link) {
        try {
            this.channel.symlink(PathHelper.toString(target), PathHelper.toString(link));
        } catch (SftpException e) {
            throw convertSftpException(e);
        }
    }
    
    @Override
    public Path readlink(String target) {
        return readlink(Paths.get(target));
    }    
    
    @Override
    public Path readlink(Path target) {
        try {
            return Paths.get(this.channel.readlink(PathHelper.toString(target)));
        } catch (SftpException e) {
            throw convertSftpException(e);
        }
    }
    
    @Override
    public Path realpath(String target) {
        return realpath(Paths.get(target));
    }    
    
    @Override
    public Path realpath(Path target) {
        try {
            return Paths.get(this.channel.realpath(PathHelper.toString(target)));
        } catch (SftpException e) {
            throw convertSftpException(e);
        }
    }
    
    private SshSftpException convertSftpException(SftpException e) {
        if (e.id == 2) {
            return new SshSftpNoSuchFileException(e.id, e.getMessage(), e);
        } else {
            return new SshSftpException(e.id, e.getMessage(), e);
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

    static public class TermProgressMonitor implements SftpProgressMonitor {
        
        private final long startTime;
        private long totalBytes;
        private long processedBytes;
        private int maxPrintLen;

        public TermProgressMonitor(Long totalBytes) {
            this.startTime = System.currentTimeMillis();
            this.totalBytes = totalBytes != null ? totalBytes : -1;
        }
        
        @Override
        public void init(int op, String src, String dest, long max) {
            if (this.totalBytes < 0) {
                this.totalBytes = max;
            }
            System.out.println("");
        }

        @Override
        public boolean count(long count) {
            this.processedBytes += count;
            
            if (this.totalBytes <= 0) {
                System.out.print(".");
            } else {
//                double progress = (double)this.processedBytes / (double)totalBytes;
                // 25%[=============>______________________________________] 25,000 100.0K/s
                //System.out.print("\r"+(progress*100d));
                this.printProgress(startTime, this.totalBytes, this.processedBytes);
            }
            
            return true;
        }

        @Override
        public void end() {
            System.out.println("");
        }
        
        private void printProgress(long startTime, long total, long current) {
            
            long elapsedMillis = (System.currentTimeMillis() - startTime);
            
            long eta = current == 0 ? 0
                : (total - current) * elapsedMillis / current;

            // bytes per second...
            final double bytesPerSec = (double)current / (elapsedMillis/1000);
            final double kilobytesPerSec = HumanReadables.kilobytes(bytesPerSec);
            
            final String speed = String.format("%.0f KiB/s", kilobytesPerSec);
            
//            int seconds = (int) (eta / 1000) % 60 ;
//            int minutes = (int) ((eta / (1000*60)) % 60);
//            int hours   = (int) ((eta / (1000*60*60)) % 24);
            
            final String etaHms = current == 0 ? "N/A" : DurationFormatter.format(eta);
            
            double totalKilobytes = HumanReadables.kilobytes(total);
            double currentKilobytes = HumanReadables.kilobytes(current);
            
//            String etaHms = current == 0 ? "N/A"
//                : String.format("%02d:%02d:%02d", hours, minutes, seconds);

            StringBuilder sb = new StringBuilder(140);
            int percent = (int) (current * 100 / total);
            sb
                .append('\r')
                .append(String.join("", Collections.nCopies(percent == 0 ? 2 : 2 - (int) (Math.log10(percent)), " ")))
                .append(String.format(" %d%% [", percent))
                .append(String.join("", Collections.nCopies(percent, "=")))
                .append('>')
                .append(String.join("", Collections.nCopies(100 - percent, " ")))
                .append(']')
                .append(String.join("", Collections.nCopies((int) (Math.log10(totalKilobytes)) - (int) (Math.log10(currentKilobytes)), " ")))
                .append(String.format(" %.0f/%.0f KiB, %s, %s ", currentKilobytes, totalKilobytes, etaHms, speed));

            if (sb.length() > maxPrintLen) {
                maxPrintLen = sb.length();
            }
            
            // pad spaces if needed
            for (int i = 0; i < (maxPrintLen - sb.length()); i++) {
                sb.append(" ");
            }
            
            System.out.print(sb);
//            System.out.println(new String(new char[70]).replace("\0", "\r\n"));
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

package com.fizzed.blaze.vfs;

import com.fizzed.blaze.ssh.*;
import com.fizzed.blaze.system.Exec;
import com.fizzed.blaze.util.StreamableInput;
import com.fizzed.blaze.vfs.util.Cksums;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.fizzed.blaze.SecureShells.sshSftp;

public class SftpFileSystem implements VirtualFileSystem {
    static private final Logger log = LoggerFactory.getLogger(SftpFileSystem.class);

    private final SshSession ssh;
    private final SshSftpSession sftp;
    private final VirtualPath pwd;
    private int maxCommandLength;

    protected SftpFileSystem(SshSession ssh, SshSftpSession sftp, VirtualPath pwd) {
        this.ssh = ssh;
        this.sftp = sftp;
        this.pwd = pwd;
        this.maxCommandLength = 8000;       // windows shell limit is 8,191, linux/mac/bsd is effectively unlimited
    }

    static public SftpFileSystem open(SshSession ssh) {
        final SshSftpSession sftp = sshSftp(ssh)
            .run();

        final String pwd2 = sftp.pwd2();

        final VirtualPath pwd = VirtualPath.parse(pwd2, true);

        log.debug("Sftp pwd: {}", pwd);

        return new SftpFileSystem(ssh, sftp, pwd);
    }

    public int getMaxCommandLength() {
        return maxCommandLength;
    }

    public SftpFileSystem setMaxCommandLength(int maxCommandLength) {
        this.maxCommandLength = maxCommandLength;
        return this;
    }

    private VirtualPath toVirtualPathWithStats(VirtualPath path, SshFileAttributes attributes) throws IOException {
        boolean isDirectory = attributes.isDirectory();
        VirtualStats stats = null;
        if (!isDirectory) {
            long size = attributes.size();
            long modifiedTime = attributes.lastModifiedTime().toMillis();
            stats = new VirtualStats(size, modifiedTime);
        }
        return new VirtualPath(path.getParentPath(), path.getName(), isDirectory, stats);
    }

    @Override
    public boolean isRemote() {
        return true;
    }

    @Override
    public VirtualPath pwd() {
        return this.pwd;
    }

    @Override
    public VirtualPath stat(VirtualPath path) throws IOException {
        try {
            // TODO: safer using full path resolved against pwd?
            final SshFileAttributes file = this.sftp.lstat(path.toString());

            return this.toVirtualPathWithStats(path, file);
        } catch (SshSftpNoSuchFileException e) {
            throw new FileNotFoundException();
        }
    }

    @Override
    public List<VirtualPath> ls(VirtualPath path) throws IOException {
        // TODO: safer using full path resolved against pwd?
        final List<SshFile> files = this.sftp.ls(path.toString());

        final List<VirtualPath> childPaths = new ArrayList<>();

        for (SshFile file : files) {
            // dir true/false doesn't matter, stats call next will correct it
            VirtualPath childPathWithoutStats = path.resolve(file.fileName(), false);
            VirtualPath childPath = this.toVirtualPathWithStats(childPathWithoutStats, file.attributes());
            childPaths.add(childPath);
        }

        return childPaths;
    }

    @Override
    public void mkdir(String path) throws IOException {
        this.sftp.mkdir(path);
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
    public StreamableInput readFile(VirtualPath path) throws IOException {
        throw new UnsupportedOperationException("readFile");
    }

    @Override
    public void writeFile(StreamableInput input, VirtualPath path) throws IOException {
        this.sftp.put()
            .source(input)
            .target(path.toString())
            .run();
    }

    @Override
    public void cksums(List<VirtualPath> paths) throws IOException {
        // we need to be smart about how many files we request in bulk, as the command line can only be so long
        // we can leverage the "workingDir" so that the paths stay shorter, if we're simply in the same dir
        final Map<String,VirtualPath> fileMapping = new HashMap<>();
        Exec exec = null;
        int commandLength = 0;

        for (int i = 0; i < paths.size(); i++) {
            final VirtualPath path = paths.get(i);

            // create new or add to request?
            if (exec == null) {
                exec = this.ssh.newExec().command("cksum");
            }

            final String fullPath = path.toString();

            exec.arg(fullPath);

            fileMapping.put(fullPath, path);

            commandLength += fullPath.length();

            // should we send this request?
            if (commandLength >= this.maxCommandLength || (i == paths.size() - 1)) {
                final String output = exec.runCaptureOutput(false)
                    .toString();

                // parse output into entries
                final List<Cksums.Entry> entries = Cksums.parse(output);
                for (Cksums.Entry entry : entries) {
                    final VirtualPath entryPath = fileMapping.get(entry.getFile());
                    entryPath.getStats().setCksum(entry.getCksum());
                }

                // reset everything for next run
                exec = null;
                commandLength = 0;
                fileMapping.clear();
            }
        }
    }
}

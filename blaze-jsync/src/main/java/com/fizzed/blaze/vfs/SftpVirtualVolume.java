package com.fizzed.blaze.vfs;

import com.fizzed.blaze.ssh.SshSession;
import com.fizzed.blaze.ssh.SshSftpSession;

public class SftpVirtualVolume implements VirtualVolume {

    private final String sshHost;
    private final SshSession ssh;
    private final SshSftpSession sftp;
    private final String path;

    public SftpVirtualVolume(String sshHost, SshSession ssh, SshSftpSession sftp, String path) {
        this.sshHost = sshHost;
        this.ssh = ssh;
        this.sftp = sftp;
        this.path = path;
    }

    @Override
    public String getPath() {
        return this.path;
    }

    @Override
    public VirtualFileSystem openFileSystem() {
        if (this.ssh != null && this.sftp != null) {
            return SftpVirtualFileSystem.open(this.ssh, this.sftp);
        } else if (this.ssh != null) {
            return SftpVirtualFileSystem.open(this.ssh);
        } else {
            return SftpVirtualFileSystem.open(this.sshHost);
        }
    }

    @Override
    public String toString() {
        if (this.ssh != null) {
            return this.ssh.uri().getHost() + ":" + this.path;
        } else {
            return this.sshHost + ":" + this.path;
        }
    }

    static public SftpVirtualVolume sftpVolume(String ssh, String path) {
        return new SftpVirtualVolume(ssh, null, null, path);
    }

    static public SftpVirtualVolume sftpVolume(SshSession ssh, String path) {
        return new SftpVirtualVolume(null, ssh, null, path);
    }

    static public SftpVirtualVolume sftpVolume(SshSession ssh, SshSftpSession sftp, String path) {
        return new SftpVirtualVolume(null, ssh, sftp, path);
    }

}
package com.fizzed.blaze.jsync;

import com.fizzed.blaze.Contexts;
import com.fizzed.blaze.ssh.SshSession;
import com.fizzed.blaze.ssh.SshSftpSession;
import com.fizzed.blaze.ssh.impl.JschSession;
import com.fizzed.blaze.ssh.impl.JschSftpSession;
import com.fizzed.jsync.engine.JsyncMode;
import com.fizzed.jsync.sftp.SftpVirtualFileSystem;
import com.fizzed.jsync.sftp.SftpVirtualVolume;
import com.fizzed.jsync.vfs.LocalVirtualVolume;
import com.fizzed.jsync.vfs.VirtualFileSystem;
import com.fizzed.jsync.vfs.VirtualVolume;

import java.io.IOException;
import java.nio.file.Path;

import static com.fizzed.blaze.SecureShells.sshConnect;

public class Jsyncs {

    static public Jsync jsync() {
        return new Jsync(Contexts.currentContext());
    }

    /**
     * Creates and configures a Jsync instance using the specified source, target, and synchronization mode.
     *
     * @param source the source VirtualVolume to synchronize from
     * @param target the target VirtualVolume to synchronize to
     * @param mode the synchronization mode to determine how the synchronization is performed
     * @return a configured Jsync instance ready for execution
     */
    static public Jsync jsync(VirtualVolume source, VirtualVolume target, JsyncMode mode) {
        return new Jsync(Contexts.currentContext())
            .source(source)
            .target(target, mode);
    }

    static public VirtualVolume localVolume(Path path) {
        return LocalVirtualVolume.localVolume(path);
    }

    static public VirtualVolume sftpVolume(String ssh, String path) {
        return new SftpVirtualVolume(null, null, true, null, true, path) {
            @Override
            public VirtualFileSystem openFileSystem() throws IOException {
                // fix ssh command
                final String sshUrl;
                if (!ssh.startsWith("ssh://")) {
                    sshUrl = "ssh://" + ssh;
                } else {
                    sshUrl = ssh;
                }

                final SshSession sshSession = sshConnect(sshUrl).run();
                final JschSession jschSsh = (JschSession)sshSession;
                return SftpVirtualFileSystem.open(jschSsh.getJschSession(), true);
            }
        };
    }

    static public VirtualVolume sftpVolume(SshSession ssh, String path) {
        final JschSession jschSsh = (JschSession)ssh;
        return SftpVirtualVolume.sftpVolume(jschSsh.getJschSession(), false, path);
    }

    static public VirtualVolume sftpVolume(SshSession ssh, SshSftpSession sftp, String path) {
        final JschSession jschSsh = (JschSession)ssh;
        final JschSftpSession jschSftp = (JschSftpSession)sftp;
        return SftpVirtualVolume.sftpVolume(jschSsh.getJschSession(), false, jschSftp.getJschChannel(), false, path);
    }

}
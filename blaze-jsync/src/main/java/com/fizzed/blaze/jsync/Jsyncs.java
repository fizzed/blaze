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

    /**
     * Creates a Jsync instance associated with the current execution context.
     *
     * @return a new Jsync instance linked to the context in which this method is executed
     */
    static public Jsync jsync() {
        return new Jsync(Contexts.currentContext());
    }

    /**
     * Creates and configures a Jsync instance using the specified source, target, and synchronization mode.
     *
     * @param source the source VirtualVolume to synchronize from
     * @param target the target VirtualVolume to synchronize to
     * @param mode the synchronization mode to determine how the synchronization is performed. JsyncMode.MERGE is
     *             the rsync equivalent of adding a "/" onto the end of the source and target, where the source
     *             will become the target and the contents of each will be merged together. For example, if "a" is
     *             the source and "b" is the target, then all the contents of "a" will be merged with "b". JsyncMode.NEST is
     *             the rsync equivalent of not adding a "/" onto the end of the target, where the source will placed
     *             INTO the target, treating the source (if a dir) as though it was a file. For example, if "a" is
     *             the source and "b" is the target, then "a" and all of its contents will be copied into "b", thus
     *             leaving you with "b/a" as the final result.
     * @return a configured Jsync instance ready for execution
     */
    static public Jsync jsync(VirtualVolume source, VirtualVolume target, JsyncMode mode) {
        return new Jsync(Contexts.currentContext())
            .source(source)
            .target(target, mode);
    }

    /**
     * Creates a local virtual volume based on the provided file system path.
     *
     * @param path the file system path to be associated with the virtual volume
     * @return a VirtualVolume instance representing the local file system at the specified path
     */
    static public VirtualVolume localVolume(Path path) {
        return LocalVirtualVolume.localVolume(path);
    }

    /**
     * Creates a VirtualVolume instance representing an SFTP-based file system.  All ssh and sftp resources will be closed once
     * the jsync operation completes.
     *
     * @param ssh the SSH connection string to the remote server; if it does not start with "ssh://", the method will automatically prepend it
     * @param path the remote file system path associated with the SFTP virtual volume
     * @return a VirtualVolume instance backed by the specified SFTP file system
     */
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

    /**
     * Creates a VirtualVolume instance representing an SFTP-based file system using an existing SSH session.
     *
     * @param ssh the existing SSH session used to establish the SFTP connection. Will not be closed when the jsync operation completes.
     * @param path the remote file system path to associate with the SFTP virtual volume
     * @return a VirtualVolume instance backed by the specified SFTP file system
     */
    static public VirtualVolume sftpVolume(SshSession ssh, String path) {
        final JschSession jschSsh = (JschSession)ssh;
        return SftpVirtualVolume.sftpVolume(jschSsh.getJschSession(), false, path);
    }

    /**
     * Creates a VirtualVolume instance representing an SFTP-based file system using an existing SSH session
     * and an active SFTP session.
     *
     * @param ssh the existing SSH session used to establish the connection to the remote server. Will not be closed when the jsync operation completes.
     * @param sftp the existing SFTP session used to manage file operations on the remote file system. Will not be closed when the jsync operation completes.
     * @param path the remote file system path to associate with the SFTP virtual volume
     * @return a VirtualVolume instance backed by the specified SFTP file system
     */
    static public VirtualVolume sftpVolume(SshSession ssh, SshSftpSession sftp, String path) {
        final JschSession jschSsh = (JschSession)ssh;
        final JschSftpSession jschSftp = (JschSftpSession)sftp;
        return SftpVirtualVolume.sftpVolume(jschSsh.getJschSession(), false, jschSftp.getJschChannel(), false, path);
    }

}
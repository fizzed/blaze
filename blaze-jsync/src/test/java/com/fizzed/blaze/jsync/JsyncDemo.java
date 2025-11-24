package com.fizzed.blaze.jsync;

import com.fizzed.blaze.logging.LogLevel;
import com.fizzed.blaze.logging.LoggerConfig;
import com.fizzed.blaze.ssh.*;
import com.fizzed.blaze.vfs.LocalVirtualFileSystem;
import com.fizzed.blaze.vfs.SftpVirtualFileSystem;
import com.fizzed.blaze.vfs.VirtualFileSystem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Paths;

import static com.fizzed.blaze.SecureShells.sshConnect;
import static com.fizzed.blaze.jsync.Jsyncs.jsync;

public class JsyncDemo {
    static private final Logger log = LoggerFactory.getLogger(JsyncDemo.class);

    static public void main(String[] args) throws Exception {
        LoggerConfig.setDefaultLogLevel(LogLevel.INFO);
//        LoggerConfig.setDefaultLogLevel(LogLevel.DEBUG);

//        final String sourceDir = Paths.get("/home/jjlauer/test-sync").toString();
        final String sourceDir = Paths.get("/home/jjlauer/workspace/third-party/jsch").toString();
//        final String sourceDir = Paths.get("/home/jjlauer/workspace/third-party/jsch").toString();
//        final String sourceDir = Paths.get("C:\\Users\\jjlauer\\test-sync").toString();
//        final String sourceDir = Paths.get("C:\\Users\\jjlauer\\workspace\\third-party\\tokyocabinet-1.4.48").toString();
//        final String sourceDir = Paths.get("/home/jjlauer/workspace/third-party/tokyocabinet-1.4.48").toString();

        final String targetDir = "test-sync";
        final boolean delete = true;

        final SshSession ssh = sshConnect("ssh://bmh-dev-x64-indy25-1").run();
//        final SshSession ssh = sshConnect("ssh://bmh-dev-x64-fedora43-1").run();
//        final SshSession ssh = sshConnect("ssh://bmh-build-x64-freebsd15-1").run();
//        final SshSession ssh = sshConnect("ssh://bmh-build-x64-win11-1").run();

        final VirtualFileSystem sourceVfs = LocalVirtualFileSystem.open();
        final VirtualFileSystem targetVfs = SftpVirtualFileSystem.open(ssh);

        final JsyncResult result = new JsyncEngine()
//            .preferredChecksums(Checksum.CK)
//            .preferredChecksums(Checksum.MD5, Checksum.SHA1)
//            .preferredChecksums(Checksum.SHA1)
            .setDelete(delete)
            .setParents(true)
            .setForce(true)
            //.setIgnoreTimes(true)
//            .setProgress(true)
            .sync(sourceVfs, sourceDir, targetVfs, targetDir, JsyncMode.MERGE);

        log.info("");
        log.info("Done, sync successful!");
        log.info("Result: {}", result);
        log.info("");

        String rsyncCommand = "rsync -ivrt --delete --mkpath --force " + sourceDir + "/ " + ssh.uri().getHost() + ":" + targetDir + "/";
        log.info("Rsync command: {}", rsyncCommand);

        ssh.close();






    }

}
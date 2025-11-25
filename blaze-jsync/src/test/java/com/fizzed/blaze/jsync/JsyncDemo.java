package com.fizzed.blaze.jsync;

import com.fizzed.blaze.logging.LogLevel;
import com.fizzed.blaze.logging.LoggerConfig;
import com.fizzed.blaze.util.Timer;
import com.fizzed.blaze.vfs.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Paths;

import static com.fizzed.blaze.jsync.Jsyncs.jsync;
import static com.fizzed.blaze.vfs.LocalVirtualVolume.localVolume;
import static com.fizzed.blaze.vfs.SftpVirtualVolume.sftpVolume;

public class JsyncDemo {
    static private final Logger log = LoggerFactory.getLogger(JsyncDemo.class);

    static public void main(String[] args) throws Exception {
        LoggerConfig.setDefaultLogLevel(LogLevel.INFO);
//        LoggerConfig.setDefaultLogLevel(LogLevel.DEBUG);

//        final String sourceDir = Paths.get("/home/jjlauer/test-sync").toString();
//        final String sourceDir = Paths.get("/home/jjlauer/workspace/third-party/jsch").toString();
//        final String sourceDir = Paths.get("/home/jjlauer/workspace/third-party/coredns").toString();
//        final VirtualVolume source = localVolume(Paths.get("/home/jjlauer/workspace/third-party/nats.java"));
        final VirtualVolume source = localVolume(Paths.get("/home/jjlauer/workspace/third-party/tokyocabinet-1.4.48"));
//        final String sourceDir = Paths.get("C:\\Users\\jjlauer\\test-sync").toString();
//        final String sourceDir = Paths.get("C:\\Users\\jjlauer\\workspace\\third-party\\tokyocabinet-1.4.48").toString();
//        final String sourceDir = Paths.get("/home/jjlauer/workspace/third-party/tokyocabinet-1.4.48").toString();
//        final String sourceDir = Paths.get("/home/jjlauer/Downloads/haiku-r1beta5-x86_64-anyboot.iso").toString();


        final String targetDir = "./////test-sync";

//        final String sshHost = "bmh-dev-x64-indy25-1";
//        final String sshHost = "bmh-dev-x64-fedora43-1";
//        final String sshHost = "bmh-build-x64-freebsd15-1";
//        final VirtualVolume target = sshVolume("bmh-build-x64-win11-1", targetDir);
        final VirtualVolume target = sftpVolume("bmh-dev-x64-indy25-1", targetDir);
//        final VirtualVolume target = sftpVolume("bmh-dev-x64-fedora43-1", targetDir);


        final Timer timer = new Timer();


        final JsyncResult result = jsync(source, target, JsyncMode.NEST)
            .verbose()
//            .debug()
            .progress()
            .parents()
            .delete()
            .force()
            .run();

       /* final JsyncResult result = new JsyncEngine()
            .verbose()
            .setProgress(true)
//            .preferredChecksums(Checksum.CK)
//            .preferredChecksums(Checksum.MD5, Checksum.SHA1)
//            .preferredChecksums(Checksum.SHA1)
            .setDelete(true)
            .setParents(true)
            .setForce(true)
            //.setIgnoreTimes(true)
//            .setProgress(true)
            .sync(source, target, JsyncMode.NEST);
//            .sync(targetVfs, targetDir, sourceVfs, sourceDir, JsyncMode.NEST);*/

        log.info("");
        log.info("Done, sync successful in {}!", timer);
        log.info("Result: {}", result);
        log.info("");

        String rsyncCommand = "rsync -ivrt --delete --mkpath --force " + source + (result.getMode() == JsyncMode.NEST ? "" : "/") + " " + target + "/";
        log.info("Rsync command: {}", rsyncCommand);
    }

}
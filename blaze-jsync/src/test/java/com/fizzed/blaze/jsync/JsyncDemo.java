package com.fizzed.blaze.jsync;

import com.fizzed.blaze.logging.LogLevel;
import com.fizzed.blaze.logging.LoggerConfig;
import com.fizzed.blaze.util.Timer;
import com.fizzed.jsync.engine.JsyncMode;
import com.fizzed.jsync.engine.JsyncResult;
import com.fizzed.jsync.vfs.VirtualVolume;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Paths;

import static com.fizzed.blaze.jsync.Jsyncs.*;

public class JsyncDemo {
    static private final Logger log = LoggerFactory.getLogger(JsyncDemo.class);

    static public void main(String[] args) throws Exception {
        LoggerConfig.setDefaultLogLevel(LogLevel.INFO);
//        LoggerConfig.setDefaultLogLevel(LogLevel.DEBUG);

//        final String sourceDir = Paths.get("/home/jjlauer/test-sync").toString();
//        final String sourceDir = Paths.get("/home/jjlauer/workspace/third-party/jsch").toString();
//        final String sourceDir = Paths.get("/home/jjlauer/workspace/third-party/coredns").toString();
        final VirtualVolume source = localVolume(Paths.get("/home/jjlauer/workspace/third-party/nats.java"));
//        final VirtualVolume source = localVolume(Paths.get("/home/jjlauer/workspace/third-party/tokyocabinet-1.4.48"));
//        final VirtualVolume source = localVolume(Paths.get("/home/jjlauer/Downloads"));
//        final String sourceDir = Paths.get("C:\\Users\\jjlauer\\test-sync").toString();
//        final String sourceDir = Paths.get("C:\\Users\\jjlauer\\workspace\\third-party\\tokyocabinet-1.4.48").toString();
//        final String sourceDir = Paths.get("/home/jjlauer/workspace/third-party/tokyocabinet-1.4.48").toString();
//        final String sourceDir = Paths.get("/home/jjlauer/Downloads/haiku-r1beta5-x86_64-anyboot.iso").toString();

//        final String sshHost = "bmh-dev-x64-indy25-1";
//        final String sshHost = "bmh-dev-x64-fedora43-1";
//        final String sshHost = "bmh-build-x64-freebsd15-1";
//        final VirtualVolume target = sshVolume("bmh-build-x64-win11-1", "test-sync");
//        final VirtualVolume target = sftpVolume("bmh-dev-x64-indy25-1", "test-sync");
        final VirtualVolume target = sftpVolume("bmh-dev-x64-fedora43-1", "test-sync");


        final JsyncResult result = jsync(source, target, JsyncMode.MERGE)
//            .verbose()
            .debug()
            .progress()
            .parents()
//            .ignoreTimes()
            .delete()
            .force()
            .run();

        log.info("");

        String rsyncCommand = "rsync -ivrt --delete --mkpath --force " + source + (result.getMode() == JsyncMode.NEST ? "" : "/") + " " + target + "/";
        log.info("Rsync command: {}", rsyncCommand);
    }

}
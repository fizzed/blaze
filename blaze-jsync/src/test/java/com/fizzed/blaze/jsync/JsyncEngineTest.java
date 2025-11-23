package com.fizzed.blaze.jsync;

import com.fizzed.crux.util.MoreFiles;
import com.fizzed.crux.util.Resources;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

class JsyncEngineTest {
    static private final Logger log = LoggerFactory.getLogger(JsyncEngineTest.class);

    private Path projectTargetDir;
    private Path syncSourceDir;
    private Path syncTargetDir;

    @BeforeEach
    public void before() throws IOException {
        projectTargetDir = Resources.file("/locate.txt").resolve("../..").toAbsolutePath().normalize();
        // setup fresh sync dirs
        syncSourceDir = this.projectTargetDir.resolve("sync-tests/source");
        syncTargetDir = this.projectTargetDir.resolve("sync-tests/target");
        // delete those dirs
        MoreFiles.deleteDirectoryIfExists(this.syncSourceDir);
        MoreFiles.deleteDirectoryIfExists(this.syncTargetDir);
        // create them fresh
        Files.createDirectories(this.syncSourceDir);
        Files.createDirectories(this.syncTargetDir);
    }

    @Test
    public void mergeLocalToLocal() throws Exception {
        Path sourceADir = this.syncSourceDir.resolve("a");
        Files.createDirectories(sourceADir);
        Path sourceADirBFile = sourceADir.resolve("b.txt");
        Files.write(sourceADirBFile, "hello".getBytes());

        new JsyncEngine()
            .sync(sourceADir, this.syncTargetDir, JsyncMode.MERGE);

        // we should now have target/b.txt if MERGE worked
        Path targetBFile = this.syncTargetDir.resolve("b.txt");
        assertThat(targetBFile).exists().isNotEmptyFile();
        assertThat(targetBFile).hasSameTextualContentAs(sourceADirBFile);
    }

    @Test
    public void nestLocalToLocal() throws Exception {
        Path sourceADir = this.syncSourceDir.resolve("a");
        Files.createDirectories(sourceADir);
        Path sourceADirBFile = sourceADir.resolve("b.txt");
        Files.write(sourceADirBFile, "hello".getBytes());

        new JsyncEngine()
            .sync(sourceADir, this.syncTargetDir, JsyncMode.NEST);

        // we should now have target/a/b.txt if NEST worked
        Path targetADir = this.syncTargetDir.resolve("a");
        Path targetADirBFile = targetADir.resolve("b.txt");
        assertThat(targetADir).exists().isDirectory();
        assertThat(targetADirBFile).exists().isNotEmptyFile();
        assertThat(targetADirBFile).hasSameTextualContentAs(sourceADirBFile);
    }


}
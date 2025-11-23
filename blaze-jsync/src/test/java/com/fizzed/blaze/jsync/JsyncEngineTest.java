package com.fizzed.blaze.jsync;

import com.fizzed.blaze.vfs.ParentDirectoryMissingException;
import com.fizzed.blaze.vfs.PathOverwriteException;
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
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.fail;

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
    public void mergeOneLevelDirectory() throws Exception {
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
    public void nestOneLevelDirectory() throws Exception {
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

    @Test
    public void mergeOneLevelFile() throws Exception {
        Path sourceADir = this.syncSourceDir.resolve("a");
        Files.createDirectories(sourceADir);
        Path sourceADirBFile = sourceADir.resolve("b.txt");
        Files.write(sourceADirBFile, "hello".getBytes());

        Path targetBFile = this.syncTargetDir.resolve("b.txt");

        new JsyncEngine()
            .sync(sourceADirBFile, targetBFile, JsyncMode.MERGE);

        // we should now have target/b.txt if MERGE worked
        assertThat(targetBFile).exists().isNotEmptyFile();
        assertThat(targetBFile).hasSameTextualContentAs(sourceADirBFile);
    }

    @Test
    public void nestOneLevelFile() throws Exception {
        Path sourceADir = this.syncSourceDir.resolve("a");
        Files.createDirectories(sourceADir);
        Path sourceADirBFile = sourceADir.resolve("b.txt");
        Files.write(sourceADirBFile, "hello".getBytes());

        new JsyncEngine()
            .sync(sourceADirBFile, this.syncTargetDir, JsyncMode.NEST);

        Path targetBFile = this.syncTargetDir.resolve("b.txt");
        // we should now have target/b.txt if MERGE worked
        assertThat(targetBFile).exists().isNotEmptyFile();
        assertThat(targetBFile).hasSameTextualContentAs(sourceADirBFile);
    }

    @Test
    public void mergeTwoLevelDirectory() throws Exception {
        Path sourceADir = this.syncSourceDir.resolve("a");
        Files.createDirectories(sourceADir);
        Path sourceADirBDir = sourceADir.resolve("b");
        Files.createDirectories(sourceADirBDir);
        Path sourceADirBDirCFile = sourceADirBDir.resolve("c.txt");
        Files.write(sourceADirBDirCFile, "hello".getBytes());

        new JsyncEngine()
            .sync(sourceADir, this.syncTargetDir, JsyncMode.MERGE);

        // we should now have target/b.txt if MERGE worked
        Path targetCFile = this.syncTargetDir.resolve("b/c.txt");
        assertThat(targetCFile).exists().isNotEmptyFile();
        assertThat(targetCFile).hasSameTextualContentAs(sourceADirBDirCFile);
    }

    @Test
    public void nestTwoLevelDirectory() throws Exception {
        Path sourceADir = this.syncSourceDir.resolve("a");
        Files.createDirectories(sourceADir);
        Path sourceADirBDir = sourceADir.resolve("b");
        Files.createDirectories(sourceADirBDir);
        Path sourceADirBDirCFile = sourceADirBDir.resolve("c.txt");
        Files.write(sourceADirBDirCFile, "hello".getBytes());

        new JsyncEngine()
            .sync(sourceADir, this.syncTargetDir, JsyncMode.NEST);

        // we should now have target/b.txt if MERGE worked
        Path targetCFile = this.syncTargetDir.resolve("a/b/c.txt");
        assertThat(targetCFile).exists().isNotEmptyFile();
        assertThat(targetCFile).hasSameTextualContentAs(sourceADirBDirCFile);
    }

    @Test
    public void mergeOneLevelDirectoryToOneLevelDirectory() throws Exception {
        Path sourceADir = this.syncSourceDir.resolve("a");
        Files.createDirectories(sourceADir);
        Path sourceADirBFile = sourceADir.resolve("b.txt");
        Files.write(sourceADirBFile, "hello".getBytes());

        new JsyncEngine()
            .sync(sourceADir, this.syncTargetDir.resolve("sub-target"), JsyncMode.MERGE);

        // we should now have target/b.txt if MERGE worked
        Path targetBFile = this.syncTargetDir.resolve("sub-target/b.txt");
        assertThat(targetBFile).exists().isNotEmptyFile();
        assertThat(targetBFile).hasSameTextualContentAs(sourceADirBFile);
    }

    @Test
    public void mergeOneLevelDirectoryToTwoLevelDirectory() throws Exception {
        Path sourceADir = this.syncSourceDir.resolve("a");
        Files.createDirectories(sourceADir);
        Path sourceADirBFile = sourceADir.resolve("b.txt");
        Files.write(sourceADirBFile, "hello".getBytes());

        // NOTE: this should fail if "parents" is not set to true
        assertThrows(ParentDirectoryMissingException.class, () -> {
            new JsyncEngine()
                .sync(sourceADir, this.syncTargetDir.resolve("sub-target/sub-target2"), JsyncMode.MERGE);
        });

        // should work with parents set to true
        new JsyncEngine()
            .setParents(true)
            .sync(sourceADir, this.syncTargetDir.resolve("sub-target/sub-target2"), JsyncMode.MERGE);

        // we should now have target/b.txt if MERGE worked
        Path targetBFile = this.syncTargetDir.resolve("sub-target/sub-target2/b.txt");
        assertThat(targetBFile).exists().isNotEmptyFile();
        assertThat(targetBFile).hasSameTextualContentAs(sourceADirBFile);
    }

    @Test
    public void syncDirectoryToAnExistingFile() throws Exception {
        Path sourceADir = this.syncSourceDir.resolve("a");
        Files.createDirectories(sourceADir);
        Path sourceADirBFile = sourceADir.resolve("b.txt");
        Files.write(sourceADirBFile, "hello".getBytes());

        // create a file in the target dir
        Path subTargetFile = this.syncTargetDir.resolve("sub-target");
        Files.write(subTargetFile, "hello".getBytes());

        assertThrows(PathOverwriteException.class, () -> {
            new JsyncEngine()
                .sync(sourceADir, subTargetFile, JsyncMode.MERGE);
        });

        // nest mode should also fail
        assertThrows(PathOverwriteException.class, () -> {
            new JsyncEngine()
                .sync(sourceADir, subTargetFile, JsyncMode.NEST);
        });
    }

    @Test
    public void mergeFileToAnExistingDirectory() throws Exception {
        Path sourceADir = this.syncSourceDir.resolve("a");
        Files.createDirectories(sourceADir);
        Path sourceADirBFile = sourceADir.resolve("b.txt");
        Files.write(sourceADirBFile, "hello".getBytes());

        assertThrows(PathOverwriteException.class, () -> {
            new JsyncEngine()
                .sync(sourceADirBFile, this.syncTargetDir, JsyncMode.MERGE);
        });

        // nest mode should actually work
        new JsyncEngine()
            .sync(sourceADirBFile, this.syncTargetDir, JsyncMode.NEST);

        // we should now have target/b.txt if MERGE worked
        Path targetBFile = this.syncTargetDir.resolve("b.txt");
        assertThat(targetBFile).exists().isNotEmptyFile();
        assertThat(targetBFile).hasSameTextualContentAs(sourceADirBFile);
    }

}
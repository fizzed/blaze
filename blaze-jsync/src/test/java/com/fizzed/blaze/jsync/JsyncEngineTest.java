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
import java.nio.file.attribute.FileAttribute;
import java.nio.file.attribute.FileTime;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;
import static org.junit.jupiter.api.Assertions.assertThrows;

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

    protected void writeFile(Path path, String content) throws IOException {
        Files.createDirectories(path.getParent());
        Files.write(path, content.getBytes());
    }

    protected void touch(Path path, Instant ts) throws IOException {
        Files.setLastModifiedTime(path, FileTime.from(ts));
    }

    protected Instant modifiedTime(Path path) throws IOException {
        return Files.getLastModifiedTime(path).toInstant();
    }

    @Test
    public void mergeOneLevelDirectory() throws Exception {
        Path sourceBFile = this.syncSourceDir.resolve("a/b.txt");
        this.writeFile(sourceBFile, "hello");

        new JsyncEngine()
            .verbose()
            .sync(this.syncSourceDir.resolve("a"), this.syncTargetDir, JsyncMode.MERGE);

        // we should now have target/b.txt if MERGE worked
        Path targetBFile = this.syncTargetDir.resolve("b.txt");
        assertThat(targetBFile).exists().isNotEmptyFile();
        assertThat(targetBFile).hasSameTextualContentAs(sourceBFile);
    }

    @Test
    public void nestOneLevelDirectory() throws Exception {
        Path sourceADir = this.syncSourceDir.resolve("a");
        Files.createDirectories(sourceADir);
        Path sourceADirBFile = sourceADir.resolve("b.txt");
        Files.write(sourceADirBFile, "hello".getBytes());

        new JsyncEngine()
            .verbose()
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
            .verbose()
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
            .verbose()
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
            .verbose()
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
            .verbose()
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
            .verbose()
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
                .verbose()
                .sync(sourceADir, this.syncTargetDir.resolve("sub-target/sub-target2"), JsyncMode.MERGE);
        });

        // should work with parents set to true
        new JsyncEngine()
            .verbose()
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
                .verbose()
                .sync(sourceADir, subTargetFile, JsyncMode.MERGE);
        });

        // nest mode should also fail (various exceptions depending on filesystems)
        assertThrows(Exception.class, () -> {
            new JsyncEngine()
                .verbose()
                .sync(sourceADir, subTargetFile, JsyncMode.NEST);
        });
    }

    @Test
    public void syncFileToAnExistingDirectory() throws Exception {
        Path sourceADir = this.syncSourceDir.resolve("a");
        Files.createDirectories(sourceADir);
        Path sourceADirBFile = sourceADir.resolve("b.txt");
        Files.write(sourceADirBFile, "hello".getBytes());

        assertThrows(PathOverwriteException.class, () -> {
            new JsyncEngine()
                .verbose()
                .sync(sourceADirBFile, this.syncTargetDir, JsyncMode.MERGE);
        });

        // nest mode should actually work
        new JsyncEngine()
            .verbose()
            .sync(sourceADirBFile, this.syncTargetDir, JsyncMode.NEST);

        // we should now have target/b.txt if MERGE worked
        Path targetBFile = this.syncTargetDir.resolve("b.txt");
        assertThat(targetBFile).exists().isNotEmptyFile();
        assertThat(targetBFile).hasSameTextualContentAs(sourceADirBFile);
    }

    @Test
    public void syncIncludesSourceDirToExistingTargetFile() throws Exception {
        Path sourceADir = this.syncSourceDir.resolve("a");
        Files.createDirectories(sourceADir);
        Path sourceADirBDir = sourceADir.resolve("b");
        Files.createDirectories(sourceADirBDir);
        Path sourceADirBDirCFile = sourceADirBDir.resolve("c.txt");
        Files.write(sourceADirBDirCFile, "hello".getBytes());

        // create a file in the target dir, named "b" that's actually a file
        Path targetADir = this.syncTargetDir.resolve("a");
        Files.createDirectories(targetADir);
        Path targetADirBFile = targetADir.resolve("b");
        Files.write(targetADirBFile, "hello".getBytes());

        // should fail with overwrite exception (w/o force flag set)
        assertThrows(PathOverwriteException.class, () -> {
            new JsyncEngine()
                .verbose()
                .sync(this.syncSourceDir, this.syncTargetDir, JsyncMode.MERGE);
        });

        // should work with force flag set
        new JsyncEngine()
            .verbose()
            .setForce(true)
            .sync(this.syncSourceDir, this.syncTargetDir, JsyncMode.MERGE);

        // we should now have target/b.txt if MERGE worked
        Path targetCFile = this.syncTargetDir.resolve("a/b/c.txt");
        assertThat(targetCFile).exists().isNotEmptyFile();
        assertThat(targetCFile).hasSameTextualContentAs(sourceADirBDirCFile);
    }

    @Test
    public void syncIncludesSourceFileToExistingTargetDirectory() throws Exception {
        Path sourceADir = this.syncSourceDir.resolve("a");
        Files.createDirectories(sourceADir);
        Path sourceADirBFile = sourceADir.resolve("b");
        Files.write(sourceADirBFile, "hello".getBytes());

        // create a file in the target dir, named "b" that's actually a file
        Path targetADir = this.syncTargetDir.resolve("a");
        Files.createDirectories(targetADir);
        Path targetADirBDir = targetADir.resolve("b");
        Files.createDirectories(targetADirBDir);
        Path targetADirBDirCFile = targetADirBDir.resolve("c.txt");
        Files.write(targetADirBDirCFile, "hello".getBytes());

        // should fail with overwrite exception (w/o force flag set)
        assertThrows(PathOverwriteException.class, () -> {
            new JsyncEngine()
                .verbose()
                .sync(this.syncSourceDir, this.syncTargetDir, JsyncMode.MERGE);
        });

        // should work with force flag set
        new JsyncEngine()
            .verbose()
            .setForce(true)
            .sync(this.syncSourceDir, this.syncTargetDir, JsyncMode.MERGE);

        // we should now have target/a/b if MERGE worked
        Path targetBFile = this.syncTargetDir.resolve("a/b");
        assertThat(targetBFile).exists().isNotEmptyFile();
        assertThat(targetBFile).hasSameTextualContentAs(sourceADirBFile);
    }

    @Test
    public void syncContentModifiedBySize() throws Exception {
        Path sourceADir = this.syncSourceDir.resolve("a");
        Files.createDirectories(sourceADir);
        Path sourceADirBFile = sourceADir.resolve("b.txt");
        Files.write(sourceADirBFile, "hello".getBytes());

        // sync so we have a replica of source
        new JsyncEngine()
            .verbose()
            .sync(this.syncSourceDir, this.syncTargetDir, JsyncMode.MERGE);

        // update b.txt in source with same file size though
        Files.write(sourceADirBFile, "hello yo".getBytes());

        // sync which should require a checksum to sync properly
        final JsyncResult result = new JsyncEngine()
            .sync(this.syncSourceDir, this.syncTargetDir, JsyncMode.MERGE);

        // we should now have target/a/b if MERGE worked
        Path targetBFile = this.syncTargetDir.resolve("a/b.txt");
        assertThat(targetBFile).exists().isNotEmptyFile();
        assertThat(targetBFile).hasSameTextualContentAs(sourceADirBFile);

        assertThat(result.getFilesCreated()).isEqualTo(0);
        assertThat(result.getFilesDeleted()).isEqualTo(0);
        assertThat(result.getFilesUpdated()).isEqualTo(1);
        assertThat(result.getChecksums()).isEqualTo(0);
    }

    @Test
    public void syncContentModifiedRequiringChecksum() throws Exception {
        Path sourceADir = this.syncSourceDir.resolve("a");
        Files.createDirectories(sourceADir);
        Path sourceADirBFile = sourceADir.resolve("b.txt");
        Files.write(sourceADirBFile, "hello".getBytes());

        // sync so we have a replica of source
        new JsyncEngine()
            .verbose()
            .sync(this.syncSourceDir, this.syncTargetDir, JsyncMode.MERGE);

        // update b.txt in source with same file size though
        Files.write(sourceADirBFile, "hellp".getBytes());

        // sync which should require a checksum to sync properly
        final JsyncResult result = new JsyncEngine()
            .setIgnoreTimes(true)       // due to how quickly the files are modified they look similar
            .sync(this.syncSourceDir, this.syncTargetDir, JsyncMode.MERGE);

        // we should now have target/a/b if MERGE worked
        Path targetBFile = this.syncTargetDir.resolve("a/b.txt");
        assertThat(targetBFile).exists().isNotEmptyFile();
        assertThat(targetBFile).hasSameTextualContentAs(sourceADirBFile);

        assertThat(result.getFilesCreated()).isEqualTo(0);
        assertThat(result.getFilesDeleted()).isEqualTo(0);
        assertThat(result.getFilesUpdated()).isEqualTo(1);
        assertThat(result.getChecksums()).isEqualTo(1);
    }

    @Test
    public void syncFileMissing() throws Exception {
        Path sourceAFile = this.syncSourceDir.resolve("a.txt");
        Files.write(sourceAFile, "hello".getBytes());

        Path targetAFile = this.syncTargetDir.resolve("a.txt");
        // don't write it yet

        JsyncResult result = new JsyncEngine()
            .verbose()
            .sync(sourceAFile, this.syncTargetDir, JsyncMode.NEST);

        assertThat(targetAFile).exists().isNotEmptyFile();
        assertThat(targetAFile).hasSameTextualContentAs(sourceAFile);
        assertThat(result.getFilesCreated()).isEqualTo(1);
        assertThat(result.getFilesDeleted()).isEqualTo(0);
        assertThat(result.getFilesUpdated()).isEqualTo(0);
        assertThat(result.getStatsUpdated()).isEqualTo(1);
        assertThat(result.getChecksums()).isEqualTo(0);         // no checksums either, size alone was enough
    }

    @Test
    public void syncFileSizeMismatch() throws Exception {
        Path sourceAFile = this.syncSourceDir.resolve("a.txt");
        Files.write(sourceAFile, "hello".getBytes());

        Path targetAFile = this.syncTargetDir.resolve("a.txt");
        Files.write(targetAFile, "helloy".getBytes());

        JsyncResult result = new JsyncEngine()
            .verbose()
            .setProgress(true)
            .sync(sourceAFile, this.syncTargetDir, JsyncMode.NEST);

        assertThat(targetAFile).exists().isNotEmptyFile();
        assertThat(targetAFile).hasSameTextualContentAs(sourceAFile);
        assertThat(result.getFilesCreated()).isEqualTo(0);
        assertThat(result.getFilesDeleted()).isEqualTo(0);
        assertThat(result.getFilesUpdated()).isEqualTo(1);
        assertThat(result.getStatsUpdated()).isEqualTo(0);       // timestamps didn't need updating
        assertThat(result.getChecksums()).isEqualTo(0);         // no checksums either, size alone was enough
    }

    @Test
    public void syncFileIgnoreTimes() throws Exception {
        Path sourceAFile = this.syncSourceDir.resolve("a.txt");
        this.writeFile(sourceAFile, "hello");

        Path targetAFile = this.syncTargetDir.resolve("a.txt");
        this.writeFile(targetAFile, "hellp");

        // set timestamps to same on file
        Instant ts = Instant.parse("2023-03-11T01:02:03.000Z");
        this.touch(sourceAFile, ts);
        this.touch(targetAFile, ts);

        // since the timestamps on each file match, this should do nothing
        JsyncResult result = new JsyncEngine()
            .verbose()
            .sync(this.syncSourceDir, this.syncTargetDir, JsyncMode.MERGE);

        assertThat(result.getFilesUpdated()).isEqualTo(0);

        // if we "ignoreTimes" though, then the file would sync
        result = new JsyncEngine()
            .verbose()
            .setIgnoreTimes(true)
            .sync(this.syncSourceDir, this.syncTargetDir, JsyncMode.MERGE);

        assertThat(targetAFile).exists().isNotEmptyFile();
        assertThat(targetAFile).hasSameTextualContentAs(sourceAFile);
        assertThat(result.getFilesCreated()).isEqualTo(0);
        assertThat(result.getFilesDeleted()).isEqualTo(0);
        assertThat(result.getFilesUpdated()).isEqualTo(1);
        assertThat(result.getStatsUpdated()).isEqualTo(0);       // timestamps didn't need updating
        assertThat(result.getChecksums()).isEqualTo(1);
    }

    @Test
    public void syncFileTimestampMismatch() throws Exception {
        Path sourceAFile = this.syncSourceDir.resolve("a.txt");
        Files.write(sourceAFile, "hello".getBytes());

        Path targetAFile = this.syncTargetDir.resolve("a.txt");
        Files.write(targetAFile, "hellp".getBytes());

        this.touch(targetAFile, Instant.now().minusSeconds(60));

        JsyncResult result = new JsyncEngine()
            .verbose()
            .sync(sourceAFile, this.syncTargetDir, JsyncMode.NEST);

        assertThat(targetAFile).exists().isNotEmptyFile();
        assertThat(targetAFile).hasSameTextualContentAs(sourceAFile);
        assertThat(result.getFilesCreated()).isEqualTo(0);
        assertThat(result.getFilesDeleted()).isEqualTo(0);
        assertThat(result.getFilesUpdated()).isEqualTo(1);
        assertThat(result.getStatsUpdated()).isEqualTo(1);
        assertThat(result.getChecksums()).isEqualTo(1);
    }

    @Test
    public void syncFileTimestamp() throws Exception {
        Path sourceAFile = this.syncSourceDir.resolve("a.txt");
        Files.write(sourceAFile, "hello".getBytes());

        Path targetAFile = this.syncTargetDir.resolve("a.txt");
        Files.write(targetAFile, "hello".getBytes());

        Instant ts = Instant.parse("2023-03-11T01:02:03.000Z");
        this.touch(sourceAFile, ts);

        JsyncResult result = new JsyncEngine()
            .verbose()
            .sync(sourceAFile, this.syncTargetDir, JsyncMode.NEST);

        assertThat(modifiedTime(targetAFile)).isCloseTo(ts, within(2, ChronoUnit.SECONDS));
        assertThat(result.getStatsUpdated()).isEqualTo(1);
        assertThat(result.getFilesCreated()).isEqualTo(0);
        assertThat(result.getFilesDeleted()).isEqualTo(0);
        assertThat(result.getFilesUpdated()).isEqualTo(0);      // important: checksum should mean we don't transfer file, just its stat
        assertThat(result.getChecksums()).isEqualTo(1);
    }

    @Test
    public void syncDirTimestamp() throws Exception {
        Path sourceADir = this.syncSourceDir.resolve("a");
        Path sourceBFile = this.syncSourceDir.resolve("a/b.txt");
        this.writeFile(sourceBFile, "hello");

        Path targetADir = this.syncTargetDir.resolve("a");
        Path targetBFile = this.syncTargetDir.resolve("a/b.txt");
        this.writeFile(targetBFile, "helloy");  // make size different so this file is synced, causing the dir "modified" time to change

        // only difference will be timestamps of "a" dir
        Instant ts = Instant.parse("2023-03-11T01:02:03.000Z");
        this.touch(sourceADir, ts);

        final JsyncResult result = new JsyncEngine()
            .verbose()
            .sync(this.syncSourceDir, this.syncTargetDir, JsyncMode.MERGE);

        assertThat(result.getStatsUpdated()).isEqualTo(1);
        assertThat(modifiedTime(targetADir)).isCloseTo(ts, within(2, ChronoUnit.SECONDS));
    }

    @Test
    public void syncExcludeDir() throws Exception {
        Path sourceADir = this.syncSourceDir.resolve("a");
        Path sourceBFile = this.syncSourceDir.resolve("a/b.txt");
        this.writeFile(sourceBFile, "hello");

        Path sourceBDir = this.syncSourceDir.resolve("b");
        Path sourceCFile = this.syncSourceDir.resolve("b/c.txt");
        Path sourceDFile = this.syncSourceDir.resolve("b/d.txt");
        this.writeFile(sourceCFile, "hello");
        this.writeFile(sourceDFile, "hello");

        // with directory "a" fully excluded, it actually should be deleted with --exclude
        Path targetADir = this.syncTargetDir.resolve("a");
        Path targetBFile = this.syncTargetDir.resolve("a/b.txt");
        this.writeFile(targetBFile, "hello");

        final JsyncResult result = new JsyncEngine()
            .verbose()
            .addExclude("a")
            .addExclude("b/c.txt")
            .setDelete(true)
            .sync(this.syncSourceDir, this.syncTargetDir, JsyncMode.MERGE);

        assertThat(result.getFilesCreated()).isEqualTo(1);
        assertThat(targetADir).doesNotExist();
        assertThat(this.syncTargetDir.resolve("b/d.txt")).hasContent("hello");
    }

    @Test
    public void syncExcludeNonRegularFiles() throws Exception {
        Path sourceADir = this.syncSourceDir.resolve("a");
        Path sourceBFile = this.syncSourceDir.resolve("a/b.txt");
        Path sourceCFile = this.syncSourceDir.resolve("a/c.txt");
        this.writeFile(sourceBFile, "hello");
        Files.createSymbolicLink(sourceCFile, sourceBFile);

        final JsyncResult result = new JsyncEngine()
            .verbose()
            .setDelete(true)
            .sync(this.syncSourceDir, this.syncTargetDir, JsyncMode.MERGE);

        assertThat(result.getFilesCreated()).isEqualTo(1);
        assertThat(this.syncTargetDir.resolve("a/c.txt")).doesNotExist();
        assertThat(this.syncTargetDir.resolve("a/b.txt")).hasContent("hello");
    }

}
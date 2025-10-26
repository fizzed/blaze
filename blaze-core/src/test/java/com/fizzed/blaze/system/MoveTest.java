package com.fizzed.blaze.system;

import com.fizzed.blaze.core.BlazeException;
import com.fizzed.blaze.core.FileNotFoundException;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static com.fizzed.blaze.util.Globber.globber;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class MoveTest extends TestAbstractBase {

    Path testMoveDir;

    @BeforeEach
    public void setup() throws Exception {
        this.testMoveDir = this.createDir(targetDir.resolve("move-test"));
    }

    @Test
    public void fileToFileFailsIfSourceDoesNotExist() {
        assertThrows(FileNotFoundException.class, () -> {
            new Move(this.context)
                .sources(this.testMoveDir.resolve("notexist"))
                .target(this.testMoveDir)
                .run();
        });
    }

    @Test
    public void fileToFileFailsToSameFile() throws Exception {
        final Path sourceFile = createFile(this.testMoveDir.resolve("fileToFileFailsToSameFile.txt"));

        assertThrows(BlazeException.class, () -> {
            new Move(this.context)
                .sources(sourceFile)
                .target(sourceFile)
                .run();
        });
    }

    @Test
    public void fileToFileFailsToSameFileEvenIfForced() throws Exception {
        final Path sourceFile = createFile(this.testMoveDir.resolve("fileToFileFailsToSameFileEvenIfForced.txt"));

        assertThrows(BlazeException.class, () -> {
            new Move(this.context)
                .sources(sourceFile)
                .target(sourceFile)
                .force()
                .run();
        });
    }

    @Test
    public void fileToDirFailsToSameFile() throws Exception {
        final Path sourceFile = createFile(this.testMoveDir.resolve("fileToDirFailsToSameFile.txt"));

        assertThrows(BlazeException.class, () -> {
            new Move(this.context)
                .sources(sourceFile)
                .target(sourceFile.getParent())
                .run();
        });
    }

    @Test
    public void fileToFile() throws Exception {
        final Path sourceFile = createFile(this.testMoveDir.resolve("fileToFile.txt"));
        final Path targetDir = createDir(this.testMoveDir.resolve("fileToFile"));
        final Path targetFile = targetDir.resolve(sourceFile.getFileName());

        assertThat(Files.exists(sourceFile), is(true));
        assertThat(Files.exists(targetFile), is(false));

        new Move(this.context)
            .sources(sourceFile)
            .target(targetFile)
            .run();

        assertThat(Files.exists(sourceFile), is(false));
        assertThat(Files.exists(targetFile), is(true));
    }

    @Test
    public void fileToFileFailsIfAlreadyExists() throws Exception {
        final Path sourceFile = createFile(this.testMoveDir.resolve("fileToFileFailsIfAlreadyExists.txt"));
        final Path targetDir = createDir(this.testMoveDir.resolve("fileToFileFailsIfAlreadyExists"));
        final Path targetFile = createFile(targetDir.resolve("exists.txt"));

        assertThrows(BlazeException.class, () -> {
            new Move(this.context)
                .sources(sourceFile)
                .target(targetFile)
                .run();
        });

        assertThat(Files.exists(sourceFile), is(true));
        assertThat(Files.exists(targetFile), is(true));
    }

    @Test
    public void fileToFileForced() throws Exception {
        final Path sourceFile = createFile(this.testMoveDir.resolve("fileToFileFailsIfAlreadyExists.txt"), "hello world");
        final Path targetDir = createDir(this.testMoveDir.resolve("fileToFileFailsIfAlreadyExists"));
        final Path targetFile = createFile(targetDir.resolve("exists.txt"));

        assertThat(Files.exists(sourceFile), is(true));
        assertThat(Files.exists(targetFile), is(true));
        assertThat(FileUtils.readFileToString(targetFile.toFile(), StandardCharsets.UTF_8), is("test"));

        new Move(this.context)
            .sources(sourceFile)
            .target(targetFile)
            .force()
            .run();

        assertThat(Files.exists(sourceFile), is(false));
        assertThat(Files.exists(targetFile), is(true));
        assertThat(FileUtils.readFileToString(targetFile.toFile(), StandardCharsets.UTF_8), is("hello world"));
    }

    @Test
    public void fileToDir() throws Exception {
        final Path sourceFile = createFile(this.testMoveDir.resolve("fileToDir.txt"));
        final Path targetDir = createDir(this.testMoveDir.resolve("fileToDir"));
        final Path targetFile = targetDir.resolve(sourceFile.getFileName());

        assertThat(Files.exists(targetFile), is(false));

        new Move(this.context)
            .sources(sourceFile)
            .target(targetDir)
            .verbose()
            .run();

        assertThat(Files.exists(sourceFile), is(false));
        assertThat(Files.exists(targetFile), is(true));
    }

    @Test
    public void dirToDirFailsIfTargetExistsAsFile() throws Exception {
        // create a source directory with a file, a subdir, and the subdir with a file
        final Path sourceDir = createDir(this.testMoveDir.resolve("dirToDirFailsIfTargetExistsAsFile"));
        final Path sourceDirFile = createFile(sourceDir.resolve("test1.txt"));
        final Path targetFile = createFile(this.testMoveDir.resolve("dirToDirFailsIfTargetExistsAsFileToMoveTo"));

        assertThrows(BlazeException.class, () -> {
            new Move(this.context)
                .sources(sourceDir)
                .target(targetFile)
                .run();
        });
    }

    @Test
    public void dirToDirFailsIfTargetIsSameDirectory() throws Exception {
        // create a source directory with a file, a subdir, and the subdir with a file
        final Path sourceDir = createDir(this.testMoveDir.resolve("dirToDirFailsIfTargetIsSameDirectory"));
        final Path sourceDirFile = createFile(sourceDir.resolve("test1.txt"));

        assertThrows(BlazeException.class, () -> {
            new Move(this.context)
                .sources(sourceDir)
                .target(sourceDir)
                .run();
        });
    }

    @Test
    public void dirToDir() throws Exception {
        // create a source directory with a file, a subdir, and the subdir with a file
        final Path sourceDir = createDir(this.testMoveDir.resolve("dirToDir"));
        final Path sourceDirFile = createFile(sourceDir.resolve("test1.txt"));
        final Path sourceDirSubDir = createDir(sourceDir.resolve("subdir"));
        final Path sourceDirSubDirFile = createFile(sourceDir.resolve("subdir/test2.txt"));

        // create a non-existent directory we want to copy to
        final Path targetDir = this.testMoveDir.resolve("dirToDirCopyTo");
        FileUtils.deleteDirectory(targetDir.toFile());

        new Move(this.context)
            .sources(sourceDir)
            .target(targetDir)
            .run();

        assertThat(Files.exists(sourceDir), is(false));
        assertThat(Files.exists(targetDir), is(true));
        assertThat(Files.exists(targetDir.resolve("test1.txt")), is(true));
        assertThat(Files.exists(targetDir.resolve("subdir")), is(true));
        assertThat(Files.exists(targetDir.resolve("subdir/test2.txt")), is(true));
    }


    @Test
    public void globCopyFailsIfNoneFound() throws Exception {
        final Path sourceDir = createDir(this.testMoveDir.resolve("glob"));
        final Path targetDir = createDir(this.testMoveDir.resolve("globTo"));

        assertThrows(BlazeException.class, () -> {
            new Move(this.context)
                .sources(globber(sourceDir, "*.{java,js}"))
                .target(targetDir)
                .run();
        });
    }

    @Test
    public void globNoneFoundForced() throws Exception {
        final Path sourceDir = createDir(this.testMoveDir.resolve("glob"));
        final Path targetDir = createDir(this.testMoveDir.resolve("globTo"));

        new Move(this.context)
            .sources(globber(sourceDir, "*.{java,js}"))
            .target(targetDir)
            .force()
            .run();
    }

    @Test
    public void glob() throws Exception {
        final Path sourceDir = createDir(this.testMoveDir.resolve("glob"));
        final Path sourceDirJavaFile = createFile(sourceDir.resolve("test.java"));
        final Path sourceDirJsFile = createFile(sourceDir.resolve("test.js"));
        final Path sourceDirKtFile = createFile(sourceDir.resolve("test.kt"));
        final Path targetDir = createDir(this.testMoveDir.resolve("globTo"));

        new Move(this.context)
            .sources(globber(sourceDir, "*.{java,js}"))
            .target(targetDir)
            .run();

        assertThat(Files.exists(sourceDirJavaFile), is(false));
        assertThat(Files.exists(sourceDirJsFile), is(false));
        assertThat(Files.exists(sourceDirKtFile), is(true));
        assertThat(Files.exists(targetDir.resolve("test.java")), is(true));
        assertThat(Files.exists(targetDir.resolve("test.js")), is(true));
        assertThat(Files.exists(targetDir.resolve("test.kt")), is(false));  // should not be copied over
    }

    @Test
    public void globFileAndDir() throws Exception {
        final Path sourceDir = createDir(this.testMoveDir.resolve("globFileAndDir"));
        final Path sourceDirJavaFile = createFile(sourceDir.resolve("test.java"));
        final Path sourceDirSubDir = createDir(sourceDir.resolve("subdir"));
        final Path sourceDirSubDirKtFile = createFile(sourceDirSubDir.resolve("test.kt"));
        final Path targetDir = createDir(this.testMoveDir.resolve("globFileAndDirTo"));

        new Move(this.context)
            .sources(globber(sourceDir, "*"))
            .target(targetDir)
            .run();

        assertThat(Files.exists(sourceDirJavaFile), is(false));
        assertThat(Files.exists(sourceDirSubDir), is(false));
        assertThat(Files.isRegularFile(targetDir.resolve("test.java")), is(true));
        assertThat(Files.isDirectory(targetDir.resolve("subdir")), is(true));
        assertThat(Files.isRegularFile(targetDir.resolve("subdir/test.kt")), is(true));
    }

}
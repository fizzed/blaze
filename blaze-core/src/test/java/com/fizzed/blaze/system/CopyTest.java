package com.fizzed.blaze.system;

import com.fizzed.blaze.core.BlazeException;
import com.fizzed.blaze.core.DirectoryNotEmptyException;
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

public class CopyTest extends TestAbstractBase {

    Path testCopyDir;

    @BeforeEach
    public void setup() throws Exception {
        this.testCopyDir = this.createDir(targetDir.resolve("copy-test"));
    }

    @Test
    public void fileToFileFailsIfSourceDoesNotExist() {
        assertThrows(FileNotFoundException.class, () -> {
            new Copy(this.context)
                .sources(this.testCopyDir.resolve("notexist"))
                .target(this.testCopyDir)
                .run();
        });
    }

    @Test
    public void fileToFileFailsToSameFile() throws Exception {
        final Path sourceFile = createFile(this.testCopyDir.resolve("fileToFileFailsToSameFile.txt"));

        assertThrows(BlazeException.class, () -> {
            new Copy(this.context)
                .sources(sourceFile)
                .target(sourceFile)
                .run();
        });
    }

    @Test
    public void fileToFileFailsToSameFileEvenIfForced() throws Exception {
        final Path sourceFile = createFile(this.testCopyDir.resolve("fileToFileFailsToSameFileEvenIfForced.txt"));

        assertThrows(BlazeException.class, () -> {
            new Copy(this.context)
                .sources(sourceFile)
                .target(sourceFile)
                .force()
                .run();
        });
    }

    @Test
    public void fileToDirFailsToSameFile() throws Exception {
        final Path sourceFile = createFile(this.testCopyDir.resolve("fileToDirFailsToSameFile.txt"));

        assertThrows(BlazeException.class, () -> {
            new Copy(this.context)
                .sources(sourceFile)
                .target(sourceFile.getParent())
                .run();
        });
    }

    @Test
    public void fileToFile() throws Exception {
        final Path sourceFile = createFile(this.testCopyDir.resolve("fileToFile.txt"));
        final Path targetDir = createDir(this.testCopyDir.resolve("fileToFileDir"));
        final Path targetFile = targetDir.resolve(sourceFile.getFileName());

        assertThat(Files.exists(targetFile), is(false));

        new Copy(this.context)
            .sources(sourceFile)
            .target(targetFile)
            .run();

        assertThat(Files.exists(targetFile), is(true));
    }

    @Test
    public void fileToFileFailsIfAlreadyExists() throws Exception {
        final Path sourceFile = createFile(this.testCopyDir.resolve("fileToFileFailsIfAlreadyExists.txt"));
        final Path targetDir = createDir(this.testCopyDir.resolve("fileToFileFailsIfAlreadyExists"));
        final Path targetFile = createFile(targetDir.resolve("exists.txt"));

        assertThrows(BlazeException.class, () -> {
            new Copy(this.context)
                .sources(sourceFile)
                .target(targetFile)
                .run();
        });
    }

    @Test
    public void fileToFileForced() throws Exception {
        final Path sourceFile = createFile(this.testCopyDir.resolve("fileToFileFailsIfAlreadyExists.txt"), "hello world");
        final Path targetDir = createDir(this.testCopyDir.resolve("fileToFileFailsIfAlreadyExists"));
        final Path targetFile = createFile(targetDir.resolve("exists.txt"));

        assertThat(FileUtils.readFileToString(targetFile.toFile(), StandardCharsets.UTF_8), is("test"));

        new Copy(this.context)
            .sources(sourceFile)
            .target(targetFile)
            .force()
            .run();

        assertThat(FileUtils.readFileToString(targetFile.toFile(), StandardCharsets.UTF_8), is("hello world"));
    }

    @Test
    public void fileToDir() throws Exception {
        final Path sourceFile = createFile(this.testCopyDir.resolve("fileToDir.txt"));
        final Path targetDir = createDir(this.testCopyDir.resolve("fileToDir"));
        final Path targetFile = targetDir.resolve(sourceFile.getFileName());

        assertThat(Files.exists(targetFile), is(false));

        new Copy(this.context)
            .sources(sourceFile)
            .target(targetDir)
            .verbose()
            .run();

        assertThat(Files.exists(targetFile), is(true));
    }

    @Test
    public void dirToDirFailsIfTargetExistsAsFile() throws Exception {
        // create a source directory with a file, a subdir, and the subdir with a file
        final Path sourceDir = createDir(this.testCopyDir.resolve("dirToDirFailsIfTargetExistsAsFile"));
        final Path sourceDirFile = createFile(sourceDir.resolve("test1.txt"));

        final Path targetFile = createFile(this.testCopyDir.resolve("dirToDirFailsIfTargetExistsAsFileToCopyTo"));

        assertThrows(BlazeException.class, () -> {
            new Copy(this.context)
                .sources(sourceDir)
                .target(targetFile)
                .run();
        });
    }

    @Test
    public void dirToDirFailsIfTargetIsSameDirectory() throws Exception {
        // create a source directory with a file, a subdir, and the subdir with a file
        final Path sourceDir = createDir(this.testCopyDir.resolve("dirToDirFailsIfTargetIsSameDirectory"));
        final Path sourceDirFile = createFile(sourceDir.resolve("test1.txt"));

        assertThrows(BlazeException.class, () -> {
            new Copy(this.context)
                .sources(sourceDir)
                .target(sourceDir)
                .run();
        });
    }

    @Test
    public void dirToDirFailsIfNotRecursive() throws Exception {
        // create a source directory with a file, a subdir, and the subdir with a file
        final Path sourceDir = createDir(this.testCopyDir.resolve("dirToDir"));
        final Path sourceDirFile = createFile(sourceDir.resolve("test1.txt"));
        final Path sourceDirSubDir = createDir(sourceDir.resolve("subdir"));
        final Path sourceDirSubDirFile = createFile(sourceDirSubDir.resolve("test2.txt"));

        // create a non-existent directory we want to copy to
        final Path targetDir = this.testCopyDir.resolve("dirToDirTo");
        FileUtils.deleteDirectory(targetDir.toFile());

        assertThat(Files.exists(targetDir), is(false));

        assertThrows(DirectoryNotEmptyException.class, () -> {
            new Copy(this.context)
                .sources(sourceDir)
                .target(targetDir)
                .run();
        });
    }

    @Test
    public void dirToDir() throws Exception {
        // create a source directory with a file, a subdir, and the subdir with a file
        final Path sourceDir = createDir(this.testCopyDir.resolve("dirToDir"));
        final Path sourceDirFile = createFile(sourceDir.resolve("test1.txt"));
        final Path sourceDirSubDir = createDir(sourceDir.resolve("subdir"));
        final Path sourceDirSubDirFile = createFile(sourceDirSubDir.resolve("test2.txt"));

        // create a non-existent directory we want to copy to
        final Path targetDir = this.testCopyDir.resolve("dirToDirTo");
        FileUtils.deleteDirectory(targetDir.toFile());

        assertThat(Files.exists(targetDir), is(false));

        new Copy(this.context)
            .sources(sourceDir)
            .target(targetDir)
            .recursive()
            .run();

        assertThat(Files.isDirectory(targetDir), is(true));
        assertThat(Files.isRegularFile(targetDir.resolve("test1.txt")), is(true));
        assertThat(Files.isDirectory(targetDir.resolve("subdir")), is(true));
        assertThat(Files.isRegularFile(targetDir.resolve("subdir/test2.txt")), is(true));
    }

    @Test
    public void dirToDirFailsIfFileAlreadyExists() throws Exception {
        // create a source directory with a file, a subdir, and the subdir with a file
        final Path sourceDir = createDir(this.testCopyDir.resolve("dirToDirFailsIfFileAlreadyExists"));
        final Path sourceDirFile = createFile(sourceDir.resolve("test1.txt"));
        final Path sourceDirSubDir = createDir(sourceDir.resolve("subdir"), false);
        final Path sourceDirSubDirFile = createFile(sourceDirSubDir.resolve("test2.txt"));

        // create a non-existent directory we want to copy to
        final Path targetDir = createDir(this.testCopyDir.resolve("dirToDirFailsIfFileAlreadyExistsCopyTo"));
        final Path targetDirFile = createFile(targetDir.resolve("test1.txt"));
        // create a directory here that already exists
        createDir(targetDir.resolve("dirToDirFailsIfFileAlreadyExists"));

        assertThat(Files.exists(targetDirFile), is(true));

        assertThrows(BlazeException.class, () -> {
            new Copy(this.context)
                .sources(sourceDir)
                .target(targetDir)
                .recursive()
                .run();
        });
    }

    @Test
    public void globFailsIfNoneFound() throws Exception {
        final Path sourceDir = createDir(this.testCopyDir.resolve("globber"));
        final Path targetDir = createDir(this.testCopyDir.resolve("globberTo"));

        assertThrows(BlazeException.class, () -> {
            new Copy(this.context)
                .sources(globber(sourceDir, "*.{java,js}"))
                .target(targetDir)
                .run();
        });
    }

    @Test
    public void globNoneFoundForced() throws Exception {
        final Path sourceDir = createDir(this.testCopyDir.resolve("globber"));
        final Path targetDir = createDir(this.testCopyDir.resolve("globberTo"));

        new Copy(this.context)
            .sources(globber(sourceDir, "*.{java,js}"))
            .target(targetDir)
            .force()
            .run();
    }

    @Test
    public void glob() throws Exception {
        final Path sourceDir = createDir(this.testCopyDir.resolve("globber"));
        final Path sourceDirJavaFile = createFile(sourceDir.resolve("test.java"));
        final Path sourceDirJsFile = createFile(sourceDir.resolve("test.js"));
        final Path sourceDirKtFile = createFile(sourceDir.resolve("test.kt"));
        final Path targetDir = createDir(this.testCopyDir.resolve("globberTo"));

        new Copy(this.context)
            .sources(globber(sourceDir, "*.{java,js}"))
            .target(targetDir)
            .run();

        assertThat(Files.exists(targetDir.resolve("test.java")), is(true));
        assertThat(Files.exists(targetDir.resolve("test.js")), is(true));
        assertThat(Files.exists(targetDir.resolve("test.kt")), is(false));  // should not be copied over
    }

    @Test
    public void globFileAndDir() throws Exception {
        final Path sourceDir = createDir(this.testCopyDir.resolve("globberFileAndDir"));
        final Path sourceDirJavaFile = createFile(sourceDir.resolve("test.java"));
        final Path sourceDirSubDir = createDir(sourceDir.resolve("subdir"));
        final Path sourceDirSubDirKtFile = createFile(sourceDirSubDir.resolve("test.kt"));
        final Path targetDir = createDir(this.testCopyDir.resolve("globberFileAndDirTo"));

        new Copy(this.context)
            .sources(globber(sourceDir, "*"))
            .target(targetDir)
            .recursive()
            .run();

        assertThat(Files.isRegularFile(targetDir.resolve("test.java")), is(true));
        assertThat(Files.isDirectory(targetDir.resolve("subdir")), is(true));
        assertThat(Files.isRegularFile(targetDir.resolve("subdir/test.kt")), is(true));
    }

}
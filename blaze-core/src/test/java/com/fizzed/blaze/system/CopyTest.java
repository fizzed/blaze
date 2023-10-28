package com.fizzed.blaze.system;

import com.fizzed.blaze.core.BlazeException;
import com.fizzed.blaze.core.DirectoryNotEmptyException;
import com.fizzed.blaze.core.FileNotFoundException;
import org.apache.commons.io.FileUtils;
import org.junit.Before;
import org.junit.Test;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static com.fizzed.blaze.util.Globber.globber;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class CopyTest extends TestAbstractBase {

    Path testCopyDir;

    @Before
    public void setup() throws Exception {
        this.testCopyDir = targetDir.resolve("copy-test");
        Files.createDirectories(this.testCopyDir);
    }

    @Test(expected=FileNotFoundException.class)
    public void fileToFileFailsIfSourceDoesNotExist() {
        new Copy(this.context)
            .source(this.testCopyDir.resolve("notexist"))
            .target(this.testCopyDir)
            .run();
    }

    @Test(expected= BlazeException.class)
    public void fileToFileCopyFailsToSameFile() throws Exception {
        final Path sourceFile = createFile(this.testCopyDir.resolve("fileToFileCopyFailsToSameFile.txt"));

        new Copy(this.context)
            .source(sourceFile)
            .target(sourceFile)
            .run();
    }

    @Test(expected= BlazeException.class)
    public void fileToFileCopyFailsToSameFileEvenIfForced() throws Exception {
        final Path sourceFile = createFile(this.testCopyDir.resolve("fileToFileCopyFailsToSameFileEvenIfForced.txt"));

        new Copy(this.context)
            .source(sourceFile)
            .target(sourceFile)
            .force()
            .run();
    }

    @Test(expected= BlazeException.class)
    public void fileToDirCopyFailsToSameFile() throws Exception {
        final Path sourceFile = createFile(this.testCopyDir.resolve("fileToDirCopyFailsToSameFile.txt"));

        new Copy(this.context)
            .source(sourceFile)
            .target(sourceFile.getParent())
            .run();
    }

    @Test
    public void fileToFileCopy() throws Exception {
        final Path sourceFile = createFile(this.testCopyDir.resolve("fileToFileCopy.txt"));
        final Path targetDir = createDir(this.testCopyDir.resolve("fileToFileCopyDir"));
        final Path targetFile = targetDir.resolve(sourceFile.getFileName());

        assertThat(Files.exists(targetFile), is(false));

        new Copy(this.context)
            .source(sourceFile)
            .target(targetFile)
            .run();

        assertThat(Files.exists(targetFile), is(true));
    }

    @Test(expected= BlazeException.class)
    public void fileToFileCopyFailsIfAlreadyExists() throws Exception {
        final Path sourceFile = createFile(this.testCopyDir.resolve("fileToFileCopyFailsIfAlreadyExists.txt"));
        final Path targetDir = createDir(this.testCopyDir.resolve("fileToFileCopyFailsIfAlreadyExists"));
        final Path targetFile = createFile(targetDir.resolve("exists.txt"));

        new Copy(this.context)
            .source(sourceFile)
            .target(targetFile)
            .run();
    }

    @Test
    public void fileToFileCopyForced() throws Exception {
        final Path sourceFile = createFile(this.testCopyDir.resolve("fileToFileCopyFailsIfAlreadyExists.txt"), "hello world");
        final Path targetDir = createDir(this.testCopyDir.resolve("fileToFileCopyFailsIfAlreadyExists"));
        final Path targetFile = createFile(targetDir.resolve("exists.txt"));

        assertThat(FileUtils.readFileToString(targetFile.toFile(), StandardCharsets.UTF_8), is("test"));

        new Copy(this.context)
            .source(sourceFile)
            .target(targetFile)
            .force()
            .run();

        assertThat(FileUtils.readFileToString(targetFile.toFile(), StandardCharsets.UTF_8), is("hello world"));
    }

    @Test
    public void fileToDirCopy() throws Exception {
        final Path sourceFile = createFile(this.testCopyDir.resolve("fileToDirCopy.txt"));
        final Path targetDir = createDir(this.testCopyDir.resolve("fileToDirCopy"));
        final Path targetFile = targetDir.resolve(sourceFile.getFileName());

        assertThat(Files.exists(targetFile), is(false));

        new Copy(this.context)
            .source(sourceFile)
            .target(targetDir)
            .verbose()
            .run();

        assertThat(Files.exists(targetFile), is(true));
    }

    @Test(expected=BlazeException.class)
    public void dirToDirCopyFailsIfTargetExistsAsFile() throws Exception {
        // create a source directory with a file, a subdir, and the subdir with a file
        final Path sourceDir = createDir(this.testCopyDir.resolve("dirToDirCopyFailsIfTargetExistsAsFile"));
        final Path sourceDirFile = createFile(sourceDir.resolve("test1.txt"));

        final Path targetFile = createFile(this.testCopyDir.resolve("dirToDirCopyFailsIfTargetExistsAsFileToCopyTo"));

        new Copy(this.context)
            .source(sourceDir)
            .target(targetFile)
            .run();
    }

    @Test(expected=BlazeException.class)
    public void dirToDirCopyFailsIfTargetIsSameDirectory() throws Exception {
        // create a source directory with a file, a subdir, and the subdir with a file
        final Path sourceDir = createDir(this.testCopyDir.resolve("dirToDirCopyFailsIfTargetIsSameDirectory"));
        final Path sourceDirFile = createFile(sourceDir.resolve("test1.txt"));

        new Copy(this.context)
            .source(sourceDir)
            .target(sourceDir)
            .run();
    }

    @Test(expected=DirectoryNotEmptyException.class)
    public void dirToDirCopyFailsIfNotRecursive() throws Exception {
        // create a source directory with a file, a subdir, and the subdir with a file
        final Path sourceDir = createDir(this.testCopyDir.resolve("dirToDirCopy"));
        final Path sourceDirFile = createFile(sourceDir.resolve("test1.txt"));
        final Path sourceDirSubDir = createDir(sourceDir.resolve("subdir"));
        final Path sourceDirSubDirFile = createFile(sourceDirSubDir.resolve("test2.txt"));

        // create a non-existent directory we want to copy to
        final Path targetDir = this.testCopyDir.resolve("dirToDirCopyTo");
        FileUtils.deleteDirectory(targetDir.toFile());

        assertThat(Files.exists(targetDir), is(false));

        new Copy(this.context)
            .source(sourceDir)
            .target(targetDir)
            .run();
    }

    @Test
    public void dirToDirCopy() throws Exception {
        // create a source directory with a file, a subdir, and the subdir with a file
        final Path sourceDir = createDir(this.testCopyDir.resolve("dirToDirCopy"));
        final Path sourceDirFile = createFile(sourceDir.resolve("test1.txt"));
        final Path sourceDirSubDir = createDir(sourceDir.resolve("subdir"));
        final Path sourceDirSubDirFile = createFile(sourceDirSubDir.resolve("test2.txt"));

        // create a non-existent directory we want to copy to
        final Path targetDir = this.testCopyDir.resolve("dirToDirCopyTo");
        FileUtils.deleteDirectory(targetDir.toFile());

        assertThat(Files.exists(targetDir), is(false));

        new Copy(this.context)
            .source(sourceDir)
            .target(targetDir)
            .recursive()
            .run();

        assertThat(Files.isDirectory(targetDir), is(true));
        assertThat(Files.isRegularFile(targetDir.resolve("test1.txt")), is(true));
        assertThat(Files.isDirectory(targetDir.resolve("subdir")), is(true));
        assertThat(Files.isRegularFile(targetDir.resolve("subdir/test2.txt")), is(true));
    }

    @Test(expected=BlazeException.class)
    public void dirToDirCopyFailsIfFileAlreadyExists() throws Exception {
        // create a source directory with a file, a subdir, and the subdir with a file
        final Path sourceDir = createDir(this.testCopyDir.resolve("dirToDirCopyFailsIfFileAlreadyExists"));
        final Path sourceDirFile = createFile(sourceDir.resolve("test1.txt"));
        final Path sourceDirSubDir = createDir(sourceDir.resolve("subdir"), false);
        final Path sourceDirSubDirFile = createFile(sourceDirSubDir.resolve("test2.txt"));

        // create a non-existent directory we want to copy to
        final Path targetDir = createDir(this.testCopyDir.resolve("dirToDirCopyFailsIfFileAlreadyExistsCopyTo"));
        final Path targetDirFile = createFile(targetDir.resolve("test1.txt"));
        // create a directory here that already exists
        createDir(targetDir.resolve("dirToDirCopyFailsIfFileAlreadyExists"));

        assertThat(Files.exists(targetDirFile), is(true));

        new Copy(this.context)
            .source(sourceDir)
            .target(targetDir)
            .recursive()
            .run();
    }

    @Test(expected=BlazeException.class)
    public void globberCopyFailsIfNoneFound() throws Exception {
        final Path sourceDir = createDir(this.testCopyDir.resolve("globberCopy"));
        final Path targetDir = createDir(this.testCopyDir.resolve("globberCopyto"));

        new Copy(this.context)
            .sources(globber(sourceDir, "*.{java,js}"))
            .target(targetDir)
            .run();
    }

    @Test
    public void globberCopyNoneFoundForced() throws Exception {
        final Path sourceDir = createDir(this.testCopyDir.resolve("globberCopy"));
        final Path targetDir = createDir(this.testCopyDir.resolve("globberCopyto"));

        new Copy(this.context)
            .sources(globber(sourceDir, "*.{java,js}"))
            .target(targetDir)
            .force()
            .run();
    }

    @Test
    public void globberCopy() throws Exception {
        final Path sourceDir = createDir(this.testCopyDir.resolve("globberCopy"));
        final Path sourceDirJavaFile = createFile(sourceDir.resolve("test.java"));
        final Path sourceDirJsFile = createFile(sourceDir.resolve("test.js"));
        final Path sourceDirKtFile = createFile(sourceDir.resolve("test.kt"));
        final Path targetDir = createDir(this.testCopyDir.resolve("globberCopyTo"));

        new Copy(this.context)
            .sources(globber(sourceDir, "*.{java,js}"))
            .target(targetDir)
            .run();

        assertThat(Files.exists(targetDir.resolve("test.java")), is(true));
        assertThat(Files.exists(targetDir.resolve("test.js")), is(true));
        assertThat(Files.exists(targetDir.resolve("test.kt")), is(false));  // should not be copied over
    }

    @Test
    public void globberFileAndDirCopy() throws Exception {
        final Path sourceDir = createDir(this.testCopyDir.resolve("globberFileAndDirCopy"));
        final Path sourceDirJavaFile = createFile(sourceDir.resolve("test.java"));
        final Path sourceDirSubDir = createDir(sourceDir.resolve("subdir"));
        final Path sourceDirSubDirKtFile = createFile(sourceDirSubDir.resolve("test.kt"));
        final Path targetDir = createDir(this.testCopyDir.resolve("globberFileAndDirCopyTo"));

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
package com.fizzed.blaze.system;

import com.fizzed.blaze.core.DirectoryNotEmptyException;
import com.fizzed.blaze.core.FileNotFoundException;
import org.junit.Before;
import org.junit.Test;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class RemoveTest extends TestAbstractBase {

    private Path testRemoveDir;

    @Before
    public void setup() throws Exception {
        this.testRemoveDir = this.targetDir.resolve("remove-test");
        Files.createDirectories(this.testRemoveDir);
    }

    @Test(expected=FileNotFoundException.class)
    public void removeFileNotFound() {
        new Remove(this.context)
            .path(this.testRemoveDir.resolve("notexist"))
            .run();
    }

    @Test
    public void removeForceFileNotFound() {
        new Remove(this.context)
            .path(this.testRemoveDir.resolve("notexist"))
            .force()
            .run();
    }

    @Test
    public void removeFile() throws Exception {
        final Path file = this.createFile(this.testRemoveDir.resolve("removeFile.txt"));

        assertThat(Files.exists(file), is(true));

        new Remove(this.context)
            .path(file)
            .run();

        assertThat(Files.exists(file), is(false));
    }

    @Test(expected=DirectoryNotEmptyException.class)
    public void removeDirRecursiveDisabled() throws Exception {
        final Path dir = this.createDir(this.testRemoveDir.resolve("removeDirRecursiveDisabled"));
        final Path file = this.createFile(dir.resolve("test.txt"));

        assertThat(Files.exists(file), is(true));

        new Remove(this.context)
            .path(dir)
            .run();

        assertThat(Files.exists(file), is(false));
    }

    @Test
    public void removeDirRecursive() throws Exception {
        final Path dir = this.createDir(this.testRemoveDir.resolve("removeDirRecursive"));
        final Path file = this.createFile(dir.resolve("test.txt"));

        assertThat(Files.exists(dir), is(true));
        assertThat(Files.exists(file), is(true));

        new Remove(this.context)
            .path(dir)
            .recursive()
            .run();

        assertThat(Files.exists(file), is(false));
        assertThat(Files.exists(dir), is(false));
    }

    @Test(expected=FileNotFoundException.class)
    public void removeDirRecursiveNotExists() throws Exception {
        final Path dir = this.testRemoveDir.resolve("dirnotexist");

        assertThat(Files.exists(dir), is(false));

        new Remove(this.context)
            .path(dir)
            .recursive()
            .run();

        assertThat(Files.exists(dir), is(false));
    }

    @Test
    public void removeDirRecursiveForcedNotExists() throws Exception {
        final Path dir = this.testRemoveDir.resolve("dirnotexist");

        assertThat(Files.exists(dir), is(false));

        new Remove(this.context)
            .path(dir)
            .recursive()
            .force()
            .run();

        assertThat(Files.exists(dir), is(false));
    }

}
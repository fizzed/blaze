package com.fizzed.blaze.system;

import com.fizzed.blaze.core.DirectoryNotEmptyException;
import com.fizzed.blaze.core.FileNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class RemoveTest extends TestAbstractBase {

    private Path testRemoveDir;

    @BeforeEach
    public void setup() throws Exception {
        this.testRemoveDir = this.createDir(this.targetDir.resolve("remove-test"));
    }

    @Test
    public void removeFileNotFound() {
        assertThrows(FileNotFoundException.class, () -> {
            new Remove(this.context)
                .path(this.testRemoveDir.resolve("notexist"))
                .run();
        });
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

    @Test
    public void removeDirRecursiveDisabled() throws Exception {
        assertThrows(DirectoryNotEmptyException.class, () -> {
            final Path dir = this.createDir(this.testRemoveDir.resolve("removeDirRecursiveDisabled"));
            final Path file = this.createFile(dir.resolve("test.txt"));

            assertThat(Files.exists(file), is(true));

            new Remove(this.context)
                .path(dir)
                .run();

            assertThat(Files.exists(file), is(false));
        });
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

    @Test
    public void removeDirRecursiveNotExists() throws Exception {
        assertThrows(FileNotFoundException.class, () -> {
            final Path dir = this.testRemoveDir.resolve("dirnotexist");

            assertThat(Files.exists(dir), is(false));

            new Remove(this.context)
                .path(dir)
                .recursive()
                .run();

            assertThat(Files.exists(dir), is(false));
        });
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
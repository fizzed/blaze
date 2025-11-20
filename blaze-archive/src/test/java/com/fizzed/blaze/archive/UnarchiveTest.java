package com.fizzed.blaze.archive;

import com.fizzed.blaze.Config;
import com.fizzed.blaze.core.BlazeException;
import com.fizzed.blaze.internal.ConfigHelper;
import com.fizzed.blaze.internal.ContextImpl;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import com.fizzed.crux.util.Resources;
import org.junit.jupiter.api.condition.DisabledIf;
import org.junit.jupiter.api.condition.DisabledIfSystemProperty;
import org.junit.jupiter.api.condition.DisabledOnOs;
import org.junit.jupiter.api.condition.OS;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.core.Is.is;
import static org.mockito.Mockito.spy;

public class UnarchiveTest {

    Config config;
    ContextImpl context;
    Path targetDir;

    @BeforeEach
    public void setup() throws Exception {
        config = ConfigHelper.createEmpty();
        context = spy(new ContextImpl(null, null, Paths.get("blaze.java"), config));
        targetDir = Resources.file("/fixtures/sample-no-root-dir.zip").resolve("../../../../target").toAbsolutePath().normalize();
    }

    protected Path createEmptyTargetDir(String path) throws IOException {
        final Path target = targetDir.resolve(path);
        FileUtils.deleteQuietly(target.toFile());
        Files.createDirectories(target);
        return target;
    }

    @Test
    public void zipNoRootDir() throws Exception {
        final Path file = Resources.file("/fixtures/sample-no-root-dir.zip");
        final Path target = this.createEmptyTargetDir("zipNoRootDir");

        new Unarchive(this.context, file)
            .target(target)
            .run();

        assertThat(Files.exists(target.resolve("a.txt")), is(true));
        assertThat(Files.exists(target.resolve("c/d.txt")), is(true));
    }

    @Test
    public void stripComponents1() throws Exception {
        final Path file = Resources.file("/fixtures/sample-with-root-dir.zip");
        final Path target = this.createEmptyTargetDir("zipWithRootDir");

        new Unarchive(this.context, file)
            .target(target)
            .stripLeadingPath()
            .run();

        assertThat(Files.exists(target.resolve("a.txt")), is(true));
        assertThat(Files.exists(target.resolve("c/d.txt")), is(true));
    }

    @Test
    public void stripComponents2() throws Exception {
        final Path file = Resources.file("/fixtures/sample-with-root-dir.zip");
        final Path target = this.createEmptyTargetDir("zipWithRootDir");

        new Unarchive(this.context, file)
            .target(target)
            .stripComponents(2)
            .run();

        assertThat(Files.exists(target.resolve("a.txt")), is(true));
        assertThat(Files.exists(target.resolve("d.txt")), is(true));
    }

    @Test
    public void doNotOverwrite() throws Exception {
        final Path file = Resources.file("/fixtures/sample-with-root-dir.zip");
        final Path target = this.createEmptyTargetDir("zipWithRootDir");

        new Unarchive(this.context, file)
            .target(target)
            .run();

        // run it again (should fail)
        try {
            new Unarchive(this.context, file)
                .target(target)
                .run();
        } catch (BlazeException e) {
            assertThat(e.getMessage(), containsString(".force()"));
        }

        new Unarchive(this.context, file)
            .target(target)
            .force()
            .run();

        assertThat(Files.exists(target.resolve("sample/a.txt")), is(true));
        assertThat(Files.exists(target.resolve("sample/c/d.txt")), is(true));
    }

    @Test
    public void tarNoRootDir() throws Exception {
        final Path file = Resources.file("/fixtures/sample-no-root-dir.tar");
        final Path target = this.createEmptyTargetDir("tarNoRootDir");

        new Unarchive(this.context, file)
            .target(target)
            .run();

        assertThat(Files.exists(target.resolve("a.txt")), is(true));
        assertThat(Files.exists(target.resolve("c/d.txt")), is(true));
    }

    @Test
    public void tarGzNoRootDir() throws Exception {
        final Path file = Resources.file("/fixtures/sample-no-root-dir.tar.gz");
        final Path target = this.createEmptyTargetDir("tarGzNoRootDir");

        new Unarchive(this.context, file)
            .target(target)
            .run();

        assertThat(Files.exists(target.resolve("a.txt")), is(true));
        assertThat(Files.exists(target.resolve("c/d.txt")), is(true));
    }

    @Test
    public void tarBz2NoRootDir() throws Exception {
        final Path file = Resources.file("/fixtures/sample-no-root-dir.tar.bz2");
        final Path target = this.createEmptyTargetDir("tarBz2NoRootDir");

        new Unarchive(this.context, file)
            .target(target)
            .run();

        assertThat(Files.exists(target.resolve("a.txt")), is(true));
        assertThat(Files.exists(target.resolve("c/d.txt")), is(true));
    }

    @Test
    public void tarXzNoRootDir() throws Exception {
        final Path file = Resources.file("/fixtures/sample-no-root-dir.tar.xz");
        final Path target = this.createEmptyTargetDir("tarXzNoRootDir");

        new Unarchive(this.context, file)
            .target(target)
            .run();

        assertThat(Files.exists(target.resolve("a.txt")), is(true));
        assertThat(Files.exists(target.resolve("c/d.txt")), is(true));
    }

    @Test
    //@DisabledOnOs({OS.OPENBSD, OS.FREEBSD}) @DisabledIfSystemProperty(named="os.arch", matches="riscv64")
    public void tarZstNoRootDir() throws Exception {
        final Path file = Resources.file("/fixtures/sample-no-root-dir.tar.zst");
        final Path target = this.createEmptyTargetDir("tarZstNoRootDir");

        new Unarchive(this.context, file)
            .target(target)
            .run();

        assertThat(Files.exists(target.resolve("a.txt")), is(true));
        assertThat(Files.exists(target.resolve("c/d.txt")), is(true));
    }

    @Test
    public void _7zWithRootDir() throws Exception {
        final Path file = Resources.file("/fixtures/sample-with-root-dir.7z");
        final Path target = this.createEmptyTargetDir("_7zWithRootDir");

        new Unarchive(this.context, file)
            .target(target)
            .run();

        assertThat(Files.exists(target.resolve("sample/a.txt")), is(true));
        assertThat(Files.exists(target.resolve("sample/c/d.txt")), is(true));
    }

    @Test
    public void gzFileOnly() throws Exception {
        final Path file = Resources.file("/fixtures/hello.txt.gz");
        final Path target = this.createEmptyTargetDir("gzFileOnly");

        new Unarchive(this.context, file)
            .target(target)
            .run();

        assertThat(Files.exists(target.resolve("hello.txt")), is(true));
    }

    @Test
    //@DisabledOnOs({OS.OPENBSD, OS.FREEBSD}) @DisabledIfSystemProperty(named="os.arch", matches="riscv64")
    public void zstdFileOnly() throws Exception {
        final Path file = Resources.file("/fixtures/hello.txt.zst");
        final Path target = this.createEmptyTargetDir("zstdFileOnly");

        new Unarchive(this.context, file)
            .target(target)
            .run();

        assertThat(Files.exists(target.resolve("hello.txt")), is(true));
    }

}
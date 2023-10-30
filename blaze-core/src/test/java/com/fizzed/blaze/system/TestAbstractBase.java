package com.fizzed.blaze.system;

import com.fizzed.blaze.Config;
import com.fizzed.blaze.internal.ConfigHelper;
import com.fizzed.blaze.internal.ContextImpl;
import com.fizzed.blaze.internal.FileHelper;
import org.apache.commons.io.FileUtils;
import org.junit.Before;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

import static org.mockito.Mockito.spy;

abstract public class TestAbstractBase {


    protected final Logger log = LoggerFactory.getLogger(TestAbstractBase.class);
    protected Config config;
    protected ContextImpl context;
    protected Path targetDir;

    protected Path createDir(Path path) throws IOException {
        return this.createDir(path, false);
    }

    protected Path createDir(Path path, boolean parents) throws IOException {
        FileUtils.deleteDirectory(path.toFile());
        if (parents) {
            Files.createDirectories(path);
        } else {
            Files.createDirectory(path);
        }
        return path;
    }

    protected Path createFile(Path path) throws IOException {
        return createFile(path, "test");
    }

    protected Path createFile(Path path, String text) throws IOException {
        Files.write(path, text.getBytes(StandardCharsets.UTF_8), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        return path;
    }

    @Before
    public void baseBefore() throws Exception {
        this.config = ConfigHelper.create(null);
        this.context = spy(new ContextImpl(null, null, Paths.get("blaze.java"), config));
        // this will help find the compile target directory, where is should be in target/test-classes
        this.targetDir = FileHelper.resourceAsPath("/fixtures/resource-locator.txt").resolve("../../..").normalize();
    }

}
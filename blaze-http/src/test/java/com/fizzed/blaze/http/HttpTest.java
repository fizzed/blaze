package com.fizzed.blaze.http;

import com.fizzed.blaze.Config;
import com.fizzed.blaze.internal.ConfigHelper;
import com.fizzed.blaze.internal.ContextImpl;
import org.junit.Before;
import org.junit.Test;

import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.Assert.*;
import static org.mockito.Mockito.spy;

public class HttpTest {

    Config config;
    ContextImpl context;
    Path targetDir;

    @Before
    public void setup() throws Exception {
        config = ConfigHelper.create(null);
        context = spy(new ContextImpl(null, null, Paths.get("blaze.java"), config));
        //targetDir = Resources.file("/fixtures/sample-no-root-dir.zip").resolve("../../../../target").toAbsolutePath().normalize();
    }

    @Test
    public void get() {
        new Http(context).get("http://example.com")
            .verbose()
            .run();
    }

    @Test
    public void postForm() {
        new Http(context).post("http://example.com")
            .verbose()
            .form("a", "b")
            .run();
    }

}
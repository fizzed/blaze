/*
 * Copyright 2015 Fizzed, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.fizzed.blaze.system;

import com.fizzed.blaze.Config;
import com.fizzed.blaze.internal.ContextImpl;
import static com.fizzed.blaze.system.ShellTestHelper.getBinDirAsResource;
import com.fizzed.blaze.internal.ConfigHelper;
import com.fizzed.blaze.internal.FileHelper;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import static org.hamcrest.CoreMatchers.either;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;
import static org.junit.Assume.assumeTrue;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import static org.mockito.Mockito.spy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author joelauer
 */
public class WhichTest {
    private static final Logger log = LoggerFactory.getLogger(WhichTest.class);
    
    Config config;
    ContextImpl context;
    
    @Before
    public void setup() {
        config = ConfigHelper.createEmpty();
        context = spy(new ContextImpl(null, null, Paths.get("blaze.js"), config));
    }
    
    @Test
    public void notFind() throws Exception {
        Path f = new Which(context)
            .command("thisdoesnotexist")
            .run();

        assertThat(f, is(nullValue()));
    }
    
    @Test
    public void worksOnWindows() throws Exception {
        assumeTrue("Test only valid on windows", ConfigHelper.OperatingSystem.windows());

        Path f = new Which(context)
            .command("cmd")
            .run();
        
        log.debug("which: {}", f);

        assertThat(f, is(not(nullValue())));
        assertThat(Files.isRegularFile(f), is(true));
        assertThat(Files.isExecutable(f), is(true));
    }

    @Test
    public void worksOnWindowsForPs1() throws Exception {
        assumeTrue("Test only valid on windows", ConfigHelper.OperatingSystem.windows());

        Path f = new Which(context)
            .command("hello-world-test2")
            .path(getBinDirAsResource())
            .run();

        log.debug("which: {}", f);

        assertThat(f, is(not(nullValue())));
        // .ps1 should be preferred to .bat
        assertThat(f.getFileName().toString(), either(is("hello-world-test2")).or(is("hello-world-test2.ps1")));
    }
    
    @Test
    public void worksOnUnix() throws Exception {
        assumeTrue("Test only valid on unix or mac", ConfigHelper.OperatingSystem.unix() || ConfigHelper.OperatingSystem.mac());

        Path f = new Which(context)
            .command("ls")
            .run();
        
        log.debug("which: {}", f);

        assertThat(f, is(not(nullValue())));
        assertThat(Files.isRegularFile(f), is(true));
        assertThat(Files.isExecutable(f), is(true));
    }
    
    @Test
    public void worksWithPath() throws Exception {
        Path f = new Which(context)
            .command("hello-world-test")
            .run();
        
        assertThat(f, is(nullValue()));
        
        f = new Which(context)
            .command("hello-world-test")
            .path(getBinDirAsResource())
            .run();

        assertThat(f, is(not(nullValue())));
        assertThat(Files.isRegularFile(f), is(true));
        assertThat(Files.isExecutable(f), is(true));
        assertThat(f.getFileName().toString(), either(is("hello-world-test")).or(is("hello-world-test.bat")));
    }
    
    @Test
    public void worksWithAbsolutePath() throws Exception {
        Path exeFile = FileHelper.resourceAsFile("/bin/hello-world-test.bat").toPath().toAbsolutePath();

        // for some reason, this file is NOT executable in CI
        exeFile.toFile().setExecutable(true);

        Path f = new Which(context)
            .command(exeFile)
            .run();
        
        assertThat(f, is(not(nullValue())));
        assertThat(f.toAbsolutePath(), is(exeFile));
    }
    
    @Test
    public void excludeDirs() throws Exception {
        Path exeFile = FileHelper.resourceAsPath("/bin/hello-world-test.bat");
        Path binDir = exeFile.getParent();
        Path resourcesDir = binDir.getParent();
        
        Path f = new Which(context)
            .path(resourcesDir)
            .command("bin")
            .run();
        
        // nothing should have been found here
        assertThat(f, is(nullValue()));
    }
    
}

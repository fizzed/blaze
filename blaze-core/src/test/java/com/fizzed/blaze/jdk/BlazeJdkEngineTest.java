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
package com.fizzed.blaze.jdk;

import com.fizzed.blaze.Context;
import com.fizzed.blaze.core.Blaze;
import com.fizzed.blaze.core.BlazeTask;
import com.fizzed.blaze.internal.ConfigHelper;
import com.fizzed.blaze.internal.ContextImpl;
import static com.fizzed.blaze.system.ShellTestHelper.getBinDirAsResource;
import static com.fizzed.blaze.internal.FileHelper.resourceAsPath;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import org.apache.commons.io.FileUtils;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertThat;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.contrib.java.lang.system.SystemOutRule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author joelauer
 */
public class BlazeJdkEngineTest {
    final static private Logger log = LoggerFactory.getLogger(BlazeJdkEngineTest.class);
    
    @Rule
    public final SystemOutRule systemOutRule = new SystemOutRule().enableLog();
    
    @BeforeClass
    static public void forceBinResourceExecutable() throws Exception {
        // this makes the files in the "bin" sample directory executable
        getBinDirAsResource();
    }
    
    @BeforeClass
    static public void clearCache() throws IOException {
        Context context = new ContextImpl(null, Paths.get(System.getProperty("user.home")), null, null);
        Path classesDir = ConfigHelper.userBlazeEngineDir(context, "java");
        FileUtils.deleteDirectory(classesDir.toFile());
    }
    
    @Test
    public void hello() throws Exception {
        Blaze blaze = new Blaze.Builder()
            .file(resourceAsPath("/jdk/hello.java"))
            .build();
        
        systemOutRule.clearLog();
        
        blaze.execute();
        
        assertThat(systemOutRule.getLog(), containsString("Hello World!"));
    }
    
    @Test
    public void tasks() throws Exception {
        Blaze blaze = new Blaze.Builder()
            .file(resourceAsPath("/jdk/only_public.java"))
            .build();
        
        systemOutRule.clearLog();
        
        List<BlazeTask> tasks = blaze.tasks();
        
        assertThat(tasks, hasSize(1));
        assertThat(tasks.get(0).getName(), is("main"));
    }
    
    @Test
    public void defaultBlazeInWorkingDir() throws Exception {
        Blaze blaze = new Blaze.Builder()
            .directory(resourceAsPath("/jdk/project0"))
            .build();
        
        systemOutRule.clearLog();
        
        blaze.execute();
        
        assertThat(systemOutRule.getLog(), containsString("worked"));
        
        assertThat(blaze.context().scriptFile(), is(resourceAsPath("/jdk/project0/blaze.java")));
        assertThat(blaze.context().baseDir(), is(resourceAsPath("/jdk/project0")));
        assertThat(blaze.context().withBaseDir("test"), is(resourceAsPath("/jdk/project0").resolve("test")));
    }
    
    @Test
    public void defaultBlazeInSubBlazeDir() throws Exception {
        Blaze blaze = new Blaze.Builder()
            .directory(resourceAsPath("/jdk/project1"))
            .build();
        
        systemOutRule.clearLog();
        
        blaze.execute();
        
        assertThat(systemOutRule.getLog(), containsString("worked"));
        
        assertThat(blaze.context().scriptFile(), is(resourceAsPath("/jdk/project1/blaze/blaze.js")));
        assertThat(blaze.context().baseDir(), is(resourceAsPath("/jdk/project1/blaze")));
        assertThat(blaze.context().withBaseDir("../test"), is(resourceAsPath("/jdk/project1").resolve("test")));
    }
    
    @Test
    public void defaultBlazeInSubDotBlazeDir() throws Exception {
        Blaze blaze = new Blaze.Builder()
            .directory(resourceAsPath("/jdk/project2"))
            .build();
        
        systemOutRule.clearLog();
        
        blaze.execute();
        
        assertThat(systemOutRule.getLog(), containsString("worked"));
        
        assertThat(blaze.context().scriptFile(), is(resourceAsPath("/jdk/project2/.blaze/blaze.js")));
        assertThat(blaze.context().baseDir(), is(resourceAsPath("/jdk/project2/.blaze")));
        assertThat(blaze.context().withBaseDir("../test"), is(resourceAsPath("/jdk/project2").resolve("test")));
    }
    
}

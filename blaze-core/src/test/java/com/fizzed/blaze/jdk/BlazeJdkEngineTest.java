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
import com.fizzed.blaze.internal.ConfigHelper;
import com.fizzed.blaze.internal.ContextImpl;
import com.fizzed.blaze.util.BlazeRunner;
import org.apache.commons.io.FileUtils;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.contrib.java.lang.system.SystemOutRule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zeroturnaround.exec.ProcessResult;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import static com.fizzed.blaze.internal.FileHelper.resourceAsFile;
import static com.fizzed.blaze.system.ShellTestHelper.getBinDirAsResource;
import static java.util.Arrays.asList;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

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
        final File scriptFile = resourceAsFile("/jdk/hello.java");

        final ProcessResult result = BlazeRunner.invokeWithCurrentJvmHome(scriptFile, null, null);

        assertThat(result.getExitValue(), is(0));
        assertThat(result.outputUTF8(), containsString("Hello World!\n"));
    }
    
    @Test
    public void tasks() throws Exception {
        final File scriptFile = resourceAsFile("/jdk/only_public.java");

        final ProcessResult result = BlazeRunner.invokeWithCurrentJvmHome(scriptFile, asList("-l"), null);

        assertThat(result.getExitValue(), is(0));
        assertThat(result.outputUTF8(), containsString("tasks =>\n" +
            " main"));
    }
    
    @Test
    public void defaultBlazeInWorkingDir() throws Exception {
        final File workingDir = resourceAsFile("/jdk/project0");
        final File scriptFile = resourceAsFile("/jdk/project0/blaze.java");

        final ProcessResult result = BlazeRunner.invokeWithCurrentJvmHome(scriptFile, null, null, workingDir);

        assertThat(result.getExitValue(), is(0));
        assertThat(result.outputUTF8(), containsString("worked\n"));
    }
    
    @Test
    public void defaultBlazeInSubBlazeDir() throws Exception {
        final File workingDir = resourceAsFile("/jdk/project1");

        final ProcessResult result = BlazeRunner.invokeWithCurrentJvmHome(null, null, null, workingDir);

        assertThat(result.getExitValue(), is(0));
        assertThat(result.outputUTF8(), containsString("worked\n"));
    }
    
    @Test
    public void defaultBlazeInSubDotBlazeDir() throws Exception {
        final File workingDir = resourceAsFile("/jdk/project2");

        final ProcessResult result = BlazeRunner.invokeWithCurrentJvmHome(null, null, null, workingDir);

        assertThat(result.getExitValue(), is(0));
        assertThat(result.outputUTF8(), containsString("worked\n"));
    }
    
}

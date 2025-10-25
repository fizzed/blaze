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
        assertThat(result.outputUTF8(), containsString("Hello World!" + System.lineSeparator()));
    }
    
    @Test
    public void tasks() throws Exception {
        final File scriptFile = resourceAsFile("/jdk/only_public.java");

        final ProcessResult result = BlazeRunner.invokeWithCurrentJvmHome(scriptFile, asList("-l"), null);

        assertThat(result.getExitValue(), is(0));
        assertThat(result.outputUTF8(), containsString("tasks =>\n  main"));
    }
    
    @Test
    public void defaultBlazeInWorkingDir() throws Exception {
        final File workingDir = resourceAsFile("/jdk/project0");
        final File scriptFile = resourceAsFile("/jdk/project0/blaze.java");

        final ProcessResult result = BlazeRunner.invokeWithCurrentJvmHome(scriptFile, null, null, workingDir);

        assertThat(result.getExitValue(), is(0));
        assertThat(result.outputUTF8(), containsString("worked" + System.lineSeparator()));
    }
    
    @Test
    public void defaultBlazeInSubBlazeDir() throws Exception {
        final File workingDir = resourceAsFile("/jdk/project1");

        final ProcessResult result = BlazeRunner.invokeWithCurrentJvmHome(null, null, null, workingDir);

        assertThat(result.getExitValue(), is(0));
        assertThat(result.outputUTF8(), containsString("worked" + System.lineSeparator()));
    }
    
    @Test
    public void defaultBlazeInSubDotBlazeDir() throws Exception {
        final File workingDir = resourceAsFile("/jdk/project2");

        final ProcessResult result = BlazeRunner.invokeWithCurrentJvmHome(null, null, null, workingDir);

        assertThat(result.getExitValue(), is(0));
        assertThat(result.outputUTF8(), containsString("worked" + System.lineSeparator()));
    }

    @Test
    public void blazeWithConfig() throws Exception {
        final File workingDir = resourceAsFile("/jdk/project3");

        final ProcessResult result = BlazeRunner.invokeWithCurrentJvmHome(null, null, null, workingDir);

        assertThat(result.getExitValue(), is(0));
        assertThat(result.outputUTF8(), containsString("I am a random value in the primary config file"));
    }

    @Test
    public void blazeWithLocalConfig() throws Exception {
        final File workingDir = resourceAsFile("/jdk/project4");

        final ProcessResult result = BlazeRunner.invokeWithCurrentJvmHome(null, null, null, workingDir);

        assertThat(result.getExitValue(), is(0));

        final String output = result.outputUTF8();
        assertThat(output, containsString("val1 = This is val1 in the local config file"));
        assertThat(output, containsString("val2 = This is val2 in the primary config file"));
    }

    @Test
    public void blazeWithConfigsAndCommandLineArguments() throws Exception {
        final File workingDir = resourceAsFile("/jdk/project5");

        final ProcessResult result1 = BlazeRunner.invokeWithCurrentJvmHome(null, asList("test"), null, workingDir);

        assertThat(result1.getExitValue(), is(0));

        final String output1 = result1.outputUTF8();
        assertThat(output1, containsString("arg1 = 1"));
        assertThat(output1, containsString("arg2 = arg2"));
        assertThat(output1, containsString("arg3 = localArg3"));

        // now with command line arguments to override config
        final ProcessResult result2 = BlazeRunner.invokeWithCurrentJvmHome(null, asList("test"), asList("--project5.arg3", "cmdLineArg3", "--project5.arg1", "999"), workingDir);

        assertThat(result2.getExitValue(), is(0));

        final String output2 = result2.outputUTF8();
        assertThat(output2, containsString("arg1 = 999"));
        assertThat(output2, containsString("arg2 = arg2"));
        assertThat(output2, containsString("arg3 = cmdLineArg3"));
    }

    @Test
    public void blazeNoTaskInArgs() throws Exception {
        final File workingDir = resourceAsFile("/jdk/project5");

        final ProcessResult result = BlazeRunner.invokeWithCurrentJvmHome(null, null, null, workingDir);

        assertThat(result.getExitValue(), is(1));

        final String output = result.outputUTF8();
        assertThat(output.contains("'main' not found"), is(false));
        assertThat(output.replaceAll("\r\n", "\n"), containsString("tasks =>\n  test"));
    }

    @Test
    public void blazeValidateAllTasksExistBeforeExecutingThem() throws Exception {
        final File workingDir = resourceAsFile("/jdk/project5");

        final ProcessResult result = BlazeRunner.invokeWithCurrentJvmHome(null, asList("test", "notexist"), null, workingDir);

        assertThat(result.getExitValue(), is(1));

        final String output = result.outputUTF8();
        // this prints out from test which should NOT run if any task is missing
        assertThat(output.contains("arg1 = 1"), is(false));
        assertThat(output, containsString("'notexist' not found"));
        assertThat(output.replaceAll("\r\n", "\n"), containsString("tasks =>\n  test"));
    }

}
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
package com.fizzed.blaze.kotlin;

import com.fizzed.blaze.Context;
import com.fizzed.blaze.internal.ConfigHelper;
import com.fizzed.blaze.internal.ContextImpl;
import com.fizzed.blaze.util.BlazeRunner;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zeroturnaround.exec.ProcessResult;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import static com.fizzed.blaze.internal.FileHelper.resourceAsFile;
import static java.util.Arrays.asList;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class BlazeKotlinEngineTest {
    final static private Logger log = LoggerFactory.getLogger(BlazeKotlinEngineTest.class);
    
    @BeforeAll
    static public void clearCache() throws IOException {
        Context context = new ContextImpl(null, Paths.get(System.getProperty("user.home")), null, null);
        Path classesDir = ConfigHelper.userBlazeEngineDir(context, "kotlin");
        FileUtils.deleteDirectory(classesDir.toFile());
    }
    
    @Test
    public void hello() throws Exception {
        final File scriptFile = resourceAsFile("/kotlin/hello.kt");

        final ProcessResult result = BlazeRunner.invokeWithCurrentJvmHome(scriptFile, null, null);

        assertThat(result.getExitValue(), is(0));
        assertThat(result.outputUTF8(), containsString("Hello World!" + System.lineSeparator()));
    }
    
    /*@Test
    public void nocompile() throws Exception {
        final File scriptFile = resourceAsFile("/kotlin/nocompile.kt");

        final ProcessResult result = BlazeRunner.invokeWithCurrentJvmHome(scriptFile, null, null);

        assertThat(result.getExitValue(), is(1));
    }*/
    
    @Test
    public void tasks() throws Exception {
        final File scriptFile = resourceAsFile("/kotlin/only_public.kt");

        final ProcessResult result = BlazeRunner.invokeWithCurrentJvmHome(scriptFile, asList("-l"), null);

        assertThat(result.getExitValue(), is(0));
        assertThat(result.outputUTF8().replaceAll("\r\n", "\n"), containsString("tasks =>\n  main"));
    }
    
}

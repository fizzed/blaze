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
package com.fizzed.blaze.nashorn;

import com.fizzed.blaze.core.Blaze;
import com.fizzed.blaze.core.BlazeException;
import com.fizzed.blaze.Config;
import com.fizzed.blaze.core.NoSuchTaskException;
import static com.fizzed.blaze.system.ShellTestHelper.getBinDirAsResource;
import com.fizzed.blaze.internal.Dependency;
import static com.fizzed.blaze.internal.FileHelper.resourceAsFile;
import java.io.File;
import java.util.Arrays;
import java.util.List;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
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
public class BlazeNashornEngineTest {
    final static private Logger log = LoggerFactory.getLogger(BlazeNashornEngineTest.class);
    
    @Rule
    public final SystemOutRule systemOutRule = new SystemOutRule().enableLog();
    
    @BeforeClass
    static public void forceBinResourceExecutable() throws Exception {
        // this makes the files in the "bin" sample directory executable
        getBinDirAsResource();
    }
    
    @Test
    public void noDefaultBlazeFile() throws Exception {
        try {
            Blaze blaze
                = Blaze.builder()
                    .build();
            fail();
        } catch (BlazeException e) {
            assertThat(e.getMessage(), containsString("Unable to find a blaze file"));
        }
    }
    
    @Test
    public void blazeFileDoesNotExist() throws Exception {
        try {
            Blaze blaze
                = Blaze.builder()
                    .file(new File("thisdoesnotexist"))
                    .build();
            fail();
        } catch (BlazeException e) {
            assertThat(e.getMessage(), containsString("Blaze file thisdoesnotexist not found"));
        }
    }
    
    @Test
    public void noEngineForFileExt() throws Exception {
        try {
            Blaze blaze
                = Blaze.builder()
                    .file(resourceAsFile("/nashorn/noengine.txt"))
                    .build();
            fail();
        } catch (BlazeException e) {
            assertThat(e.getMessage(), containsString("Unable to find script engine for file extension"));
        }
    }
    
    @Test
    public void defaultFileName() throws Exception {
        Blaze blaze
            = Blaze.builder()
                .directory(resourceAsFile("/nashorn"))
                .build();
        
        systemOutRule.clearLog();
        
        blaze.execute();
        
        assertThat(systemOutRule.getLog(), containsString("Default Blaze!"));
    }
    
    @Test
    public void empty() throws Exception {
        Blaze blaze
            = Blaze.builder()
                .file(resourceAsFile("/nashorn/empty.js"))
                .build();
        
        try {
            blaze.execute();
            fail();
        } catch (NoSuchTaskException e) {
            assertThat(e.getTask(), is("main"));
        }
    }
    
    @Test
    public void hello() throws Exception {
        Blaze blaze
            = Blaze.builder()
                .file(resourceAsFile("/nashorn/hello.js"))
                .build();
        
        systemOutRule.clearLog();
        
        blaze.execute();
        
        assertThat(systemOutRule.getLog(), containsString("Hello World!"));
    }
    
    @Test
    public void confFileLoaded() throws Exception {
        // this should find the corrosponding conf file and change the default task
        Blaze blaze
            = Blaze.builder()
                .file(resourceAsFile("/nashorn/new_default_task.js"))
                .build();
        
        assertThat(blaze.context().config().find(Config.KEY_DEFAULT_TASK).get(), is("blaze"));
        
        systemOutRule.clearLog();
        
        blaze.execute();
        
        assertThat(systemOutRule.getLog(), containsString("New Default Task!"));
    }
    
    @Test
    public void executeTwoTasks() throws Exception {
        // this should find the corrosponding conf file and change the default task
        Blaze blaze
            = Blaze.builder()
                .file(resourceAsFile("/nashorn/two_tasks.js"))
                .build();
        
        systemOutRule.clearLog();
        
        blaze.executeAll(Arrays.asList("main", "blaze"));
        
        assertThat(systemOutRule.getLog(), containsString("Hello World!"));
        assertThat(systemOutRule.getLog(), containsString("In Blaze!"));
    }
    
    @Test
    public void tasks() throws Exception {
        Blaze blaze
            = Blaze.builder()
                .file(resourceAsFile("/nashorn/two_tasks.js"))
                .build();
        
        systemOutRule.clearLog();
        
        List<String> tasks = blaze.tasks();
        
        log.debug("tasks: {}", tasks);
        
        assertThat(tasks, hasSize(2));
        assertThat(tasks, hasItem("main"));
        assertThat(tasks, hasItem("blaze"));
    }
    
    @Test
    public void dependency() throws Exception {
        systemOutRule.clearLog();

        Blaze blaze
            = Blaze.builder()
                .file(resourceAsFile("/nashorn/dependency.js"))
                .build();
        
        assertThat(blaze.dependencies(), hasItem(Dependency.parse("com.google.guava:guava:18.0")));
        
        // executing the script will confirm the dependency in on classpath and would work
        // since the script uses the guava library and wouldn't compile without it
        
        systemOutRule.clearLog();
        
        blaze.execute("main");
        
        assertThat(systemOutRule.getLog(), containsString("Harry; Ron; Hermione"));
    }
    
    @Test
    public void logInBindings() throws Exception {
        systemOutRule.clearLog();

        Blaze blaze
            = Blaze.builder()
                .file(resourceAsFile("/nashorn/log.js"))
                .build();
        
        systemOutRule.clearLog();
        
        blaze.execute("main");
        
        assertThat(systemOutRule.getLog(), containsString("Did this work?"));
    }
    
    @Test
    public void captureOutputDisablesLoggingToStdout() throws Exception {
        systemOutRule.clearLog();

        Blaze blaze
            = Blaze.builder()
                .file(resourceAsFile("/nashorn/capture_output.js"))
                .build();
        
        blaze.execute("main");
        
        assertThat(systemOutRule.getLog(), not(containsString("Hello World")));
        
        systemOutRule.clearLog();
        
        blaze.execute("output");
        
        assertThat(systemOutRule.getLog(), containsString("Hello World"));
    }
    
}

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
package com.fizzed.blaze.groovy;

import com.fizzed.blaze.core.Blaze;
import com.fizzed.blaze.core.BlazeTask;
import com.fizzed.blaze.core.MessageOnlyException;
import com.fizzed.blaze.core.NoSuchTaskException;
import static com.fizzed.blaze.internal.FileHelper.resourceAsFile;
import com.fizzed.blaze.internal.NoopDependencyResolver;
import java.util.List;
import org.codehaus.groovy.control.MultipleCompilationErrorsException;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import org.junit.Rule;
import org.junit.Test;
import org.junit.contrib.java.lang.system.SystemOutRule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BlazeGroovyEngineTest {
    static final private Logger log = LoggerFactory.getLogger(BlazeGroovyEngineTest.class);
    
    @Rule
    public final SystemOutRule systemOutRule = new SystemOutRule().enableLog();
    
    @Test
    public void empty() throws Exception {
        Blaze blaze = new Blaze.Builder()
            // to prevent tests failing on new version not being installed locally yet
            .dependencyResolver(new NoopDependencyResolver())
            .file(resourceAsFile("/groovy/empty.groovy"))
            .build();
        
        try {
            blaze.execute(null);
            fail();
        } catch (NoSuchTaskException e) {
            assertThat(e.getTask(), is("main"));
        }
    }
    
    @Test
    public void hello() throws Exception {
        Blaze blaze = new Blaze.Builder()
            .dependencyResolver(new NoopDependencyResolver())
            .file(resourceAsFile("/groovy/hello.groovy"))
            .build();
        
        systemOutRule.clearLog();
        
        blaze.execute(null);
        
        assertThat(systemOutRule.getLog(), containsString("Hello World!"));
    }
    
    @Test
    public void taskNotFound() throws Exception {
        Blaze blaze = new Blaze.Builder()
            .dependencyResolver(new NoopDependencyResolver())
            .file(resourceAsFile("/groovy/hello.groovy"))
            .build();
        
        try {
            blaze.execute("doesnotexist");
            fail();
        } catch (NoSuchTaskException e) {
            assertThat(e.getTask(), is("doesnotexist"));
        }
    }
    
    @Test
    public void tasks() throws Exception {
        Blaze blaze = new Blaze.Builder()
            .dependencyResolver(new NoopDependencyResolver())
            .file(resourceAsFile("/groovy/two_tasks.groovy"))
            .build();
        
        systemOutRule.clearLog();
        
        List<BlazeTask> tasks = blaze.getTasks();
        
        log.debug("tasks: {}", tasks);
        
        assertThat(tasks, hasSize(2));
        assertThat(tasks.get(0), is(new BlazeTask("blaze")));
        assertThat(tasks.get(1), is(new BlazeTask("main")));
    }
    
    @Test
    public void scriptInitiliazed() throws Exception {
        systemOutRule.clearLog();
        
        Blaze blaze = new Blaze.Builder()
            .dependencyResolver(new NoopDependencyResolver())
            .file(resourceAsFile("/groovy/script_initialized.groovy"))
            .build();
        
        // script should have been initialized
        assertThat(systemOutRule.getLog(), containsString("Groovy run() called"));
        
        // main should not have been called yet
        assertThat(systemOutRule.getLog(), not(containsString("1.0.FINAL")));
        
        blaze.execute();
        
        // main should have been called
        assertThat(systemOutRule.getLog(), containsString("1.0.FINAL"));
    }
    
    @Test
    public void exceptionsNotWrappedDuringExecution() throws Exception {
        try {
            Blaze blaze = new Blaze.Builder()
                .dependencyResolver(new NoopDependencyResolver())
                .file(resourceAsFile("/groovy/message_only_exception.groovy"))
                .build();

            blaze.execute();
            
            fail();
        } catch (MessageOnlyException e) {
            assertThat(e.getMessage(), containsString("This message should be displayed"));
        }
    }
    
    @Test
    public void compileFailNotWrappedExecution() throws Exception {
        try {
            Blaze blaze = new Blaze.Builder()
                .dependencyResolver(new NoopDependencyResolver())
                .file(resourceAsFile("/groovy/compile_fail.groovy"))
                .build();

            blaze.execute();
            
            fail();
        } catch (MultipleCompilationErrorsException e) {
            assertThat(e.getMessage(), containsString("startup failed"));
        }
    }
    
}

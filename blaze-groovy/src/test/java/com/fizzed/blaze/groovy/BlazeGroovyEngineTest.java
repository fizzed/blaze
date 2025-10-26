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

import com.fizzed.blaze.util.BlazeRunner;
import org.codehaus.groovy.control.MultipleCompilationErrorsException;

import static org.codehaus.groovy.runtime.InvokerHelper.asList;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.fail;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zeroturnaround.exec.ProcessResult;

public class BlazeGroovyEngineTest {
    static final private Logger log = LoggerFactory.getLogger(BlazeGroovyEngineTest.class);
    
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
        final ProcessResult result = BlazeRunner.invokeWithCurrentJvmHome(
            resourceAsFile("/groovy/hello.groovy"), null, null);

        assertThat(result.outputUTF8(), containsString("Hello World!"));
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

        List<BlazeTask> tasks = blaze.getTasks();

        assertThat(tasks, hasSize(2));
        assertThat(tasks.get(0), is(new BlazeTask("blaze")));
        assertThat(tasks.get(1), is(new BlazeTask("main")));
    }
    
    @Test
    public void scriptInitiliazed() throws Exception {
        final ProcessResult result = BlazeRunner.invokeWithCurrentJvmHome(
            resourceAsFile("/groovy/script_initialized.groovy"), null, null);

        // script should have been initialized
        assertThat(result.outputUTF8(), containsString("Groovy run() called"));
        
        // main should not have been called yet
        //assertThat(result.outputUTF8(), not(containsString("1.0.FINAL")));
        
        // main should have been called
        assertThat(result.outputUTF8(), containsString("1.0.FINAL"));
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

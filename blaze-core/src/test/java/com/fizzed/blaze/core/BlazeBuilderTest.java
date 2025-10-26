/*
 * Copyright 2016 Fizzed, Inc.
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
package com.fizzed.blaze.core;

import com.fizzed.blaze.Task;
import com.fizzed.blaze.internal.NoopDependencyResolver;
import com.fizzed.blaze.jdk.TargetObjectScript;
import java.util.Arrays;
import java.util.List;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasSize;
import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

public class BlazeBuilderTest {
    
    @Test
    public void scriptObject() throws Exception {
        TestScriptObject scriptObject = spy(new TestScriptObject());
        
        Blaze.Builder blazeBuilder = new Blaze.Builder()
            .scriptObject(scriptObject);
        
        Blaze blaze = blazeBuilder.build();
        
        assertThat(blaze, is(not(nullValue())));
        assertThat(blaze.getContext(), is(not(nullValue())));
        assertThat(blaze.getContext().config(), is(not(nullValue())));
        assertThat(blaze.getDependencies(), is(not(empty())));
        assertThat(blaze.getEngine(), is(nullValue()));
        assertThat(blaze.getScript(), instanceOf(TargetObjectScript.class));
        
        blaze.execute("main");

        verify(scriptObject).main();
    }

    @Test
    public void dependencyResolverNotCalledIfNoExtraDependencies() throws Exception {
        DependencyResolver dr = spy(new NoopDependencyResolver());
        TestScriptObject scriptObject = spy(new TestScriptObject());
        
        Blaze.Builder blazeBuilder = new Blaze.Builder()
            .scriptObject(scriptObject)
            .collectedDependencies(Arrays.asList(new Dependency("test", "test", "1.0.0")))
            .dependencyResolver(dr);
        
        blazeBuilder.resolveDependencies();
        
        assertThat(blazeBuilder, is(not(nullValue())));
        verify(dr, never()).resolve(any(), any(), any());
    }
    
    @Test
    public void tasks() throws Exception {
        TestScriptObject scriptObject = new TestScriptObject();
        
        Blaze.Builder blazeBuilder = new Blaze.Builder()
            .scriptObject(scriptObject);
        
        Blaze blaze = blazeBuilder.build();
        
        assertThat(blaze, is(not(nullValue())));
        
        List<BlazeTask> tasks = blaze.getTasks();
        
        assertThat(tasks, hasSize(1));
        assertThat(tasks.get(0).getName(), is("main"));
    }
    
    static public class Script1 {
        
        public void a1() { }
        
        public void b2() { } 
        
        public void c3() { }
        
    }
    
    @Test
    public void tasksInAlphabeticalOrder() throws Exception {
        Script1 script = new Script1();
        
        Blaze.Builder blazeBuilder = new Blaze.Builder()
            .scriptObject(script);
        
        Blaze blaze = blazeBuilder.build();
        
        // should be in alphabetical order
        List<BlazeTask> tasks = blaze.getTasks();
        
        assertThat(tasks, hasSize(3));
        assertThat(tasks.get(0).getName(), is("a1"));
        assertThat(tasks.get(1).getName(), is("b2"));
        assertThat(tasks.get(2).getName(), is("c3"));
    }
    
    static public class Script2 {
        
        @Task("this is a1")
        public void a1() { }
        
        @Task("this is b2")
        public void b2() { } 
        
        @Task("this is c3")
        public void c3() { }
        
    }
    
    @Test
    public void tasksWithDescriptions() throws Exception {
        Script2 script = new Script2();
        
        Blaze.Builder blazeBuilder = new Blaze.Builder()
            .scriptObject(script);
        
        Blaze blaze = blazeBuilder.build();
        
        // should be in alphabetical order
        List<BlazeTask> tasks = blaze.getTasks();
        
        assertThat(tasks, hasSize(3));
        assertThat(tasks.get(0).getDescription(), is("this is a1"));
        assertThat(tasks.get(1).getDescription(), is("this is b2"));
        assertThat(tasks.get(2).getDescription(), is("this is c3"));
    }
    
    static public class Script3 {
        
        @Task(order = 3)
        public void a1() { }
        
        @Task(order = 2)
        public void b2() { } 
        
        @Task(order = 1)
        public void c3() { }
        
    }
    
    @Test
    public void tasksWithOrdering() throws Exception {
        Script3 script = new Script3();
        
        Blaze.Builder blazeBuilder = new Blaze.Builder()
            .scriptObject(script);
        
        Blaze blaze = blazeBuilder.build();
        
        // should be in alphabetical order
        List<BlazeTask> tasks = blaze.getTasks();
        
        assertThat(tasks, hasSize(3));
        assertThat(tasks.get(0).getName(), is("c3"));
        assertThat(tasks.get(1).getName(), is("b2"));
        assertThat(tasks.get(2).getName(), is("a1"));
        
    }
    
}

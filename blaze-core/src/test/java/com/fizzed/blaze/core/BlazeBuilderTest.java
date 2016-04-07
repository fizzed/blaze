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

import com.fizzed.blaze.internal.NoopDependencyResolver;
import com.fizzed.blaze.jdk.TargetObjectScript;
import java.util.Arrays;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.empty;
import static org.junit.Assert.assertThat;
import org.junit.Test;
import static org.mockito.Matchers.anyObject;
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
        assertThat(blaze.context(), is(not(nullValue())));
        assertThat(blaze.context().config(), is(not(nullValue())));
        assertThat(blaze.dependencies(), is(not(empty())));
        assertThat(blaze.engine(), is(nullValue()));
        assertThat(blaze.script(), instanceOf(TargetObjectScript.class));
        
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
        verify(dr, never()).resolve(anyObject(), anyObject(), anyObject());
    }
    
}

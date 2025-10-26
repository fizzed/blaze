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
package com.fizzed.blaze.ivy;

import com.fizzed.blaze.Config;
import com.fizzed.blaze.Config.Value;
import com.fizzed.blaze.Context;
import com.fizzed.blaze.core.Dependency;
import com.fizzed.blaze.internal.ContextImpl;
import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class IvyDependencyResolverTest {
    
    Config config;
    Context context;

    @BeforeEach
    public void before() {
        config = mock(Config.class);
        context = new ContextImpl(null, null, null, config);
        when(config.value(anyString(), any())).thenReturn(Value.of("", Boolean.FALSE));
        when(config.valueList(anyString())).thenReturn(Value.of("", null));
    }

    @Test
    public void resolveSingle() throws Exception {
        IvyDependencyResolver resolver = new IvyDependencyResolver();
    
        List<Dependency> resolved = Collections.emptyList();
    
        List<Dependency> dependencies = Arrays.asList(
            Dependency.parse("commons-io:commons-io:2.5"));
    
        List<File> files = resolver.resolve(context, resolved, dependencies);
    
        assertThat(files, hasSize(1));
        assertThat(files.get(0).getName(), is("commons-io-2.5.jar"));
        assertThat(files.get(0).length(), greaterThan(100L));
    }

    @Test
    public void resolveAlreadyResolvedNotReturned() throws Exception {
        IvyDependencyResolver resolver = new IvyDependencyResolver();
    
        List<Dependency> dependencies = Arrays.asList(
            Dependency.parse("commons-io:commons-io:2.5"));
    
        List<File> files = resolver.resolve(context, dependencies, dependencies);
    
        assertThat(files, hasSize(0));
    }

}

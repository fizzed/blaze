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
package com.fizzed.blaze.internal;

import com.fizzed.blaze.core.Dependency;
import com.fizzed.blaze.Config;
import java.util.Arrays;
import java.util.List;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.junit.Assert.assertThat;
import org.junit.Test;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 *
 * @author joelauer
 */
public class DependencyHelperTest {
    
    @Test
    public void cleanMavenDependencyLine() {
        // typical line of text
        assertThat(DependencyHelper.cleanMavenDependencyLine("com.example:hello:jar:1.0.1"), is("com.example:hello:1.0.1"));

        // new maven 3.9.11 line of text
        //
        assertThat(DependencyHelper.cleanMavenDependencyLine("com.fizzed:blaze-core:jar:1.9.1-SNAPSHOT\u001B[36m -- module blaze.core\u001B[0;1;33m (auto)\u001B[m"), is("com.fizzed:blaze-core:1.9.1-SNAPSHOT"));
    }
    
    @Test
    public void applicationDependencies() {
        Config config = mock(Config.class);
        
        when(config.valueList(Config.KEY_DEPENDENCIES)).thenReturn(Config.Value.empty(Config.KEY_DEPENDENCIES));
        
        List<Dependency> applicationDependencies = DependencyHelper.applicationDependencies(config);
        
        assertThat(applicationDependencies, is(nullValue()));
        
        
        when(config.valueList(Config.KEY_DEPENDENCIES)).thenReturn(Config.Value.of(Config.KEY_DEPENDENCIES, Arrays.asList("com.example:hello:1.0.0")));
        
        applicationDependencies = DependencyHelper.applicationDependencies(config);
        
        assertThat(applicationDependencies, contains(Dependency.parse("com.example:hello:1.0.0")));
        assertThat(applicationDependencies, not(contains(Dependency.parse("com.example:hello:1.0.1"))));
    }

    @Test
    public void alreadyBundledDefault() {
        final List<Dependency> dependencies = DependencyHelper.alreadyBundled();

        // NOTE: 4 OR the version of slf4j-api may change if those dependencies actually change, but this will help
        // validate that the bundled.txt file is being parsed correctly
        assertThat(dependencies, hasSize(4));
        assertThat(dependencies, hasItem(Dependency.parse("org.slf4j:slf4j-api:2.0.17")));
    }

    @Test
    public void alreadyBundledMaven397() {
        // maven v3.9.7 uses a dependency plugin that produces a bundled.txt that looks like different than other versions
        final List<Dependency> dependencies = DependencyHelper.alreadyBundled("/fixtures/bundled-maven-3.9.7.txt");

        assertThat(dependencies, hasSize(8));
        assertThat(dependencies.get(0).toString(), is("com.fizzed:blaze-ivy:1.9.0"));
        assertThat(dependencies.get(1).toString(), is("org.apache.ivy:ivy:2.5.2"));
        assertThat(dependencies.get(7).toString(), is("org.zeroturnaround:zt-exec:1.12"));
    }

    @Test
    public void alreadyBundledMaven3911() {
        // maven v3.9.7 uses a dependency plugin that produces a bundled.txt that looks like different than other versions
        final List<Dependency> dependencies = DependencyHelper.alreadyBundled("/fixtures/bundled-maven-3.9.11.txt");

        assertThat(dependencies, hasSize(8));
        assertThat(dependencies.get(0).toString(), is("com.fizzed:blaze-core:1.9.1-SNAPSHOT"));
        assertThat(dependencies.get(6).toString(), is("org.apache.ivy:ivy:2.5.2"));
        assertThat(dependencies.get(7).toString(), is("org.slf4j:slf4j-simple:2.0.13"));
    }
    
}

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
package com.fizzed.blaze.util;

import com.fizzed.blaze.Config;
import java.util.Arrays;
import java.util.List;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.Matchers.contains;
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
        assertThat(DependencyHelper.cleanMavenDependencyLine("com.example:hello:jar:1.0.1"), is("com.example:hello:1.0.1"));
    }
    
    @Test
    public void applicationDependencies() {
        Config config = mock(Config.class);
        
        when(config.getStringList(Config.KEY_DEPENDENCIES)).thenReturn(null);
        
        List<Dependency> applicationDependencies = DependencyHelper.applicationDependencies(config);
        
        assertThat(applicationDependencies, is(nullValue()));
        
        
        when(config.getStringList(Config.KEY_DEPENDENCIES)).thenReturn(Arrays.asList("com.example:hello:1.0.0"));
        
        applicationDependencies = DependencyHelper.applicationDependencies(config);
        
        assertThat(applicationDependencies, contains(Dependency.parse("com.example:hello:1.0.0")));
        assertThat(applicationDependencies, not(contains(Dependency.parse("com.example:hello:1.0.1"))));
    }
    
}

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
import java.util.HashSet;
import java.util.Set;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.MatcherAssert.assertThat;

import org.junit.jupiter.api.Test;

public class DependencyTest {
    
    @Test
    public void testEquals() {
        Dependency a = new Dependency("com.example", "hello", "1.0.0");
        Dependency b = new Dependency("com.example", "hello", "1.0.1");
        Dependency c = new Dependency("com.example", "hello", "1.0.0");
        
        assertThat(a.equals(b), is(not(true)));
        assertThat(a.equals(c), is(true));
        
        Set<Dependency> set = new HashSet<>();
        
        set.add(a);
        
        assertThat(set.contains(b), is(not(true)));
        assertThat(set.contains(c), is(true));
    }
    
}

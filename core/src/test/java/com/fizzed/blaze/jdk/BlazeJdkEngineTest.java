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
package com.fizzed.blaze.jdk;

import com.fizzed.blaze.core.Blaze;
import static com.fizzed.blaze.system.ShellTestHelper.getBinDirAsResource;
import static com.fizzed.blaze.internal.FileHelper.resourceAsFile;
import java.util.List;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertThat;
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
public class BlazeJdkEngineTest {
    final static private Logger log = LoggerFactory.getLogger(BlazeJdkEngineTest.class);
    
    @Rule
    public final SystemOutRule systemOutRule = new SystemOutRule().enableLog();
    
    @BeforeClass
    static public void forceBinResourceExecutable() throws Exception {
        // this makes the files in the "bin" sample directory executable
        getBinDirAsResource();
    }
    
    @Test
    public void hello() throws Exception {
        Blaze blaze
            = Blaze.builder()
                .file(resourceAsFile("/jdk/hello.java"))
                .build();
        
        systemOutRule.clearLog();
        
        blaze.execute();
        
        assertThat(systemOutRule.getLog(), containsString("Hello World!"));
    }
    
    @Test
    public void tasks() throws Exception {
        Blaze blaze
            = Blaze.builder()
                .file(resourceAsFile("/jdk/only_public.java"))
                .build();
        
        systemOutRule.clearLog();
        
        List<String> tasks = blaze.tasks();
        
        assertThat(tasks, hasSize(1));
        assertThat(tasks, contains("main"));
    }
    
}

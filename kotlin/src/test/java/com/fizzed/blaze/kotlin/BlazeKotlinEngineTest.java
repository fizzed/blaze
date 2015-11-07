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
package com.fizzed.blaze.kotlin;

import com.fizzed.blaze.Context;
import com.fizzed.blaze.core.Blaze;
import com.fizzed.blaze.core.CompilationException;
import com.fizzed.blaze.internal.ConfigHelper;
import com.fizzed.blaze.internal.ContextImpl;
import static com.fizzed.blaze.internal.FileHelper.resourceAsFile;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import org.apache.commons.io.FileUtils;
import static org.hamcrest.CoreMatchers.containsString;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.contrib.java.lang.system.SystemOutRule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import org.junit.Ignore;

public class BlazeKotlinEngineTest {
    final static private Logger log = LoggerFactory.getLogger(BlazeKotlinEngineTest.class);
    
    @BeforeClass
    static public void clearCache() throws IOException {
        Context context = new ContextImpl(null, Paths.get(System.getProperty("user.home")), null, null);
        Path classesDir = ConfigHelper.userBlazeEngineDir(context, "kotlin");
        FileUtils.deleteDirectory(classesDir.toFile());
    }
    
    @Rule
    public final SystemOutRule systemOutRule = new SystemOutRule().enableLog();
    
    @Test
    public void hello() throws Exception {
        Blaze blaze
            = Blaze.builder()
                .file(resourceAsFile("/kotlin/hello.kts"))
                .build();
        
        systemOutRule.clearLog();
        
        blaze.execute();
        
        assertThat(systemOutRule.getLog(), containsString("Hello World!"));
    }
    
    @Test @Ignore("Not sure we should support this style")
    public void noclazz() throws Exception {
        Blaze blaze
            = Blaze.builder()
                .file(resourceAsFile("/kotlin/noclazz.kt"))
                .build();
        
        systemOutRule.clearLog();
        
        blaze.execute();
        
        assertThat(systemOutRule.getLog(), containsString("Hello World!"));
    }
    
    @Test
    public void nocompile() throws Exception {
        try {
            Blaze blaze
                = Blaze.builder()
                    .file(resourceAsFile("/kotlin/nocompile.kts"))
                    .build();
            fail();
        } catch (CompilationException e) {
            assertThat(e.getMessage(), containsString("Unable to compile"));
        }
    }
    
    @Test
    public void tasks() throws Exception {
        Blaze blaze
            = Blaze.builder()
                .file(resourceAsFile("/kotlin/only_public.kt"))
                .build();
        
        systemOutRule.clearLog();
        
        List<String> tasks = blaze.tasks();
        
        assertThat(tasks, hasSize(1));
        assertThat(tasks, contains("main"));
    }
    
}

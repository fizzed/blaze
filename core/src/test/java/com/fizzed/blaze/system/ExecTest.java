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
package com.fizzed.blaze.system;

import com.fizzed.blaze.Config;
import com.fizzed.blaze.core.ContextImpl;
import static com.fizzed.blaze.system.ShellTestHelper.getBinDirAsResource;
import com.fizzed.blaze.internal.ConfigHelper;
import java.io.File;
import java.nio.file.Paths;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import org.junit.Before;
import org.junit.Test;
import static org.mockito.Mockito.spy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author joelauer
 */
public class ExecTest {
    private static final Logger log = LoggerFactory.getLogger(ExecTest.class);
    
    Config config;
    ContextImpl context;
    
    @Before
    public void setup() {
        config = ConfigHelper.create(null);
        context = spy(new ContextImpl(null, Paths.get("blaze.js"), config));
    }
    
    @Test(expected=ExecutableNotFoundException.class)
    public void notFind() throws Exception {
        ExecResult r = new Exec(context)
            .command("thisdoesnotexist")
            .run();
    }
    
    @Test
    public void works() throws Exception {
        ExecResult r = new Exec(context)
            .command("hello-world-test")
            .path(getBinDirAsResource())
            .run();
        
        assertThat(r.exitValue(), is(0));
    }
    
    @Test
    public void outputSetupBad() throws Exception {
        try {
            ExecResult r = new Exec(context)
                .command("hello-world-test")
                .path(getBinDirAsResource())
                .run();
            
            r.output();
            
            fail();
        } catch (IllegalStateException e) {
            assertThat(e.getMessage(), containsString("readOutput"));
        }
    }
    
    @Test
    public void capture() throws Exception {
        ExecResult r = new Exec(context)
            .command("hello-world-test")
            .path(getBinDirAsResource())
            .captureOutput()
            .run();
            
        String output = r.output();
        
        assertThat(output.trim(), is("Hello World"));
    }
    
}

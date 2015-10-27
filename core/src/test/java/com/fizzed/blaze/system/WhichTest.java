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

import com.fizzed.blaze.system.Which;
import com.fizzed.blaze.Config;
import com.fizzed.blaze.Context;
import static com.fizzed.blaze.system.ShellTestHelper.getBinDirAsResource;
import com.fizzed.blaze.util.ConfigHelper;
import com.fizzed.blaze.util.FileHelper;
import java.io.File;
import java.nio.file.Paths;
import static org.hamcrest.CoreMatchers.either;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;
import static org.junit.Assume.assumeTrue;
import org.junit.Before;
import org.junit.Test;
import static org.mockito.Mockito.spy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author joelauer
 */
public class WhichTest {
    private static final Logger log = LoggerFactory.getLogger(WhichTest.class);
    
    Config config;
    Context context;
    
    @Before
    public void setup() {
        config = ConfigHelper.create(null);
        context = spy(new Context(null, Paths.get("blaze.js"), config));
    }
    
    @Test
    public void notFind() throws Exception {
        File f = new Which(context)
            .command("thisdoesnotexist")
            .run();

        assertThat(f, is(nullValue()));
    }
    
    @Test
    public void worksOnWindows() throws Exception {
        assumeTrue("Test only valid on windows", ConfigHelper.OperatingSystem.windows());

        File f = new Which(context)
            .command("cmd")
            .run();
        
        log.debug("which: {}", f);

        assertThat(f, is(not(nullValue())));
        assertThat(f.isFile(), is(true));
        assertThat(f.canExecute(), is(true));
    }
    
    @Test
    public void worksOnUnix() throws Exception {
        assumeTrue("Test only valid on unix or mac", ConfigHelper.OperatingSystem.unix() || ConfigHelper.OperatingSystem.mac());

        File f = new Which(context)
            .command("ls")
            .run();
        
        log.debug("which: {}", f);

        assertThat(f, is(not(nullValue())));
        assertThat(f.isFile(), is(true));
        assertThat(f.canExecute(), is(true));
    }
    
    @Test
    public void worksWithPath() throws Exception {
        File f = new Which(context)
            .command("hello-world-test")
            .run();
        
        assertThat(f, is(nullValue()));
        
        f = new Which(context)
            .command("hello-world-test")
            .path(getBinDirAsResource())
            .run();

        assertThat(f, is(not(nullValue())));
        assertThat(f.isFile(), is(true));
        assertThat(f.canExecute(), is(true));
        assertThat(f.getName(), either(is("hello-world-test")).or(is("hello-world-test.bat")));
    }
    
}

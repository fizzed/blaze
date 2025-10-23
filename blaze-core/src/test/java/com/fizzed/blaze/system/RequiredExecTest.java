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
import com.fizzed.blaze.internal.ContextImpl;
import com.fizzed.blaze.core.MessageOnlyException;
import static com.fizzed.blaze.system.ShellTestHelper.getBinDirAsResource;
import com.fizzed.blaze.internal.ConfigHelper;
import java.nio.file.Path;
import java.nio.file.Paths;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.nullValue;
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
public class RequiredExecTest {
    private static final Logger log = LoggerFactory.getLogger(RequiredExecTest.class);
    
    Config config;
    ContextImpl context;
    
    @Before
    public void setup() {
        config = ConfigHelper.createEmpty();
        context = spy(new ContextImpl(null, null, Paths.get("blaze.js"), config));
    }
    
    @Test(expected=MessageOnlyException.class)
    public void notFind() throws Exception {
        Path f = new RequireExec(context)
            .command("thisdoesnotexist")
            .run();
    }
    
    @Test
    public void works() throws Exception {
        Path f = new RequireExec(context)
            .command("hello-world-test")
            .path(getBinDirAsResource())
            .run();
        
        assertThat(f, is(not(nullValue())));
    }
    
    @Test
    public void notFindWithMessage() throws Exception {
        try {
            Path f = new RequireExec(context)
                .command("thisdoesnotexist")
                .message("Download from http://blah...")
                .run();
            
            fail();
        } catch (MessageOnlyException e) {
            assertThat(e.getMessage(), containsString("Download from http://blah..."));
        }
    }
    
}

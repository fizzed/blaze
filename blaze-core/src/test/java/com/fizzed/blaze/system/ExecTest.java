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

import com.fizzed.blaze.core.ExecutableNotFoundException;
import com.fizzed.blaze.Config;
import com.fizzed.blaze.internal.ContextImpl;
import static com.fizzed.blaze.system.ShellTestHelper.getBinDirAsResource;
import com.fizzed.blaze.internal.ConfigHelper;
import com.fizzed.blaze.local.LocalExec;
import com.fizzed.blaze.util.CaptureOutput;
import com.fizzed.blaze.util.StreamableInput;
import com.fizzed.blaze.util.StreamableOutput;
import com.fizzed.blaze.util.Streamables;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Paths;
import java.util.Objects;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assume.assumeTrue;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

public class ExecTest {
    private static final Logger log = LoggerFactory.getLogger(ExecTest.class);
    
    Config config;
    ContextImpl context;
    
    @Before
    public void setup() {
        config = ConfigHelper.create(null);
        context = spy(new ContextImpl(null, null, Paths.get("blaze.js"), config));
    }
    
    @Test(expected=ExecutableNotFoundException.class)
    public void notFind() throws Exception {
        new LocalExec(context)
            .command("thisdoesnotexist")
            .run();
    }
    
    @Test
    public void works() throws Exception {
        Integer exitValue
            = new LocalExec(context)
                .command("hello-world-test")
                .path(getBinDirAsResource())
                .verbose()
                .run();
        
        assertThat(exitValue, is(0));
    }
    
    @Test
    public void nullInputAndOutputs() throws Exception {
        Integer exitValue
            = new LocalExec(context)
                .command("hello-world-test")
                .path(getBinDirAsResource())
                .pipeInput((StreamableInput)null)
                .pipeOutput((StreamableOutput)null)
                .pipeError((StreamableOutput)null)
                .run();
        
        assertThat(exitValue, is(0));
    }
    
    @Test
    public void customInputAndOutputs() throws Exception {
        // custom input and outputs
        InputStream in = spy(new ByteArrayInputStream(new byte[0]));
        OutputStream out = spy(new ByteArrayOutputStream());
        OutputStream err = spy(new ByteArrayOutputStream());
        
        new LocalExec(context)
            .command("hello-world-test")
            .path(getBinDirAsResource())
            .pipeInput(in)
            .pipeOutput(out)
            .pipeError(err)
            .run();
        
        // verify all streams were closed
        verify(in, atLeast(1)).close();
        verify(out, atLeast(1)).close();
        verify(err, atLeast(1)).close();
    }
    
    @Test
    public void captureOutput() throws Exception {
        CaptureOutput capture = Streamables.captureOutput();
        
        new LocalExec(context)
            .command("hello-world-test")
            .path(getBinDirAsResource())
            .pipeInput(Streamables.standardInput())
            .pipeOutput(capture)
            .run();
            
        String output = capture.asString();
        
        assertThat(output.trim(), is("Hello World 7586930100"));
    }

    @Test
    public void captureOutputWithPs1() throws Exception {
        CaptureOutput capture = Streamables.captureOutput();

        new LocalExec(context)
            .command("hello-world-test2")       // will be .ps1 on windows, using powershell.exe
            .path(getBinDirAsResource())
            .pipeInput(Streamables.standardInput())
            .pipeOutput(capture)
            .run();

        String output = capture.asString();

        assertThat(output.trim(), is("Hello World 5624112309877"));
    }
    
    @Test
    public void customInput() throws Exception {
        CaptureOutput capture = Streamables.captureOutput();
        
        new LocalExec(context)
            .command("tee")
            .path(getBinDirAsResource())
            .pipeInput(Streamables.input("hello dude"))
            .pipeOutput(capture)
            .run();
            
        String output = capture.asString();
        
        assertThat(output.trim(), is("hello dude"));
    }
    
}

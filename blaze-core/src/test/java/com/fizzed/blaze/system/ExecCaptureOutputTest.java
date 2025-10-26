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

import com.fizzed.blaze.core.DefaultContext;
import com.fizzed.blaze.local.LocalExec;
import static com.fizzed.blaze.system.ShellTestHelper.getBinDirAsResource;
import com.fizzed.blaze.util.CaptureOutput;
import com.fizzed.blaze.util.Streamables;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.sameInstance;
import static org.hamcrest.MatcherAssert.assertThat;

public class ExecCaptureOutputTest {
    private static final Logger log = LoggerFactory.getLogger(ExecCaptureOutputTest.class);

    @Test
    public void runCaptureOutput() throws Exception {
        String output =
            new LocalExec(new DefaultContext())
                .command("hello-world-test")
                .path(getBinDirAsResource())
                .disablePipeInput()
                .disablePipeError()
                .runCaptureOutput()
                .toString();
        
        assertThat(output.trim(), is("Hello World 7586930100"));
    }
    
    @Test
    public void runCaptureOutputUseExistingCaptureOutputIfSupplied() throws Exception {
        CaptureOutput captureOutput = Streamables.captureOutput();
        CaptureOutput output =
            new LocalExec(new DefaultContext())
                .command("hello-world-test")
                .path(getBinDirAsResource())
                .disablePipeInput()
                .disablePipeError()
                .pipeOutput(captureOutput)
                .runCaptureOutput();
        
        assertThat(output, sameInstance(captureOutput));
        assertThat(output.toString().trim(), is("Hello World 7586930100"));
    }
    
}

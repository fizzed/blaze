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
package com.fizzed.blaze.core;

import com.fizzed.blaze.util.CaptureOutput;
import com.fizzed.blaze.util.StreamableOutput;
import com.fizzed.blaze.util.Streamables;
import java.io.File;
import java.nio.file.Path;
import java.util.concurrent.TimeUnit;

public interface ExecMixin<T> extends PipeErrorMixin<T> {

    T command(Path command);
    
    T command(File command);
    
    T command(String command);
    
    /**
     * Adds one argument by appending to existing list.
     * @param argument
     * @return
     * @see #args(java.lang.Object...) For adding more than one argument
     */
    T arg(Object argument);

    /**
     * Adds one or more arguments by appending to existing list.
     * @param arguments
     * @return
     * @see #arg(java.lang.Object...) For adding a single argument
     */
    T args(Object... arguments);
    
    T env(String name, String value);

    T timeout(long timeoutInMillis);
    
    default public T timeout(long timeout, TimeUnit units) {
        this.timeout(TimeUnit.MILLISECONDS.convert(timeout, units));
        return (T)this;
    }
    
    default public T exitValue(Integer exitValue) {
        return exitValues(new Integer[] { exitValue });
    }
    
    T exitValues(Integer... exitValues);
    
    /**
    default public T captureOutput() {
        return this.captureOutput(true);
    }
    
    T captureOutput(boolean captureOutput);
    */
    
    default public T pipeErrorToOutput() {
        return pipeErrorToOutput(true);
    }
    
    T pipeErrorToOutput(boolean pipeErrorToOutput);
    
    // this will exist on any impl
    Object run();
    
    /**
     * Helper method to make it easier to exec a program and capture its output.
     * @return The captured output
     * @throws BlazeException 
     */
    default public CaptureOutput runCaptureOutput() throws BlazeException {
        CaptureOutput captureOutput = null;
        StreamableOutput output = getPipeOutput();
        
        // already set as capture output?
        if (output != null && output instanceof CaptureOutput) {
            captureOutput = (CaptureOutput)output;
        } else {
            captureOutput = Streamables.captureOutput();
            this.pipeOutput(captureOutput);
        }
        
        this.run();
        
        return captureOutput;
    }
    
}

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

import com.fizzed.blaze.core.PathsMixin;
import com.fizzed.blaze.core.PipeErrorMixin;
import com.fizzed.blaze.util.DeferredFileInputStream;
import com.fizzed.blaze.util.DeferredFileOutputStream;
import com.fizzed.blaze.util.NamedStream;
import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Path;
import java.util.concurrent.TimeUnit;

/**
 *
 * @author joelauer
 */
public interface ExecSupport<T> extends PipeErrorMixin<T> {

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
    
    default public T captureOutput() {
        return this.captureOutput(true);
    }
    
    T captureOutput(boolean captureOutput);
    
    default public T pipeErrorToOutput() {
        return pipeErrorToOutput(true);
    }
    
    T pipeErrorToOutput(boolean pipeErrorToOutput);
    
}

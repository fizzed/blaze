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

import com.fizzed.blaze.util.StreamableInput;
import com.fizzed.blaze.util.StreamableOutput;
import com.fizzed.blaze.util.Streamables;
import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.nio.file.Path;

/**
 * Mixin for piping in and out.
 * 
 * @author joelauer
 * @param <T>
 */
public interface PipeMixin<T> {
    
    StreamableInput getPipeInput();
    
    T pipeInput(StreamableInput pipeInput);

    default public T pipeInput(InputStream stream) {
        return pipeInput(Streamables.input(stream));
    }
    
    default public T pipeInput(Path path) {
        return pipeInput(Streamables.input(path));
    }
    
    default public T pipeInput(File file) {
        return pipeInput(Streamables.input(file));
    }
    
    default public T pipeInput(String text) {
        return pipeInput(Streamables.input(text));
    }
    
    default public T pipeInput(String text, Charset charset) {
        return pipeInput(Streamables.input(text, charset));
    }
    
    default public T disablePipeInput() {
        return pipeInput((StreamableInput)null);
    }
    
    StreamableOutput getPipeOutput();
    
    T pipeOutput(StreamableOutput pipeOutput);

    default public T pipeOutput(OutputStream stream) {
        return pipeOutput(Streamables.output(stream));
    }
    
    default public T pipeOutput(Path path) {
        return pipeOutput(Streamables.output(path));
    }
    
    default public T pipeOutput(File file) {
        return pipeOutput(Streamables.output(file));
    }
    
    default public T disablePipeOutput() {
        return pipeOutput((StreamableOutput)null);
    }
    
}

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

import com.fizzed.blaze.util.NamedStream;
import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Path;

/**
 * Mixin for piping in and out.
 * 
 * @author joelauer
 * @param <T>
 */
public interface PipeMixin<T> {
    
    NamedStream<InputStream> getPipeInput();
    
    T pipeInput(NamedStream<InputStream> pipeInput);

    default public T pipeInput(InputStream stream) {
        return pipeInput(NamedStream.input(stream));
    }
    
    default public T pipeInput(Path path) {
        return pipeInput(NamedStream.input(path));
    }
    
    default public T pipeInput(File file) {
        return pipeInput(NamedStream.input(file));
    }
    
    NamedStream<OutputStream> getPipeOutput();
    
    T pipeOutput(NamedStream<OutputStream> pipeOutput);

    default public T pipeOutput(OutputStream stream) {
        return pipeOutput(NamedStream.output(stream));
    }
    
    default public T pipeOutput(Path path) {
        return pipeOutput(NamedStream.output(path));
    }
    
    default public T pipeOutput(File file) {
        return pipeOutput(NamedStream.output(file));
    }
    
}

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
import java.io.OutputStream;
import java.nio.file.Path;

/**
 * Mixin with pipeError support.
 * 
 * @author joelauer
 */
public interface PipeErrorMixin<T> extends PipeMixin<T> {
    
    T pipeError(NamedStream<OutputStream> pipeError);

    default public T pipeError(OutputStream stream) {
        return pipeError(NamedStream.output(stream));
    }
    
    default public T pipeError(Path path) {
        return pipeError(NamedStream.output(path));
    }
    
    default public T pipeError(File file) {
        return pipeError(NamedStream.output(file));
    }
    
    
}

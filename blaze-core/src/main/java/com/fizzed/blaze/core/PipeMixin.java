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

    /**
     * Retrieves the configured output stream for piping operations.
     *
     * @return an instance of StreamableOutput representing the current output stream
     *         configuration for piping data.
     */
    StreamableOutput getPipeOutput();

    /**
     * Configures the output stream for piping operations.
     *
     * @param pipeOutput the StreamableOutput instance representing the output stream to be used for
     *                   data piping. Can be a file, path, or other streamable resource.
     * @return the current instance of the implementing class to allow method chaining.
     */
    T pipeOutput(StreamableOutput pipeOutput);

    /**
     * Configures the output stream for piping operations using an OutputStream.
     *
     * @param stream the OutputStream instance to be used for data piping.
     * @return the current instance of the implementing class to allow method chaining.
     */
    default public T pipeOutput(OutputStream stream) {
        return pipeOutput(Streamables.output(stream));
    }

    /**
     * Configures the output stream for piping operations using a Path.
     *
     * @param path the Path instance representing the file system location to be used for output piping. Truncates the file if it already exists.
     * @return the current instance of the implementing class to allow method chaining.
     */
    default public T pipeOutput(Path path) {
        return pipeOutput(Streamables.output(path));
    }

    /**
     * Configures the output stream for piping operations using a File.
     *
     * @param file the File instance representing the file system location to be used for output piping.
     *             Truncates the file if it already exists.
     * @return the current instance of the implementing class to allow method chaining.
     */
    default public T pipeOutput(File file) {
        return pipeOutput(Streamables.output(file));
    }

    /**
     * Configures the output stream for piping operations using a Path, with an option to append to the file if it already exists.
     *
     * @param path the Path instance representing the file system location to be used for output piping.
     * @param append a boolean flag indicating whether to append to the file if it already exists (true) or truncate it (false).
     * @return the current instance of the implementing class to allow method chaining.
     */
    default public T pipeOutput(Path path, boolean append) {
        return pipeOutput(Streamables.outputA(path, append));
    }

    /**
     * Configures the output stream for piping operations using a File, with an option
     * to append to the file if it already exists.
     *
     * @param file the File instance representing the file system location to be used
     *             for output piping.
     * @param append a boolean flag indicating whether to append to the file if it
     *               already exists (true) or truncate it (false).
     * @return the current instance of the implementing class to allow method chaining.
     */
    default public T pipeOutput(File file, boolean append) {
        return pipeOutput(Streamables.outputA(file, append));
    }

    /**
     * Disables the output stream for piping operations by setting it to null.
     *
     * @return the current instance of the implementing class to allow method chaining.
     */
    default public T disablePipeOutput() {
        return pipeOutput((StreamableOutput)null);
    }
    
}

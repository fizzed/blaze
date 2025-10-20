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

import com.fizzed.blaze.util.StreamableOutput;
import com.fizzed.blaze.util.Streamables;

import java.io.File;
import java.nio.file.Path;

public interface TargetOutputMixin<T> {

    /**
     * Sets the target destination using the provided {@link StreamableOutput}.
     *
     * @param output the StreamableOutput instance that represents the target destination for output.
     * @return an instance of type T after setting the target.
     */
    T target(StreamableOutput output);

    /**
     * Sets the target destination using the given file path.
     *
     * @param file the Path instance representing the target file.
     * @return an instance of type T after setting the target.
     */
    default T target(Path file) {
        return this.target(Streamables.output(file));
    }

    /**
     * Sets the target destination using the given file path and an option to use a temporary file.
     *
     * @param file the Path instance representing the target file.
     * @param useTemporaryFile a boolean indicating whether to use a temporary file during processing.
     * @return an instance of type T after setting the target.
     */
    default T target(Path file, boolean useTemporaryFile) {
        return this.target(Streamables.output(file, useTemporaryFile));
    }

    /**
     * Sets the target destination using the specified file.
     *
     * @param file the File instance representing the target file.
     * @return an instance of type T after setting the target.
     */
    default T target(File file) {
        return this.target(Streamables.output(file));
    }

}
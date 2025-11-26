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

import com.fizzed.blaze.util.ValueHolder;

/**
 * The {@code VerbosityMixin} interface provides a mechanism for managing verbosity levels
 * in classes that require configurable logging or output levels. By implementing this interface,
 * a class can enable methods for setting verbosity levels such as `VERBOSE`, `DEBUG`, or `TRACE`.
 * These levels, defined in the {@code Verbosity} enum, allow granular control over the amount
 * of output detail produced during operations.
 *
 * @param <T> the type of the implementing class to enable method chaining with fluent interfaces.
 */
public interface ProgressMixin<T> {

    ValueHolder<Boolean> getProgressHolder();

    /**
     * Enables or maintains the current progress state for the implementing class.
     *
     * @return the reference to the current instance of the implementing class, allowing method chaining.
     */
    default public T progress() {
        return this.progress(true);
    }

    /**
     * Sets the progress state for the current instance.
     *
     * @param progress a boolean indicating whether progress tracking should be enabled or disabled
     * @return the reference to the current instance of the implementing class, allowing method chaining
     */
    default public T progress(boolean progress) {
        this.getProgressHolder().set(progress);
        return (T)this;
    }

}
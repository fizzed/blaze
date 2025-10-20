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

import com.fizzed.blaze.util.VerboseLogger;

/**
 * The {@code VerbosityMixin} interface provides a mechanism for managing verbosity levels
 * in classes that require configurable logging or output levels. By implementing this interface,
 * a class can enable methods for setting verbosity levels such as `VERBOSE`, `DEBUG`, or `TRACE`.
 * These levels, defined in the {@code Verbosity} enum, allow granular control over the amount
 * of output detail produced during operations.
 *
 * @param <T> the type of the implementing class to enable method chaining with fluent interfaces.
 */
public interface VerbosityMixin<T> {

    VerboseLogger getVerboseLogger();

    /**
     * Sets the verbosity level to `VERBOSE` for logging or output purposes.
     * By invoking this method, the verbosity setting is switched to a more detailed level,
     * providing verbose information as defined in the {@code Verbosity} enum. The specific behavior
     * depends on the implementation of the verbosity handling in the containing class.
     *
     * @return the current instance with verbosity set to `VERBOSE`, enabling a fluent interface.
     */
    default T verbose() {
        return this.verbosity(Verbosity.VERBOSE);
    }

    /**
     * Sets the verbosity level to `DEBUG` for logging or output purposes.
     * By invoking this method, the verbosity setting is switched to debugging level,
     * enabling detailed debug information as defined in the {@code Verbosity} enum.
     * The specific behavior depends on the implementation of the verbosity handling
     * in the containing class.
     *
     * @return the current instance with verbosity set to `DEBUG`, enabling a fluent interface.
     */
    default T debug() {
        return this.verbosity(Verbosity.DEBUG);
    }


    /**
     * Sets the verbosity level to `TRACE` for logging or output purposes.
     * By invoking this method, the verbosity setting is switched to the most detailed level,
     * providing highly granular trace-level information as defined in the {@code Verbosity} enum.
     * The specific behavior depends on the implementation of the verbosity handling in the containing class.
     *
     * @return the current instance with verbosity set to `TRACE`, enabling a fluent interface.
     */
    default T trace() {
        return this.verbosity(Verbosity.TRACE);
    }

    /**
     * Sets the verbosity level for logging or output purposes. The input verbosity level determines
     * the amount of detail in the output, such as `VERBOSE`, `DEBUG`, or `TRACE`, as defined in the
     * {@code Verbosity} enum. The specific behavior depends on the implementation of the
     * {@code VerboseLogger} in the containing class.
     *
     * @param verbosity the verbosity level to set, representing the desired logging or output detail.
     * @return the current instance with the specified verbosity level set, enabling a fluent interface.
     */
    default T verbosity(Verbosity verbosity) {
        this.getVerboseLogger().setLevel(verbosity);
        return (T)this;
    }

}
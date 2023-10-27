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
 * Mixin for path() in an action by simply providing a getPaths() method.
 * 
 * @author joelauer
 */
public interface VerbosityMixin<T> {

    VerboseLogger getVerboseLogger();

    default T verbose() {
        return this.verbosity(Verbosity.VERBOSE);
    }

    default T debug() {
        return this.verbosity(Verbosity.DEBUG);
    }

    default T trace() {
        return this.verbosity(Verbosity.TRACE);
    }

    default T verbosity(Verbosity verbosity) {
        this.getVerboseLogger().setLevel(verbosity);
        return (T)this;
    }

}
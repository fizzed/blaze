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

import com.fizzed.blaze.util.IntRange;

import java.util.List;

/**
 *
 * @author joelauer
 */
public class UnexpectedExitValueException extends BlazeException {

    final private List<IntRange> expected;
    final private Integer actual;
    
    /**
     * Constructs an instance of <code>MessageOnlyException</code> with the
     * specified detail message.
     *
     * @param msg the detail message.
     */
    public UnexpectedExitValueException(String msg, List<IntRange> expected, Integer actual) {
        super(msg + " (expected = " + expected + "; actual = " + actual + ")");
        this.expected = expected;
        this.actual = actual;
    }

    public List<IntRange> getExpected() {
        return expected;
    }

    public Integer getActual() {
        return actual;
    }
    
    
    
}

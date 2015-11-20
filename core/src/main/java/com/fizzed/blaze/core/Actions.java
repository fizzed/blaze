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

import com.fizzed.blaze.util.CaptureOutput;
import com.fizzed.blaze.util.StreamableOutput;

public class Actions {
    
    static public <A extends PipeMixin, R extends Result> CaptureOutput toCaptureOutput(A action, R result) {
        StreamableOutput output = action.getPipeOutput();
        
        if (!(output instanceof CaptureOutput)) {
            throw new IllegalArgumentException("Action " + action.getClass().getCanonicalName() + " did not have a pipeOutput set to an instance of " + CaptureOutput.class.getCanonicalName());
        }

        return (CaptureOutput)output;
    }
    
    static public <A extends PipeErrorMixin, R extends Result> CaptureOutput toCaptureError(A action, R result) {
        StreamableOutput output = action.getPipeError();
        
        if (!(output instanceof CaptureOutput)) {
            throw new IllegalArgumentException("Action " + action.getClass().getCanonicalName() + " did not have a pipeError set to an instance of " + CaptureOutput.class.getCanonicalName());
        }

        return (CaptureOutput)output;
    }
    
}

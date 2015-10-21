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
package com.fizzed.blaze.util;

import com.fizzed.blaze.BlazeException;
import com.fizzed.blaze.Context;
import com.fizzed.blaze.Engine;
import com.fizzed.blaze.Script;

abstract public class AbstractEngine<S extends Script> implements Engine<S> {

    protected Context initialContext;
    
    @Override
    public boolean isInitialized() {
        return initialContext != null;
    }
    
    @Override
    public void init(Context initialContext) throws BlazeException {
        this.initialContext = initialContext;
    }
    
}

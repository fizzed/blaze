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

import com.fizzed.blaze.Context;
import java.util.List;

public interface Engine<S extends Script> {
    
    public String getName();
    
    public List<String> getFileExtensions();
    
    public boolean isInitialized();
    
    public void init(Context initialContext) throws BlazeException;
    
    public S compile(Context context) throws BlazeException;
    
}

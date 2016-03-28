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

import java.util.function.BiFunction;

public class Result<A extends Action,V,R extends Result> {
    
    private final A action;
    private final V value;

    public Result(A action, V value) {
        this.action = action;
        this.value = value;
    }
    
    public A action() {
        return this.action;
    }
    
    public V get() {
        return this.value;
    }
    
    public <U> U map(BiFunction<A,R,U> mapper) {
        return mapper.apply(action, (R)this);
    }
    
}

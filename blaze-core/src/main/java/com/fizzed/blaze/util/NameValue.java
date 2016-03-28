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

public class NameValue<N,V> {
    
    final private N name;
    final private V value;

    public NameValue(N name, V value) {
        ObjectHelper.requireNonNull(name, "name cannot be null");
        this.name = name;
        this.value = value;
    }

    public N name() {
        return name;
    }

    public V value() {
        return value;
    }

    @Override
    public String toString() {
        return name + "=" + value;
    }
    
}

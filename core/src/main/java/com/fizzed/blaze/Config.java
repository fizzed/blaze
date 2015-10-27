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
package com.fizzed.blaze;

import java.util.Arrays;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;

/**
 *
 * @author joelauer
 */
public interface Config {
    
    static String KEY_COMMAND_EXTS = "blaze.command.exts";
    static String KEY_DEFAULT_TASK = "blaze.default.task";
    static String KEY_DEPENDENCIES = "blaze.dependencies";
    static String KEY_DEPENDENCY_CLEAN = "blaze.dependency.clean";
    
    static String DEFAULT_TASK = "main";
    static Boolean DEFAULT_DEPENDENCY_CLEAN = Boolean.FALSE;
    
    static List<String> DEFAULT_COMMAND_EXTS_UNIX = Arrays.asList("", ".sh");
    static List<String> DEFAULT_COMMAND_EXTS_WINDOWS = Arrays.asList(".exe", ".bat", ".cmd");
    
    Value<String> find(String key);
    
    <T> Value<T> find(String key, Class<T> type);
    
    Value<List<String>> findList(String key);
    
    <T> Value<List<T>> findList(String key, Class<T> type);
    
    /**
     * Like an optional, but remembers the 'key' that created it.
     * @param <T> 
     */
    static public class Value<T> {
    
        private final String key;
        private final T value;

        static public <T> Value<T> of(String key, T value) {
            return new Value(key, value);
        }
        
        static public <T> Value<T> empty(String key) {
            return new Value(key, null);
        }
        
        private Value(String key, T value) {
            this.key = key;
            this.value = value;
        }
        
        public boolean isPresent() {
            return this.value != null;
        }
        
        public T get() throws NoSuchElementException {
            if (value == null) {
                throw new NoSuchElementException("Value for key '" + key + "' is missing");
            } else {
                return value;
            }
        }
        
        public T or(T defaultValue) throws NoSuchElementException {
            if (value == null) {
                return defaultValue;
            } else {
                return value;
            }
        }

        @Override
        public String toString() {
            if (value == null) {
                return null;
            } else {
                return value.toString();
            }
        }

        @Override
        public int hashCode() {
            int hash = 7;
            hash = 83 * hash + Objects.hashCode(this.value);
            return hash;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final Value<?> other = (Value<?>) obj;
            return Objects.equals(this.value, other.value);
        }
        
    }
    
}

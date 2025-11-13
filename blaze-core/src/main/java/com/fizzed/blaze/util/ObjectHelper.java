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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 *
 * @author joelauer
 */
public class ObjectHelper {
    
    static public void requireNonNull(Object value, String message) {
        if (value == null) {
            throw new IllegalArgumentException(message);
        }
    }
    
    static public String nonNullToString(Object object) {
        if (object == null) {
            throw new IllegalArgumentException("object was null");
        }
        
        return object.toString();
    }

    static public List<String> nonNullToStringList(Collection<?> objects) {
        if (objects == null) {
            return Collections.emptyList();
        }

        List<String> strings = new ArrayList<>();

        int i = 0;
        for (Object object : objects) {
            if (object == null) {
                throw new IllegalArgumentException("object #" + i + " was null");
            }
            strings.add(object.toString());
            i++;
        }

        return strings;
    }

    static public List<String> nonNullToStringList(Object[] objects) {
        if (objects == null) {
            return Collections.emptyList();
        }
        
        List<String> strings = new ArrayList<>();
        
        for (int i = 0; i < objects.length; i++) {
            Object object = objects[i];
            if (object == null) {
                throw new IllegalArgumentException("object #" + i + " was null");
            }
            strings.add(object.toString());
        }
        
        return strings;
    }
    
}

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

/**
 *
 * @author joelauer
 */
public class Converter {
    
    static public <T> T convert(String value, Class<T> type) {
        if (type.equals(Integer.class) || type.equals(int.class)) {
            return (T)Integer.valueOf(value);
        } else if (type.equals(Long.class) || type.equals(long.class)) {
            return (T)Long.valueOf(value);
        } else if (type.equals(Boolean.class) || type.equals(boolean.class)) {
            return (T)Boolean.valueOf(value);
        } else if (type.equals(Float.class) || type.equals(float.class)) {
            return (T)Float.valueOf(value);
        } else if (type.equals(Short.class) || type.equals(short.class)) {
            return (T)Short.valueOf(value);
        } else if (type.equals(Byte.class) || type.equals(byte.class)) {
            return (T)Byte.valueOf(value);
        } else if (type.equals(Double.class) || type.equals(double.class)) {
            return (T)Double.valueOf(value);
        } else {
            throw new IllegalArgumentException("We do not support converting values to type " + type.getCanonicalName());
        }
    }
    
}

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

import java.io.File;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author joelauer
 */
public class Converter {
    
    @FunctionalInterface
    static public interface Impl<T> {
        T convert(String value);
    }
    
    final static public Map<Class<?>,Impl<?>> CONVERTERS = new HashMap<>();
    static {
        CONVERTERS.put(Integer.class, (Impl<Integer>) (String value) -> {
            return Integer.valueOf(value);
        });
        CONVERTERS.put(Long.class, (Impl<Long>) (String value) -> {
            return Long.valueOf(value);
        });
        CONVERTERS.put(Boolean.class, (Impl<Boolean>) (String value) -> {
            return Boolean.valueOf(value);
        });
        CONVERTERS.put(Float.class, (Impl<Float>) (String value) -> {
            return Float.valueOf(value);
        });
        CONVERTERS.put(Short.class, (Impl<Short>) (String value) -> {
            return Short.valueOf(value);
        });
        CONVERTERS.put(Byte.class, (Impl<Byte>) (String value) -> {
            return Byte.valueOf(value);
        });
        CONVERTERS.put(Double.class, (Impl<Double>) (String value) -> {
            return Double.valueOf(value);
        });
        CONVERTERS.put(URI.class, (Impl<URI>) (String value) -> {
            return URI.create(value);
        });
        CONVERTERS.put(URL.class, (Impl<URL>) (String value) -> {
            try {
                return new URL(value);
            } catch (MalformedURLException e) {
                throw new IllegalArgumentException(e.getMessage(), e);
            }
        });
        CONVERTERS.put(MutableUri.class, (Impl<MutableUri>) (String value) -> {
            return MutableUri.of(value);
        });
        CONVERTERS.put(ImmutableUri.class, (Impl<ImmutableUri>) (String value) -> {
            return MutableUri.of(value).toImmutableUri();
        });
        
        
        // primitives as well
        CONVERTERS.put(int.class, CONVERTERS.get(Integer.class));
        CONVERTERS.put(long.class, CONVERTERS.get(Long.class));
        CONVERTERS.put(boolean.class, CONVERTERS.get(Boolean.class));
        CONVERTERS.put(float.class, CONVERTERS.get(Float.class));
        CONVERTERS.put(short.class, CONVERTERS.get(Short.class));
        CONVERTERS.put(byte.class, CONVERTERS.get(Byte.class));
        CONVERTERS.put(double.class, CONVERTERS.get(Double.class));

        CONVERTERS.put(Path.class, Paths::get);
        CONVERTERS.put(File.class, File::new);
    }
    
    static public <T> T convert(String value, Class<T> type) {
        Impl converter = CONVERTERS.get(type);
        
        if (converter == null) {
            throw new IllegalArgumentException("We do not support converting values to type " + type.getCanonicalName());
        }
        
        return (T)converter.convert(value);
    }
    
}

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
package com.fizzed.blaze.internal;

import com.fizzed.blaze.util.Converter;
import com.fizzed.blaze.Config;
import com.typesafe.config.ConfigException.Missing;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author joelauer
 */
public class ConfigImpl implements Config {
    
    private final com.typesafe.config.Config config;
    
    public ConfigImpl(com.typesafe.config.Config config) {
        this.config = config;
    }

    @Override
    public Value<String> find(String key) {
        try {
            return Value.of(key, this.config.getString(key));
        } catch (Missing e) {
            return Value.empty(key);
        }
    }
    
    @Override
    public <T> Value<T> find(String key, Class<T> type) {
        try {
            String value = this.config.getString(key);
            return Value.of(key, Converter.convert(value, type));
        } catch (Missing e) {
            return Value.empty(key);
        }
    }
    
    @Override
    public Value<List<String>> findList(String key) {
        try {
            return Value.of(key, this.config.getStringList(key));
        } catch (Missing e) {
            return Value.empty(key);
        }
    }
    
    @Override
    public <T> Value<List<T>> findList(String key, Class<T> type) {
        try {
            List<String> values = this.config.getStringList(key);
            List<T> convertedValues = new ArrayList<>(values.size());
            for (String value : values) {
                convertedValues.add(Converter.convert(value, type));
            }
            return Value.of(key, convertedValues);
        } catch (Missing e) {
            return Value.empty(key);
        }
    }
    
}

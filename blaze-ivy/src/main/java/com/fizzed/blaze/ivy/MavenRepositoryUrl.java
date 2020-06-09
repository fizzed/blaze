/*
 * Copyright 2020 Fizzed, Inc.
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
package com.fizzed.blaze.ivy;

import com.fizzed.blaze.util.MutableUri;

public class MavenRepositoryUrl {
    
    private final String id;
    private final MutableUri url;

    public MavenRepositoryUrl(String id, MutableUri url) {
        this.id = id;
        this.url = url;
    }

    public String getId() {
        return id;
    }

    public MutableUri getUrl() {
        return url;
    }
    
    static public MavenRepositoryUrl parse(String value) {
        if (value == null) {
            return null;
        }
        
        String[] tokens = value.split("\\|");
        String id;
        String root;
        
        if (tokens.length != 2) {
            throw new IllegalArgumentException("Malformed repository url. Format should be <id>|<url> but was '" + value + "'");
        }
        
        id = tokens[0];
        root = tokens[1];
        
        MutableUri url = new MutableUri(root);
        
        return new MavenRepositoryUrl(id, url);
    }
    
}
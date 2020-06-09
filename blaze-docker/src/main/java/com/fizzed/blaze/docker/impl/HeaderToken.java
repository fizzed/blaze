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
package com.fizzed.blaze.docker.impl;

import java.util.ArrayList;
import java.util.List;

public class HeaderToken {
 
    private final String name;
    private final int start;

    public HeaderToken(String name, int start) {
        this.name = name;
        this.start = start;
    }

    public String getName() {
        return name;
    }

    public int getStart() {
        return start;
    }
    
    @Override
    public String toString() {
        return "HeaderToken{" + "name=" + name + ", start=" + start + '}';
    }
    
    static public List<HeaderToken> parseHeaderLine(
            String line) {
        
        // split on multiple spaces...
        String[] tokens = line.split("\\s{2,}");
        
        List<HeaderToken> headerTokens = new ArrayList<>(tokens.length);

        for (String token : tokens) {
            int start = line.indexOf(token);
            headerTokens.add(new HeaderToken(token, start));
        }
        
        return headerTokens;
    }
    
}
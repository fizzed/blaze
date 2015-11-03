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

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Utility class for parsing paths and retaining all slashes.  E.g. /test 
 * and /test/ are not equal.
 * 
 * @author joelauer
 */
public class BasicPaths {
    
    static public boolean isAbsolute(List<String> paths) {
        return paths != null && !paths.isEmpty() && paths.get(0).equals("");
    }
    
    static public String toString(Path path) {
        List<String> paths = split(path);
        return toString(paths);
    }
    
    static public String toString(List<String> paths) {
        return toString(paths, 0, paths.size(), "/");
    }
    
    static public String toString(List<String> paths, int fromIndex, int toIndex, String delimiter) {
        final StringBuilder s = new StringBuilder();
        
        for (int i = fromIndex; i < toIndex; i++) {
            if (i != fromIndex) {
                s.append(delimiter);
            }
            s.append(paths.get(i));
        }
        
        return s.toString();
    }
    
    static public Path toPath(List<String> paths) {
        return Paths.get(toString(paths));
    }
    
    static public Path toPath(List<String> paths, int fromIndex, int toIndex) {
        return Paths.get(toString(paths, fromIndex, toIndex, "/"));
    }
    
    static public List<String> split(Path path) {
        if (path == null) {
            throw new IllegalArgumentException("path cannot be null");
        }
        
        int count = path.getNameCount();
        
        List<String> components = new ArrayList<>(count);
        
        for (int i = 0; i < count; i++) {
            components.add(path.getName(i).toString());
        }
        
        return components;
    }

    static public List<String> split(String path) {
        if (path == null) {
            throw new IllegalArgumentException("path cannot be null");
        }
        
        if (path.equals("")) {
            throw new IllegalArgumentException("path cannot be empty");
        }
        
        Objects.requireNonNull(path, "path cannot be null");
        
        List<String> components = new ArrayList<>();
        
        boolean escaped = false;
        int fromIndex = 0;
        
        for (int i = 0; i < path.length(); i++) {
            char c = path.charAt(i);
            
            if (c == '\\' && !escaped) {
                escaped = true;
            } else {
                if (!escaped && c == '/') {
                    String component = path.substring(fromIndex, i).replaceAll("\\\\/", "/");
                    components.add(component);
                    fromIndex = i + 1;
                }
                escaped = false;
            }
        }
        
        // add last component
        if (fromIndex <= path.length()) {
            String component = path.substring(fromIndex).replaceAll("\\\\/", "/");
            components.add(component);
        }
        
        return components;
    }
}

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
package com.fizzed.blaze.ssh.impl;

import java.nio.file.Path;

/**
 *
 * @author Joe Lauer
 */
public class PathHelper {
    
    static public String toString(Path path) {
        // TODO: figure out path of remote system?
        // for now assume its linux
        
        char pathSep = '/';
        
        StringBuilder s = new StringBuilder();
        
        // is absolute?  normal path.isAbsolute() doesn't work since local FS
        // may not match remote FS
        if (path.startsWith("\\") || path.startsWith("/")) {
            s.append(pathSep);
        }
        
        int count = path.getNameCount();
        for (int i = 0; i < count; i++) {
            Path name = path.getName(i);
            if (i != 0) {
                s.append(pathSep);
            }
            s.append(name.toString());
        }
        
        return s.toString();
    }
    
}

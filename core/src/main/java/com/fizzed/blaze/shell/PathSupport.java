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
package com.fizzed.blaze.shell;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

/**
 * Support for path() in an action.
 * 
 * @author joelauer
 */
public interface PathSupport<T> {

    public List<Path> getPaths();
    
    default public T path(Path path) {
        // insert onto front since user would likely want this searched first
        getPaths().add(0, path);
        return (T)this;
    }
    
    default public T path(File path) {
        // insert onto front since user would likely want this searched first
        getPaths().add(0, path.toPath());
        return (T)this;
    }
    
    default public T path(String path) {
        // insert onto front since user would likely want this searched first
        getPaths().add(0, Paths.get(path));
        return (T)this;
    }
    
    default public T path(String first, String ... more) {
        // insert onto front since user would likely want this searched first
        getPaths().add(0, Paths.get(first, more));
        return (T)this;
    }
    
}

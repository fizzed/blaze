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
package com.fizzed.blaze.core;

import com.fizzed.blaze.util.ObjectHelper;
import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Support for path() in an action by simply providing a getPaths() method.
 * 
 * Sort of a 'mixin for Java.
 * 
 * @author joelauer
 */
public interface PathsMixin<T> {

    public List<Path> getPaths();
    
    default public T path(Path path) {
        ObjectHelper.requireNonNull(path, "path cannot be null");
        // insert onto front since user would likely want this searched first
        getPaths().add(0, path);
        return (T)this;
    }
    
    default public T path(File path) {
        ObjectHelper.requireNonNull(path, "path cannot be null");
        // insert onto front since user would likely want this searched first
        getPaths().add(0, path.toPath());
        return (T)this;
    }
    
    default public T path(String path) {
        ObjectHelper.requireNonNull(path, "path cannot be null");
        // insert onto front since user would likely want this searched first
        getPaths().add(0, Paths.get(path));
        return (T)this;
    }
    
    default public T paths(Path... paths) {
        ObjectHelper.requireNonNull(paths, "paths cannot be null");
        // insert onto front since user would likely want this searched first
        for (int i = paths.length - 1; i >= 0; i--) {
            getPaths().add(0, paths[i]);
        }
        return (T)this;
    }
    
    default public T paths(File... files) {
        ObjectHelper.requireNonNull(files, "files cannot be null");
        // insert onto front since user would likely want this searched first
        for (int i = files.length - 1; i >= 0; i--) {
            getPaths().add(0, files[i].toPath());
        }
        return (T)this;
    }
    
}

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

import com.fizzed.blaze.util.Globber;
import java.nio.file.PathMatcher;

/**
 * Support for globber in an action by simply providing a getGlobber() method.
 * 
 * Sort of a 'mixin for Java.
 * 
 * @author joelauer
 */
public interface GlobberMixin<T> {

    public Globber getGlobber();
    
    default public T recursive() {
        getGlobber().recursive();
        return (T)this;
    }
    
    default public T recursive(boolean recursive) {
        getGlobber().recursive(recursive);
        return (T)this;
    }
    
    default public T filesOnly() {
        getGlobber().filesOnly();
        return (T)this;
    }
    
    default public T filesOnly(boolean filesOnly) {
        getGlobber().filesOnly(filesOnly);
        return (T)this;
    }
    
    default public T visibleOnly() {
        getGlobber().visibleOnly();
        return (T)this;
    }
    
    default public T visibleOnly(boolean visibleOnly) {
        getGlobber().visibleOnly(visibleOnly);
        return (T)this;
    }
    
    default public T include(String glob) {
        getGlobber().include(glob);
        return (T)this;
    }
    
    default public T include(PathMatcher matcher) {
        getGlobber().include(matcher);
        return (T)this;
    }
    
    default public T exclude(String glob) {
        getGlobber().exclude(glob);
        return (T)this;
    }
    
    default public T exclude(PathMatcher matcher) {
        getGlobber().exclude(matcher);
        return (T)this;
    }
}

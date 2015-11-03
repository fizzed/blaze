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

import com.fizzed.blaze.util.MutableUri;

/**
 * Support for directly supplying MutableUri methods in an action by simply
 * providing a getUri() method.
 * 
 * Sort of a 'mixin for Java.
 * 
 * @author joelauer
 */
public interface UriMixin<T> {

    public MutableUri getUri();
    
    default public T host(String host) {
        getUri().host(host);
        return (T)this;
    }
    
    default public T port(Integer port) {
        getUri().port(port);
        return (T)this;
    }
    
    default public T username(String username) {
        getUri().username(username);
        return (T)this;
    }
    
    default public T password(String password) {
        getUri().password(password);
        return (T)this;
    }

}

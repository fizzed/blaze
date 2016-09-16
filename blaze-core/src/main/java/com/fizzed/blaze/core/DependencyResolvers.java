/*
 * Copyright 2016 Fizzed, Inc.
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

import com.fizzed.blaze.internal.NoopDependencyResolver;
import java.util.Iterator;
import java.util.ServiceLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public interface DependencyResolvers {
    static final Logger log = LoggerFactory.getLogger(DependencyResolvers.class);
    
    static public DependencyResolver load() {
        ServiceLoader<DependencyResolver> loader = ServiceLoader.load(DependencyResolver.class);
        Iterator<DependencyResolver> iterator = loader.iterator();
        
        DependencyResolver resolver = null;
        while (iterator.hasNext()) {
            DependencyResolver r = iterator.next();
            if (resolver != null) {
                throw new IllegalStateException("Multiple dependency resolvers on classpath!");
            }
            resolver = r;
        }
        
        if (resolver == null) {
            //log.debug("No dependency resolver on classpath");
            //throw new IllegalStateException("No dependency resolver on classpath!");
            return new NoopDependencyResolver();
        }
        
        return resolver;
    }
    
}

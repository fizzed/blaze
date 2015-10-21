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

import com.fizzed.blaze.Engine;
import java.util.Iterator;
import java.util.ServiceLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author joelauer
 */
public class EngineHelper {
    static private final Logger log = LoggerFactory.getLogger(EngineHelper.class);
 
    private static final ServiceLoader<Engine> ENGINE_LOADER
        = ServiceLoader.load(Engine.class, Thread.currentThread().getContextClassLoader());
    
    static public Engine findByFileExtension(String fileExtension, boolean invalidateCache) {
        if (invalidateCache) {
            ENGINE_LOADER.reload();
        }
        
        Iterator<Engine> iterator = ENGINE_LOADER.iterator();

        while (iterator.hasNext()) {
            Engine engine = iterator.next();
            
            if (engine.getFileExtension().equals(fileExtension)) {
                return engine;
                /**
                // strange issue with abstract class being instantiated...
                try {
                    Class engineClass = engine.getClass();
                    log.debug("Found engine class {}", engineClass);
                    return (Engine)engineClass.newInstance();
                } catch (InstantiationException | IllegalAccessException e) {
                    throw new BlazeException("Unable to create new engine instance", e);
                }
                */
            }
        }
        
        return null;
    }
    
}

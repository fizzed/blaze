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
package com.fizzed.blaze.internal;

import com.fizzed.blaze.internal.ContextImpl;
import com.fizzed.blaze.Context;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author joelauer
 */
public class ContextHolder {
    private static final Logger log = LoggerFactory.getLogger(ContextHolder.class);
    
    private static final ThreadLocal<Context> CONTEXT =
         new ThreadLocal<Context>() {
             @Override protected ContextImpl initialValue() {
                 log.info("Creating new context for thread " + Thread.currentThread().getName());
                 return null;
         }
    };
    
    static public void set(Context context) {
        // set context to the the thread local
        CONTEXT.set(context);
    }
    
    static public Context get() {
        Context context = CONTEXT.get();
        
        if (context == null) {
            throw new IllegalStateException("Context not bound");
        }
        
        return context;
    }
    
}

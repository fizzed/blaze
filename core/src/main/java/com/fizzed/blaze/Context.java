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
package com.fizzed.blaze;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author joelauer
 */
public class Context {
    static private final Logger log = LoggerFactory.getLogger(Context.class);
    
    private static final ThreadLocal<Context> CONTEXT =
         new ThreadLocal<Context>() {
             @Override protected Context initialValue() {
                 log.info("Creating new context in thread " + Thread.currentThread().getName());
                 return null;
         }
    };
    
    static public void bindContext(Context context) {
        // bind context to the the thread local
        CONTEXT.set(context);
    }
    
    static public Context currentContext() {
        Context context = CONTEXT.get();
        
        if (context == null) {
            throw new IllegalStateException("Blaze context not bound");
        }
        
        return context;
    }
    
    final private Path baseDir;
    final private Path file;
    final private Config config;
    final private Logger logger;
    
    public Context(Path baseDir, Path file, Config config) {
        this.baseDir = baseDir;
        this.file = file;
        this.config = config;
        this.logger = LoggerFactory.getLogger("script");
    }
    
    public Logger logger() {
        return this.logger;
    }

    public Path baseDir() {
        if (this.baseDir == null) {
            return Paths.get(".");
        } else {
            return this.baseDir;
        }
    }
    
    public Path withBaseDir(Path path) {
        if (this.baseDir() == null) {
            return path;
        } else {
            return baseDir.resolve(path);
        }
    }
    
    public Path withBaseDir(File file) {
        if (file.isAbsolute()) {
            return file.toPath();
        } else {
            return baseDir.resolve(file.toPath());
        }
    }
    
    public Path withBaseDir(String path) {
        return withBaseDir(Paths.get(path));
    }

    public Path scriptFile() {
        return this.file;
    }
    
    public Config config() {
        return this.config;
    }
    
}

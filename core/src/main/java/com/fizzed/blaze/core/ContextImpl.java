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

import com.fizzed.blaze.Config;
import com.fizzed.blaze.Context;
import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author joelauer
 */
public class ContextImpl implements Context {
    static private final Logger log = LoggerFactory.getLogger(ContextImpl.class);
    
    final private Path baseDir;
    final private Path file;
    final private Config config;
    final private Logger logger;
    
    public ContextImpl(Path baseDir, Path file, Config config) {
        this.baseDir = baseDir;
        this.file = file;
        this.config = config;
        this.logger = LoggerFactory.getLogger("script");
    }
    
    @Override
    public Logger logger() {
        return this.logger;
    }

    @Override
    public Path baseDir() {
        if (this.baseDir == null) {
            return Paths.get(".");
        } else {
            return this.baseDir;
        }
    }
    
    @Override
    public Path withBaseDir(Path path) {
        if (this.baseDir == null) {
            return path;
        } else {
            return this.baseDir().resolve(path);
        }
    }
    
    @Override
    public Path withBaseDir(File file) {
        if (file.isAbsolute()) {
            return file.toPath();
        } else {
            return this.baseDir().resolve(file.toPath());
        }
    }
    
    @Override
    public Path withBaseDir(String path) {
        return withBaseDir(Paths.get(path));
    }
    
    @Override
    public Path scriptFile() {
        return this.file;
    }
    
    @Override
    public Config config() {
        return this.config;
    }

    @Override
    public Path userDir() {
        return Paths.get(System.getProperty("user.home"));
    }

    @Override
    public Path withUserDir(Path path) {
        return userDir().resolve(path);
    }

    @Override
    public Path withUserDir(File file) {
        return userDir().resolve(file.toPath());
    }

    @Override
    public Path withUserDir(String path) {
        return userDir().resolve(Paths.get(path));
    }
    
}

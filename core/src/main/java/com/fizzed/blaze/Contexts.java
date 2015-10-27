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

import com.fizzed.blaze.core.MessageOnlyException;
import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 *
 * @author joelauer
 */
public class Contexts {

    static public Path baseDir() {
        return Context.currentContext().baseDir();
    }
    
    static public Path withBaseDir(Path path) {
        return Context.currentContext().withBaseDir(path);
    }
    
    static public Path withBaseDir(File path) {
        return Context.currentContext().withBaseDir(path);
    }
    
    static public Path withBaseDir(String path) {
        return Context.currentContext().withBaseDir(path);
    }

    static public Path userDir() {
        return Paths.get(System.getProperty("user.home"));
    }
    
    static public void fail(String message) {
        throw new MessageOnlyException(message);
    }
    
}

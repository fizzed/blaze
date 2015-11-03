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

import com.fizzed.blaze.core.BlazeException;
import com.fizzed.blaze.core.ContextHolder;
import com.fizzed.blaze.core.MessageOnlyException;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.logging.Level;
import org.slf4j.Logger;

/**
 * Helpful static access to get context methods.
 * 
 * @author joelauer
 */
public class Contexts {

    /**
     * Current context of execution. Usually bound to a thread local.
     * @return The current context
     */
    static public Context currentContext() {
        return ContextHolder.get();
    }
    
    /**
     * A logger for the current context.
     * @return A logger for the current context. Guaranteed to be present.
     */
    static public Logger logger() {
        return ContextHolder.get().logger();
    }
    
    /**
     * The application configuration
     * @return The application configuration instance. Guaranteed to be present.
     */
    static public Config config() {
        return ContextHolder.get().config();
    }
    
    /**
     * The application base directory.  The base directory is the directory
     * of the script that is executing. So if your script is located at
     * "/home/joelauer/project/blaze.java" then this baes directory would be
     * "/home/joelauer/project"
     * @return The application base directory
     */
    static public Path baseDir() {
        return ContextHolder.get().baseDir();
    }
    
    /**
     * Resolves a path relative to the base directory. If the path is absolute
     * it will be returned intact, otherwise it will be appended to the
     * current base directory. So if your base directory is "/home/joelauer/project"
     * and you provide a path of "images/my.png" then a path of "/home/joelauer/project/images/my.png"
     * will be returned
     * @param path The path to resolve
     * @return The resolved path
     * @see #withBaseDir(java.io.File) 
     * @see #withBaseDir(java.lang.String) 
     */
    static public Path withBaseDir(Path path) {
        return ContextHolder.get().withBaseDir(path);
    }
    
    /**
     * Resolves a path relative to the base directory. If the path is absolute
     * it will be returned intact, otherwise it will be appended to the
     * current base directory. So if your base directory is "/home/joelauer/project"
     * and you provide a path of "images/my.png" then a path of "/home/joelauer/project/images/my.png"
     * will be returned
     * @param path The path to resolve
     * @return The resolved path
     * @see #withBaseDir(java.io.File) 
     * @see #withBaseDir(java.lang.String) 
     */
    static public Path withBaseDir(File path) {
        return ContextHolder.get().withBaseDir(path);
    }
    
    /**
     * Resolves a path relative to the base directory. If the path is absolute
     * it will be returned intact, otherwise it will be appended to the
     * current base directory. So if your base directory is "/home/joelauer/project"
     * and you provide a path of "images/my.png" then a path of "/home/joelauer/project/images/my.png"
     * will be returned
     * @param path The path to resolve
     * @return The resolved path
     * @see #withBaseDir(java.io.File) 
     * @see #withBaseDir(java.lang.String) 
     */
    static public Path withBaseDir(String path) {
        return ContextHolder.get().withBaseDir(path);
    }

    /**
     * The current user's home directory such as "/home/joelauer" or "C:\Users\Joe Lauer"
     * @return The current user's home directory
     */
    static public Path userDir() {
        return ContextHolder.get().userDir();
    }
    
    static public Path withUserDir(Path path) {
        return ContextHolder.get().withUserDir(path);
    }
    
    static public Path withUserDir(File path) {
        return ContextHolder.get().withUserDir(path);
    }
    
    static public Path withUserDir(String path) {
        return ContextHolder.get().withUserDir(path);
    }
    
    /**
     * Fails a script with a supplied message to be logged.  Does not trigger
     * a stacktrace to be logged!
     * @param message The message
     */
    static public void fail(String message) {
        throw new MessageOnlyException(message);
    }
    
    static public String prompt(String prompt, Object... args) {
        return ContextHolder.get().prompt(prompt, args);
    }
    
    static public char[] passwordPrompt(String prompt, Object... args) {
        return ContextHolder.get().passwordPrompt(prompt, args);
    }
    
}

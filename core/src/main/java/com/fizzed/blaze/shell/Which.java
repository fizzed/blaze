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
package com.fizzed.blaze.shell;

import com.fizzed.blaze.Action;
import com.fizzed.blaze.BlazeException;
import com.fizzed.blaze.Context;
import com.fizzed.blaze.util.ConfigHelper;
import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * which - locate a file for a command.
 * 
 * @author joelauer
 */
public class Which extends Action<File> {
    private static final Logger logger = LoggerFactory.getLogger(Which.class);
    
    private String command;
    private final List<Path> paths;
    
    public Which(Context context) {
        super(context);
        // initialize with system environment PATHs
        this.paths = ConfigHelper.systemEnvironmentPaths();
    }

    public String getCommand() {
        return this.command;
    }
    
    public Which command(String command) {
        this.command = command;
        return this;
    }

    public List<Path> getPaths() {
        return this.paths;
    }
    
    public Which path(Path path) {
        // insert onto front since user would likely want this searched first
        this.paths.add(0, path);
        return this;
    }
    
    public Which path(File path) {
        // insert onto front since user would likely want this searched first
        this.paths.add(0, path.toPath());
        return this;
    }
    
    public Which path(String path) {
        // insert onto front since user would likely want this searched first
        this.paths.add(0, Paths.get(path));
        return this;
    }
    
    public Which path(String first, String ... more) {
        // insert onto front since user would likely want this searched first
        this.paths.add(0, Paths.get(first, more));
        return this;
    }
    
    @Override
    public File doRun() throws BlazeException {
        return findExecutable(context, paths, command);
    }
    
    static public File findExecutable(Context context, List<Path> paths, String command) throws BlazeException {
        for (Path path : paths) {
            List<String> commandExtensions = ConfigHelper.commandExtensions(context.config());
            for (String ext : commandExtensions) {
                String commandWithExt = command + ext;
                
                File commandFile = new File(path.toFile(), commandWithExt);
                
                //logger.trace("commandFile: {}", commandFile);
                File f = context.withBaseDir(commandFile);
                
                logger.trace("Trying file: {}", f);
                if (f.exists() && f.isFile()) {
                    if (f.canExecute()) {
                        return f;
                    } else {
                        logger.warn("Found executable [" + f + "] but it is not executable! (continuing search...)");
                    }
                }
            }
        }
        
        return null;
    }
    
}

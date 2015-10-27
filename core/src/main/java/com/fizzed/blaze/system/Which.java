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
package com.fizzed.blaze.system;

import com.fizzed.blaze.core.Action;
import com.fizzed.blaze.core.BlazeException;
import com.fizzed.blaze.Context;
import com.fizzed.blaze.util.ConfigHelper;
import java.io.File;
import java.nio.file.Path;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * which - locate a file for a command.
 * 
 * @author joelauer
 */
public class Which extends Action<File> implements PathSupport<Which> {
    private static final Logger log = LoggerFactory.getLogger(Which.class);
    
    private final List<Path> paths;
    private String command;
    
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

    @Override
    public List<Path> getPaths() {
        return this.paths;
    }
    
    @Override
    protected File doRun() throws BlazeException {
        return find(context, paths, command);
    }
    
    static public File find(Context context, List<Path> paths, String command) throws BlazeException {
        for (Path path : paths) {
            List<String> commandExtensions = ConfigHelper.commandExtensions(context.config());
            for (String ext : commandExtensions) {
                String commandWithExt = command + ext;
                
                File commandFile = new File(path.toFile(), commandWithExt);
                
                //logger.trace("commandFile: {}", commandFile);
                File f = commandFile;
                
                log.trace("Trying file: {}", f);
                if (f.exists() && f.isFile()) {
                    if (f.canExecute()) {
                        return f;
                    } else {
                        log.warn("Command '" + f + "' found but it isn't executable! (continuing search...)");
                    }
                }
            }
        }
        
        return null;
    }
    
}

/*
 * Copyright 2014 Fizzed Inc.
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
package co.fizzed.blaze.action;

import co.fizzed.blaze.core.Context;
import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author joelauer
 */
public class WhichAction extends Action<File> {
    private static final Logger logger = LoggerFactory.getLogger(WhichAction.class);
    
    //private static final Map<String,File> cache = new ConcurrentHashMap<>();
    
    private String command;
    private final List<Path> paths;
    
    public WhichAction(Context context) {
        super(context, "which");
        // initialize with system environment PATHs
        this.paths = systemEnvironmentPaths();
    }

    public String getCommand() {
        return command;
    }
    
    public WhichAction command(String command) {
        this.command = command;
        return this;
    }

    public List<Path> getPaths() {
        return paths;
    }
    
    public WhichAction insertPath(Path path) {
        this.paths.add(0, path);
        return this;
    }
    
    public WhichAction insertPath(String path, String ... extra) {
        this.paths.add(0, Paths.get(path, extra));
        return this;
    }
    
    public WhichAction addPath(Path path) {
        this.paths.add(path);
        return this;
    }
    
    public WhichAction addPath(String path, String ... extra) {
        this.paths.add(Paths.get(path, extra));
        return this;
    }
    
    @Override
    protected Result<File> execute() throws Exception {
        File exeFile;
        
        // is it cached?
        //if (cache.containsKey(command)) {
        //    exeFile = cache.get(command);
        //} else {
            // search path for executable...
            exeFile = findExecutable(context, paths, command);
        //    if (exeFile != null) {
                // cache result
        //        cache.put(command, exeFile);
        //    }
        //}
        
        return new Result(exeFile);
    }
    
    static public File findExecutable(Context context, List<Path> paths, String command) throws Exception {
        for (Path path : paths) {
            for (String ext : context.getSettings().getExecutableExtensions()) {
                String commandWithExt = command + ext;
                File commandFile = new File(path.toFile(), commandWithExt);
                //logger.trace("commandFile: {}", commandFile);
                File f = context.resolveWithBaseDir(commandFile);
                logger.trace("Trying file: {}", f);
                if (f.exists() && f.isFile()) {
                    if (f.canExecute()) {
                        return f;
                    } else {
                        logger.debug("Found executable [" + f + "] but it is not executable! (continuing search...)");
                    }
                }
            }
        }
        
        return null;
    }
    
    static public List<Path> systemEnvironmentPaths() {
        List<String> pathStrings = systemEnvironmentPathsAsStrings();
        return pathStrings.stream().map(s -> Paths.get(s)).collect(Collectors.toList());
    }

    static public List<String> systemEnvironmentPathsAsStrings() {
        String path = System.getenv("PATH");
        if (path != null) {
            return Arrays.asList(path.split(File.pathSeparator));
        } else {
            return Collections.EMPTY_LIST;
        }
    }
    
}

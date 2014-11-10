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
package co.fizzed.otter.core;

import co.fizzed.otter.action.ExecAction;
import co.fizzed.otter.action.IvyAction;
import co.fizzed.otter.action.ListFilesAction;
import co.fizzed.otter.action.WhichAction;
import java.io.File;

/**
 *
 * @author joelauer
 */
public class Actions {
    
    private final Context context;
    
    public Actions(Context context) {
        this.context = context;
    }
    
    public ExecAction exec(String... command) throws Exception {
        return new ExecAction(context).setCommand(command);
    }
    
    public WhichAction which(String command) {
        return new WhichAction(context).setCommand(command);
    }
    
    public ListFilesAction ls(String path) {
        return new ListFilesAction(context).setPath(path);
    }
    
    public ListFilesAction ls(File path) {
        return new ListFilesAction(context).setPath(path);
    }
    
    public IvyAction ivy() {
        return new IvyAction(context);
    }
    
    
    /**
    private Map<String,File> cachedWhich = new HashMap<String,File>();
    
    private File findExecutable(String command) throws Exception {
        // search PATH environment variable
        String path = System.getenv("PATH");
        if (path != null) {
            String[] paths = path.split(File.pathSeparator);
            for (String p : paths) {
                //System.out.println("searching: " + p);
                for (String ext : context.getSettings().getExecutableExtensions()) {
                    String commandWithExt = command + ext;
                    File f = new File(p, commandWithExt);
                    if (f.exists() && f.isFile() && f.canExecute()) {
                        return f;
                    }
                }
            }
        }
        
        return null;
    }
    
    public File which(String command) throws Exception {
        // is it cached?
        if (cachedWhich.containsKey(command)) {
            return cachedWhich.get(command);
        }
        
        // search path for executable...
        File exeFile = findExecutable(command);
        
        if (exeFile != null) {
            // cache result
            this.cachedWhich.put(command, exeFile);
        }
            
        return exeFile;
    }
    
    public ProcessExecutor executor(String ... command) throws Exception {
        // first argument is a command we need to search for
        File exeFile = which(command[0]);
        if (exeFile == null) {
            throw new FileNotFoundException("Unable to find executable for command [" + command[0] + "]");
        } else {
            command[0] = exeFile.getAbsolutePath();
        }
        
        return new ProcessExecutor()
            .command(command)
            .redirectOutput(System.out)
            .redirectErrorStream(true)
            .exitValueNormal();
    }
    
    public int execute(String ... command) throws Exception {
        return executor(command)
            .execute()
            .getExitValue();
    }
    
    public List<File> listFiles() {
        return Arrays.asList(new File(".").listFiles());
    }
    */
}
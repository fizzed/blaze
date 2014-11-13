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
import java.io.FileNotFoundException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import org.zeroturnaround.exec.ProcessExecutor;
import org.zeroturnaround.exec.ProcessResult;

/**
 *
 * @author joelauer
 */
public class ExecAction extends Action<ProcessResult> {

    private final ProcessExecutor pe;
    private final List<Path> paths;
    private String executable;
    private List<String> arguments;
    
    public ExecAction(Context context) {
        super(context, "exec");
        // initialize with system environment PATHs
        this.paths = WhichAction.systemEnvironmentPaths();
        this.pe = new ProcessExecutor()
            .redirectInput(System.in)
            .redirectOutput(System.out)
            .redirectErrorStream(true)
            // initialize executable to context of current project basedir
            .directory(this.context.getBaseDir())
            .exitValueNormal();
        this.executable = null;
        this.arguments = null;
    }
    
    public List<Path> getPaths() {
        return paths;
    }
    
    public ExecAction insertPath(Path path) {
        this.paths.add(0, path);
        return this;
    }
    
    public ExecAction insertPath(String first, String ... more) {
        this.paths.add(0, Paths.get(first, more));
        return this;
    }
    
    public ExecAction addPath(Path path) {
        this.paths.add(path);
        return this;
    }
    
    public ExecAction addPath(String first, String ... more) {
        this.paths.add(Paths.get(first, more));
        return this;
    }
    
    public ExecAction workingDir(String first, String ... more) {
        this.pe.directory(Paths.get(first, more).toFile());
        return this;
    }
    
    public ExecAction workingDir(Path path) {
        this.pe.directory(path.toFile());
        return this;
    }
    
    public ExecAction environment(String name, String value) {
        this.pe.environment(name, value);
        return this;
    }
    
    public ExecAction environment(Map<String,String> env) {
        this.pe.environment(env);
        return this;
    }
    
    public ExecAction timeoutMillis(long timeout) {
        this.pe.timeout(timeout, TimeUnit.MILLISECONDS);
        return this;
    }
    
    public ExecAction timeout(long timeout, TimeUnit units) {
        this.pe.timeout(timeout, units);
        return this;
    }
    
    public ExecAction readOutput() {
        this.pe.readOutput(true);
        return this;
    }
    
    public ExecAction command(String executable, String ... arguments) throws Exception {
       this.executable = executable;
       this.arguments = Arrays.asList(arguments);
        return this;
    }
    
    @Override
    protected Result<ProcessResult> execute() throws Exception {
        File exeFile = WhichAction.findExecutable(context, paths, executable);
        if (exeFile == null) {
            throw new FileNotFoundException("Unable to find executable [" + executable + "]");
        }
        
        // build final list of command to execute (executable first then args)
        List<String> finalCommand = new ArrayList<>();
        finalCommand.add(exeFile.getAbsolutePath());
        finalCommand.addAll(arguments);
        
        this.pe.command(finalCommand);
        
        ProcessResult pr = this.pe.execute();
        return new Result(pr);
    }
}

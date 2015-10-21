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
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeoutException;
import org.zeroturnaround.exec.InvalidExitValueException;
import org.zeroturnaround.exec.ProcessExecutor;
import org.zeroturnaround.exec.ProcessResult;

/**
 *
 * @author joelauer
 */
public class Exec extends Action<ProcessResult> {

    final private Which which;
    final private ProcessExecutor executor;
    final private List<String> arguments;
    
    public Exec(Context context) {
        super(context);
        // which will be used to locate the executable
        this.which = new Which(context);
        this.arguments = new ArrayList<>();
        this.executor = new ProcessExecutor()
            .redirectInput(System.in)
            .redirectOutput(System.out)
            .redirectErrorStream(true)
            // initialize executable to context of current project basedir
            .directory(context.baseDir())
            .exitValueNormal();
    }
    
    public Exec command(String command, String ... arguments) {
        this.which.command(command);
        this.arguments.clear();
        this.arguments.addAll(Arrays.asList(arguments));
        return this;
    }
    
    public Exec arg(String ... arguments) {
        this.arguments.addAll(Arrays.asList(arguments));
        return this;
    }
    
    public Exec args(String ... arguments) {
        this.arguments.clear();
        this.arguments.addAll(Arrays.asList(arguments));
        return this;
    }
    
    public Exec path(Path path) {
        this.which.path(path);
        return this;
    }
    
    public Exec path(File path) {
        this.which.path(path);
        return this;
    }
    
    public Exec path(String path) {
        this.which.path(path);
        return this;
    }
    
    public Exec path(String first, String ... more) {
        this.which.path(first, more);
        return this;
    }

    public Exec env(String name, String value) {
        this.executor.environment(name, value);
        return this;
    }
    
    public Exec workingDir(Path path) {
        this.executor.directory(context.withBaseDir(path));
        return this;
    }
    
    public Exec workingDir(File path) {
        this.executor.directory(context.withBaseDir(path));
        return this;
    }
    
    public Exec workingDir(String dir) {
        this.executor.directory(context.withBaseDir(Paths.get(dir)));
        return this;
    }

    /**
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

    public ExecAction timeoutMillis(long timeout) {
        this.executor.timeout(timeout, TimeUnit.MILLISECONDS);
        return this;
    }
    
    public ExecAction timeout(long timeout, TimeUnit units) {
        this.executor.timeout(timeout, units);
        return this;
    }
    */
    
    /**
    public ExecAction readOutput() {
        this.executor.readOutput(true);
        return this;
    }
    */
    

    @Override
    public ProcessResult run() throws BlazeException {
        File exeFile = this.which.run();
        
        if (exeFile == null) {
            throw new BlazeException("Unable to find executable [" + this.which.getCommand() + "]");
        }
        
        // build final list of command to execute (executable first then args)
        List<String> command = new ArrayList<>();
        
        command.add(exeFile.getAbsolutePath());
        
        command.addAll(arguments);
        
        this.executor.command(command);
        
        try {
            return this.executor.execute();
            //return new Result(pr);
        } catch (IOException | InterruptedException | TimeoutException | InvalidExitValueException e) {
            throw new BlazeException("Unable to execute", e);
        }
    }
}

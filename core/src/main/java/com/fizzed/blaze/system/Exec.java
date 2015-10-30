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

import com.fizzed.blaze.core.ExecutableNotFoundException;
import com.fizzed.blaze.core.PathSupport;
import com.fizzed.blaze.Context;
import com.fizzed.blaze.core.Action;
import com.fizzed.blaze.core.BlazeException;
import com.fizzed.blaze.util.DeferredFileInputStream;
import com.fizzed.blaze.internal.ObjectHelper;
import com.fizzed.blaze.util.DeferredFileOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import org.apache.commons.io.output.NullOutputStream;
import org.zeroturnaround.exec.InvalidExitValueException;
import org.zeroturnaround.exec.ProcessExecutor;

/**
 *
 * @author joelauer
 */
public class Exec extends Action<ExecResult> implements PathSupport<Exec>, ExecSupport<Exec> {

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
            .redirectError(System.err)
            .exitValueNormal();
    }
    
    @Override
    public Exec command(String command, Object... arguments) {
        this.which.command(command);
        this.arguments.clear();
        this.arguments.addAll(ObjectHelper.toStringList(arguments));
        return this;
    }
    
    /**
     * Adds one or more arguments by appending to existing list.
     * @param arguments
     * @return 
     * @see #args(java.lang.Object...) For replacing existing arguments
     */
    @Override
    public Exec arg(Object... arguments) {
        this.arguments.addAll(ObjectHelper.toStringList(arguments));
        return this;
    }
    
    /**
     * Replaces existing arguments with one or more new arguments.
     * @param arguments
     * @return 
     * @see #arg(java.lang.Object...) For adding to existing arguments rather
     *      than replacing
     */
    @Override
    public Exec args(Object... arguments) {
        this.arguments.clear();
        this.arguments.addAll(ObjectHelper.toStringList(arguments));
        return this;
    }
    
    @Override
    public List<Path> getPaths() {
        return this.which.getPaths();
    }

    @Override
    public Exec env(String name, String value) {
        this.executor.environment(name, value);
        return this;
    }
    
    public Exec workingDir(Path path) {
        this.executor.directory(context.withBaseDir(path).toFile());
        return this;
    }
    
    public Exec workingDir(File path) {
        this.executor.directory(context.withBaseDir(path).toFile());
        return this;
    }
    
    public Exec workingDir(String dir) {
        this.executor.directory(context.withBaseDir(Paths.get(dir)).toFile());
        return this;
    }
    
    @Override
    public Exec captureOutput(boolean captureOutput) {
        if (captureOutput) {
            this.executor.redirectOutput(new NullOutputStream());
            this.executor.readOutput(true);
        } else {
            this.executor.redirectOutput(System.out);
            this.executor.readOutput(false);
        }
        return this;
    }
    
    @Override
    public Exec exitValues(Integer... exitValues) {
        this.executor.exitValues(exitValues);
        return this;
    }

    @Override
    public Exec timeout(long timeoutInMillis) {
        this.executor.timeout(timeoutInMillis, TimeUnit.MILLISECONDS);
        return this;
    }

    @Override
    public Exec pipeInput(InputStream pipeInput) {
        this.executor.redirectInput(pipeInput);
        return this;
    }

    @Override
    public Exec pipeOutput(OutputStream pipeOutput) {
        this.executor.redirectOutput(pipeOutput);
        return this;
    }

    @Override
    public Exec pipeError(OutputStream pipeError) {
        this.executor.redirectError(pipeError);
        return this;
    }
    
    @Override
    public Exec pipeErrorToOutput(boolean pipeErrorToOutput) {
        this.executor.redirectErrorStream(true);
        return this;
    }
    
    @Override
    protected ExecResult doRun() throws BlazeException {
        Path exeFile = this.which.run();
        
        if (exeFile == null) {
            throw new ExecutableNotFoundException("Executable '" + this.which.getCommand() + "' not found");
        }
        
        // build final list of command to execute (executable first then args)
        List<String> command = new ArrayList<>();
        
        command.add(exeFile.toAbsolutePath().toString());
        
        command.addAll(arguments);
        
        this.executor.command(command);
        
        try {
            return new ExecResult(this.executor.execute());
        } catch (IOException | InterruptedException | TimeoutException | InvalidExitValueException e) {
            throw new BlazeException("Unable to cleanly execute", e);
        }
    }
}

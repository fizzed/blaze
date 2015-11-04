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
import com.fizzed.blaze.Context;
import com.fizzed.blaze.core.Action;
import com.fizzed.blaze.core.BlazeException;
import com.fizzed.blaze.util.ObjectHelper;
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
import com.fizzed.blaze.core.PathsMixin;
import com.fizzed.blaze.util.NamedStream;
import java.util.Arrays;

/**
 *
 * @author joelauer
 */
public class Exec extends Action<ExecResult> implements PathsMixin<Exec>, ExecSupport<Exec> {

    final private Which which;
    final private ProcessExecutor executor;
    final private List<String> arguments;
    private NamedStream<InputStream> pipeInput;
    private NamedStream<OutputStream> pipeOutput;
    private NamedStream<OutputStream> pipeError;
    final private List<Integer> exitValues;
    
    public Exec(Context context) {
        super(context);
        // which will be used to locate the executable
        this.which = new Which(context);
        this.arguments = new ArrayList<>();
        this.executor = new ProcessExecutor()
            .exitValueNormal();
        this.pipeInput = NamedStream.STDIN;
        this.pipeOutput = NamedStream.STDOUT;
        this.pipeError = NamedStream.STDERR;
        this.exitValues = new ArrayList<>();
        this.exitValues.add(0);  
    }
    
    @Override
    public Exec command(String command) {
        this.which.command(command);
        return this;
    }
    
    @Override
    public Exec arg(Object argument) {
        this.arguments.add(ObjectHelper.nonNullToString(argument));
        return this;
    }

    @Override
    public Exec args(Object... arguments) {
        this.arguments.addAll(ObjectHelper.nonNullToStringList(arguments));
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
            pipeOutput(NamedStream.NULLOUT);
            this.executor.readOutput(true);
        } else {
            pipeOutput(NamedStream.STDOUT);
            this.executor.readOutput(false);
        }
        return this;
    }
    
    @Override
    public Exec exitValues(Integer... exitValues) {
        this.executor.exitValues(exitValues);
        this.exitValues.clear();
        this.exitValues.addAll(Arrays.asList(exitValues));
        return this;
    }

    @Override
    public Exec timeout(long timeoutInMillis) {
        this.executor.timeout(timeoutInMillis, TimeUnit.MILLISECONDS);
        return this;
    }
    
    @Override
    public Exec pipeInput(NamedStream<InputStream> pipeInput) {
        this.pipeInput = pipeInput;
        return this;
    }
    
    @Override
    public Exec pipeOutput(NamedStream<OutputStream> pipeOutput) {
        this.pipeOutput = pipeOutput;
        return this;
    }
    
    @Override
    public Exec pipeError(NamedStream<OutputStream> pipeError) {
        this.pipeError = pipeError;
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
        
        this.executor
            .command(command)
            .redirectInput(pipeInput.stream())
            .redirectOutput(pipeOutput.stream())
            .redirectError(pipeError.stream());
        
        try {
            return new ExecResult(this.executor.execute());
        } catch (InvalidExitValueException e) {
            throw new com.fizzed.blaze.core.UnexpectedExitValueException("Process exited with unexpected value", this.exitValues, e.getExitValue());
        } catch (IOException | InterruptedException | TimeoutException e) {
            throw new BlazeException("Unable to cleanly execute process", e);
        }
    }
}

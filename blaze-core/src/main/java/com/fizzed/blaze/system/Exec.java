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

import com.fizzed.blaze.core.ExecMixin;
import com.fizzed.blaze.core.ExecutableNotFoundException;
import com.fizzed.blaze.Context;
import com.fizzed.blaze.core.Action;
import com.fizzed.blaze.core.BlazeException;
import com.fizzed.blaze.util.ObjectHelper;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import org.zeroturnaround.exec.InvalidExitValueException;
import org.zeroturnaround.exec.ProcessExecutor;
import com.fizzed.blaze.core.PathsMixin;
import com.fizzed.blaze.util.InterruptibleInputStream;
import com.fizzed.blaze.util.StreamableInput;
import com.fizzed.blaze.util.StreamableOutput;
import com.fizzed.blaze.util.Streamables;
import java.io.InputStream;
import java.util.Arrays;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zeroturnaround.exec.ProcessResult;
import org.zeroturnaround.exec.stream.PumpStreamHandler;

/**
 *
 * @author joelauer
 */
public class Exec extends Action<Exec.Result,Integer> implements PathsMixin<Exec>, ExecMixin<Exec> {
    private final static Logger log = LoggerFactory.getLogger(Exec.class);

    final private Which which;
    final private ProcessExecutor executor;
    final private List<String> arguments;
    private StreamableInput pipeInput;
    private StreamableOutput pipeOutput;
    private StreamableOutput pipeError;
    private boolean pipeErrorToOutput;
    final private List<Integer> exitValues;
    
    public Exec(Context context) {
        super(context);
        // which will be used to locate the executable
        this.which = new Which(context);
        this.arguments = new ArrayList<>();
        this.executor = new ProcessExecutor()
            .exitValueNormal();
        this.pipeInput = Streamables.standardInput();
        this.pipeOutput = Streamables.standardOutput();
        this.pipeError = Streamables.standardError();
        this.exitValues = new ArrayList<>();
        this.exitValues.add(0);  
    }
    
    @Override
    public Exec command(Path command) {
        this.which.command(command);
        return this;
    }

    @Override
    public Exec command(File command) {
        this.which.command(command);
        return this;
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
    public StreamableInput getPipeInput() {
        return this.pipeInput;
    }

    @Override
    public Exec pipeInput(StreamableInput pipeInput) {
        this.pipeInput = pipeInput;
        return this;
    }

    @Override
    public StreamableOutput getPipeOutput() {
        return this.pipeOutput;
    }
    
    @Override
    public Exec pipeOutput(StreamableOutput pipeOutput) {
        this.pipeOutput = pipeOutput;
        return this;
    }
    
    @Override
    public StreamableOutput getPipeError() {
        return this.pipeError;
    }
    
    @Override
    public Exec pipeError(StreamableOutput pipeError) {
        this.pipeError = pipeError;
        return this;
    }
    
    @Override
    public Exec pipeErrorToOutput(boolean pipeErrorToOutput) {
        this.pipeErrorToOutput = pipeErrorToOutput;
        return this;
    }
    
    @Override
    protected Result doRun() throws BlazeException {
        Path exeFile = this.which.run();
        
        if (exeFile == null) {
            throw new ExecutableNotFoundException("Executable '" + this.which.getCommand() + "' not found");
        }
        
        // build final list of command to execute (executable first then args)
        List<String> command = new ArrayList<>();
        
        command.add(exeFile.toAbsolutePath().toString());
        
        command.addAll(arguments);
        
        // use a custom streampumper so we can more accuratly handle inputstream
        final InputStream is = (pipeInput != null ? new InterruptibleInputStream(pipeInput.stream()) : null);
        final OutputStream os = (pipeOutput != null ? pipeOutput.stream() : null);
        final OutputStream es = (pipeErrorToOutput ? os : (pipeError != null ? pipeError.stream() : null));
        
        PumpStreamHandler streams = new PumpStreamHandler(os, es, is) {
            @Override
            public void stop() {
                // NOTE: travis ci deadlocks unless we add this -- never happens
                // on a real system so its pretty odd
                Thread.yield();
                
                // make sure any input, output, and error streams are closed
                // before the superclass stop() is triggered
                Streamables.closeQuietly(is);
                //Streamables.closeQuietly(os);
                //Streamables.closeQuietly(es);
                
                super.stop();
            }
        };
        
        this.executor
            .command(command)
            .streams(streams);
        
        try {
            ProcessResult processResult = this.executor.execute();
            return new Result(this, processResult.getExitValue());
        } catch (InvalidExitValueException e) {
            throw new com.fizzed.blaze.core.UnexpectedExitValueException("Process exited with unexpected value", this.exitValues, e.getExitValue());
        } catch (IOException | InterruptedException | TimeoutException e) {
            throw new BlazeException("Unable to cleanly execute process", e);
        } finally {
            // close all the output streams (input stream closed above)
            Streamables.close(os);
            Streamables.close(es);
        }
    }
    
    static public class Result extends com.fizzed.blaze.core.Result<Exec,Integer,Result> {
        
        Result(Exec action, Integer value) {
            super(action, value);
        }
        
    }
    
}

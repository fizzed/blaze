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
import com.fizzed.blaze.Context;
import com.fizzed.blaze.core.Action;
import com.fizzed.blaze.core.BlazeException;
import com.fizzed.blaze.util.ObjectHelper;
import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import com.fizzed.blaze.core.PathsMixin;
import com.fizzed.blaze.util.CaptureOutput;
import com.fizzed.blaze.util.StreamableInput;
import com.fizzed.blaze.util.StreamableOutput;
import com.fizzed.blaze.util.Streamables;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

abstract public class Exec<T extends Exec> extends Action<Exec.Result<T>,Integer> implements PathsMixin<T>, ExecMixin<T> {
    protected final Logger log = LoggerFactory.getLogger(this.getClass());

    static public class Result<R extends Exec> extends com.fizzed.blaze.core.Result<R,Integer,Result<R>> {
        
        public Result(R action, Integer value) {
            super(action, value);
        }
        
    }
    
    protected final Map<String,String> environment;
    protected Path command;
    protected Path workingDirectory;
    final protected List<String> arguments;
    protected StreamableInput pipeInput;
    protected StreamableOutput pipeOutput;
    protected StreamableOutput pipeError;
    protected boolean pipeErrorToOutput;
    final protected List<Integer> exitValues;
    protected long timeoutMillis = -1L;
    protected boolean sudo;
    protected boolean shell;
    
    public Exec(Context context) {
        super(context);
        this.environment = new LinkedHashMap<>();
        this.arguments = new ArrayList<>();
        this.pipeInput = Streamables.standardInput();
        this.pipeOutput = Streamables.standardOutput();
        this.pipeError = Streamables.standardError();
        this.exitValues = new ArrayList<>();
        this.exitValues.add(0);
        this.sudo = false;
        this.shell = false;
    }    

    public T sudo(boolean sudo) {
        this.sudo = sudo;
        return (T)this;
    }

    public T shell(boolean shell) {
        this.shell = shell;
        return (T)this;
    }
    
    @Override
    public T command(Path command) {
        Objects.requireNonNull(command, "command was null");
        this.command = command;
        return (T)this;
    }

    @Override
    public T command(File command) {
        Objects.requireNonNull(command, "command was null");
        this.command = command.toPath();
        return (T)this;
    }
    
    @Override
    public T command(String command) {
        Objects.requireNonNull(command, "command was null");
        this.command = Paths.get(command);
        return (T)this;
    }
    
    @Override
    public T arg(Object argument) {
        this.arguments.add(ObjectHelper.nonNullToString(argument));
        return (T)this;
    }

    @Override
    public T args(Object... arguments) {
        this.arguments.addAll(ObjectHelper.nonNullToStringList(arguments));
        return (T)this;
    }
    
    @Override
    public T env(String name, String value) {
        this.environment.put(name, value);
        return (T)this;
    }
    
    public T workingDir(Path path) {
        Objects.requireNonNull(path, "path cannot be null");
//        this.executor.directory(path.toFile());
        this.workingDirectory = path;
        return (T)this;
    }
    
    public T workingDir(File path) {
        Objects.requireNonNull(path, "path cannot be null");
//        this.executor.directory(path);
        this.workingDirectory = path.toPath();
        return (T)this;
    }
    
    public T workingDir(String path) {
        Objects.requireNonNull(path, "path cannot be null");
//        this.executor.directory(Paths.get(path).toFile());
        this.workingDirectory = Paths.get(path);
        return (T)this;
    }
    
    @Override
    public T exitValues(Integer... exitValues) {
        this.exitValues.clear();
        this.exitValues.addAll(Arrays.asList(exitValues));
//        this.executor.exitValues(exitValues);
//        this.exitValues.clear();
//        this.exitValues.addAll(Arrays.asList(exitValues));
        return (T)this;
    }

    @Override
    public T timeout(long timeoutInMillis) {
//        this.executor.timeout(timeoutInMillis, TimeUnit.MILLISECONDS);
        this.timeoutMillis = timeoutInMillis;
        return (T)this;
    }
    
    @Override
    public StreamableInput getPipeInput() {
        return this.pipeInput;
    }

    @Override
    public T pipeInput(StreamableInput pipeInput) {
        this.pipeInput = pipeInput;
        return (T)this;
    }

    @Override
    public StreamableOutput getPipeOutput() {
        return this.pipeOutput;
    }
    
    @Override
    public T pipeOutput(StreamableOutput pipeOutput) {
        this.pipeOutput = pipeOutput;
        return (T)this;
    }
    
    @Override
    public StreamableOutput getPipeError() {
        return this.pipeError;
    }
    
    @Override
    public T pipeError(StreamableOutput pipeError) {
        this.pipeError = pipeError;
        return (T)this;
    }
    
    @Override
    public T pipeErrorToOutput(boolean pipeErrorToOutput) {
        this.pipeErrorToOutput = pipeErrorToOutput;
        return (T)this;
    }
 
    // TO FIX CODE COMPLETION, THESE MIXINS NEED SOME CONCRETE IMPLS...
    
    @Override
    public CaptureOutput runCaptureOutput() throws BlazeException {
        return ExecMixin.super.runCaptureOutput();
    }

    @Override
    public T pipeErrorToOutput() {
        return ExecMixin.super.pipeErrorToOutput();
    }

    @Override
    public T exitValue(Integer exitValue) {
        return ExecMixin.super.exitValue(exitValue);
    }

    @Override
    public T timeout(long timeout, TimeUnit units) {
        return ExecMixin.super.timeout(timeout, units);
    }

    @Override
    public T disablePipeOutput() {
        return ExecMixin.super.disablePipeOutput(); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public T pipeOutput(File file) {
        return ExecMixin.super.pipeOutput(file); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public T pipeOutput(Path path) {
        return ExecMixin.super.pipeOutput(path); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public T pipeOutput(OutputStream stream) {
        return ExecMixin.super.pipeOutput(stream); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public T disablePipeInput() {
        return ExecMixin.super.disablePipeInput(); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public T pipeInput(String text, Charset charset) {
        return ExecMixin.super.pipeInput(text, charset); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public T pipeInput(String text) {
        return ExecMixin.super.pipeInput(text); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public T pipeInput(File file) {
        return ExecMixin.super.pipeInput(file); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public T pipeInput(Path path) {
        return ExecMixin.super.pipeInput(path); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public T pipeInput(InputStream stream) {
        return ExecMixin.super.pipeInput(stream); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public T disablePipeError() {
        return ExecMixin.super.disablePipeError(); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public T pipeError(File file) {
        return ExecMixin.super.pipeError(file); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public T pipeError(Path path) {
        return ExecMixin.super.pipeError(path); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public T pipeError(OutputStream stream) {
        return ExecMixin.super.pipeError(stream); //To change body of generated methods, choose Tools | Templates.
    }

}
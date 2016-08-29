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
package com.fizzed.blaze.ssh;

import com.fizzed.blaze.Context;
import com.fizzed.blaze.core.Action;
import com.fizzed.blaze.util.ObjectHelper;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.fizzed.blaze.util.StreamableInput;
import com.fizzed.blaze.util.StreamableOutput;
import com.fizzed.blaze.util.Streamables;
import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import com.fizzed.blaze.core.ExecMixin;

abstract public class SshExec extends Action<SshExec.Result,Integer> implements ExecMixin<SshExec> {
    static private final Logger log = LoggerFactory.getLogger(SshExec.class);

    final protected SshSession session;
    protected Path command;
    final protected List<String> arguments;
    protected StreamableInput pipeInput;
    protected StreamableOutput pipeOutput;
    protected StreamableOutput pipeError;
    protected boolean pipeErrorToOutput;
    protected Map<String,String> environment;
    protected boolean pty;
    protected long timeout;
    final protected List<Integer> exitValues;
    
    public SshExec(Context context, SshSession session) {
        super(context);
        this.pipeInput = Streamables.standardInput();
        this.pipeOutput = Streamables.standardOutput();
        this.pipeError = Streamables.standardError();
        this.pipeErrorToOutput = false;
        this.session = session;
        this.arguments = new ArrayList<>();
        this.pty = false;
        this.exitValues = new ArrayList<>(Arrays.asList(0));
    }
    
    @Override
    public SshExec command(String command) {
        ObjectHelper.requireNonNull(command, "command cannot be null");
        this.command = Paths.get(command);
        return this;
    }
    
    @Override
    public SshExec command(Path command) {
        ObjectHelper.requireNonNull(command, "command cannot be null");
        this.command = command;
        return this;
    }
    
    @Override
    public SshExec command(File command) {
        ObjectHelper.requireNonNull(command, "command cannot be null");
        this.command = command.toPath();
        return this;
    }
    
    @Override
    public SshExec arg(Object argument) {
        this.arguments.add(ObjectHelper.nonNullToString(argument));
        return this;
    }

    @Override
    public SshExec args(Object... arguments) {
        this.arguments.addAll(ObjectHelper.nonNullToStringList(arguments));
        return this;
    }
    
    @Override
    public StreamableInput getPipeInput() {
        return this.pipeInput;
    }
    
    @Override
    public SshExec pipeInput(StreamableInput pipeInput) {
        this.pipeInput = pipeInput;
        return this;
    }

    @Override
    public StreamableOutput getPipeOutput() {
        return this.pipeOutput;
    }

    @Override
    public SshExec pipeOutput(StreamableOutput pipeOutput) {
        this.pipeOutput = pipeOutput;
        return this;
    }
    
    @Override
    public StreamableOutput getPipeError() {
        return this.pipeError;
    }
    
    @Override
    public SshExec pipeError(StreamableOutput pipeError) {
        this.pipeError = pipeError;
        return this;
    }

    @Override
    public SshExec pipeErrorToOutput(boolean pipeErrorToOutput) {
        this.pipeErrorToOutput = pipeErrorToOutput;
        return this;
    }

    @Override
    public SshExec env(String name, String value) {
        if (this.environment == null) {
            this.environment = new HashMap<>();
        }
        this.environment.put(name, value);
        return this;
    }

    @Override
    public SshExec timeout(long timeoutInMillis) {
        this.timeout = timeoutInMillis;
        return this;
    }
    
    @Override
    public SshExec exitValues(Integer... exitValues) {
        this.exitValues.clear();
        this.exitValues.addAll(Arrays.asList(exitValues));
        return this;
    }
    
    public SshExec pty(boolean value) {
        this.pty = value;
        return this;
    }

    static public class Result extends com.fizzed.blaze.core.Result<SshExec,Integer,Result> {
        
        public Result(SshExec action, Integer value) {
            super(action, value);
        }
        
    }
    
}

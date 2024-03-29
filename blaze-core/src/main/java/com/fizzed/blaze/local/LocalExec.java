/*
 * Copyright 2020 Fizzed, Inc.
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
package com.fizzed.blaze.local;

import com.fizzed.blaze.core.ExecutableNotFoundException;
import com.fizzed.blaze.Context;
import com.fizzed.blaze.core.BlazeException;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import com.fizzed.blaze.util.CommandLines;
import org.zeroturnaround.exec.InvalidExitValueException;
import org.zeroturnaround.exec.ProcessExecutor;
import com.fizzed.blaze.system.Exec;
import com.fizzed.blaze.system.Which;
import com.fizzed.blaze.util.InputStreamPumper;
import com.fizzed.blaze.util.Streamables;
import java.io.InputStream;
import org.zeroturnaround.exec.ProcessResult;
import org.zeroturnaround.exec.stream.PumpStreamHandler;

public class LocalExec extends Exec<LocalExec> {
    
    final protected Which which;
    
    public LocalExec(Context context) {
        super(context);
        this.which = new Which(context);
    }
    
    @Override
    public LocalExec command(Path command) {
        this.which.command(command);
        return this;
    }

    @Override
    public LocalExec command(File command) {
        this.which.command(command);
        return this;
    }
    
    @Override
    public LocalExec command(String command) {
        this.which.command(command);
        return this;
    }
    
    @Override
    public List<Path> getPaths() {
        return this.which.getPaths();
    }
    
    @Override
    protected Exec.Result doRun() throws BlazeException {
        Path exeFile = this.which.run();
        
        if (exeFile == null) {
            throw new ExecutableNotFoundException("Executable '" + this.which.getCommand() + "' not found");
        }
        

        final ProcessExecutor executor = new ProcessExecutor();

        
        if (this.environment.size() > 0) {
            this.environment.forEach((k,v) -> executor.environment(k, v));
        }
        
        if (this.workingDirectory != null) {
            executor.directory(this.workingDirectory.toFile());
        }
        
        if (this.exitValues != null && this.exitValues.size() > 0) {
            executor.exitValues(this.exitValues.toArray(new Integer[0]));
        }
        
        if (this.timeoutMillis > 0) {
            executor.timeout(timeoutMillis, TimeUnit.MILLISECONDS);
        }

        
        // build final list of command to execute (executable first then args)
        final List<String> finalCommand = new ArrayList<>();
        
        if (this.sudo) {
//            if (commands.containsKey("doas")) {
//                arguments.add("doas");
//            } else {
                // man sudo
                // -S  The -S (stdin) option causes sudo to read the password from the
                //     standard input instead of the terminal device.
                finalCommand.add("sudo");
                finalCommand.add("-S");
//            }
        }

        if (this.shell) {
            finalCommand.add("sh");
            finalCommand.add("-c");
        }
        
        finalCommand.add(exeFile.toAbsolutePath().toString());
        
        finalCommand.addAll(this.arguments);
        
        // use a custom streampumper so we can more accuratly handle inputstream
        final InputStream is = (this.pipeInput != null ? this.pipeInput.stream() : null);
        final OutputStream os = (this.pipeOutput != null ? this.pipeOutput.stream() : null);
        final OutputStream es = (this.pipeErrorToOutput ? os : (this.pipeError != null ? this.pipeError.stream() : null));
        
        PumpStreamHandler streams = new PumpStreamHandler(os, es, is) {
            @Override
            protected Thread createSystemInPump(InputStream is, OutputStream os) {
                InputStreamPumper pumper = new InputStreamPumper(is, os);
                final Thread result = new Thread(pumper);
                result.setDaemon(true);
                return result;
            }
            
            @Override
            public void stop() {
                // NOTE: travis ci deadlocks unless we add this -- never happens
                // on a real system so its pretty odd
//                Thread.yield();
                
                // make sure any input, output, and error streams are closed
                // before the superclass stop() is triggered
                Streamables.closeQuietly(is);
                //Streamables.closeQuietly(os);
                //Streamables.closeQuietly(es);
                
                super.stop();
            }
        };

        if (log.isVerbose()) {
            // build a verbose string representing the executable command we are about to run
            String cmd = CommandLines.debug(finalCommand);
            String workingDir = "";
            String env = "";
            if (this.workingDirectory != null) {
                workingDir = " in working dir [" + this.workingDirectory + "]";
            }
            if (!this.environment.isEmpty()) {
                env = " with env " + this.environment;
            }
            log.verbose("Exec [{}]{}{}", cmd, workingDir, env);
        }

        executor
            .command(finalCommand)
            .streams(streams);
        
        try {
            ProcessResult processResult = executor.execute();
            
            return new Exec.Result(this, processResult.getExitValue());
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
    
    static public class Result extends com.fizzed.blaze.core.Result<LocalExec,Integer,Result> {
        
        Result(LocalExec action, Integer value) {
            super(action, value);
        }
        
    }
    
}

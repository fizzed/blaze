package com.fizzed.blaze.local;

import com.fizzed.blaze.core.ExecutableNotFoundException;
import com.fizzed.blaze.Context;
import com.fizzed.blaze.core.BlazeException;
import java.io.File;
import java.io.OutputStream;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import com.fizzed.blaze.core.UnexpectedExitValueException;
import com.fizzed.blaze.internal.IntRangeHelper;
import com.fizzed.blaze.util.*;
import org.zeroturnaround.exec.InvalidExitValueException;
import org.zeroturnaround.exec.ProcessExecutor;
import com.fizzed.blaze.system.Exec;
import com.fizzed.blaze.system.Which;

import java.io.InputStream;
import org.zeroturnaround.exec.ProcessResult;
import org.zeroturnaround.exec.StartedProcess;
import org.zeroturnaround.exec.stream.PumpStreamHandler;

public class LocalExec extends Exec {
    
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

        
        if (!this.environment.isEmpty()) {
            this.environment.forEach(executor::environment);
        }
        
        if (this.workingDirectory != null) {
            executor.directory(this.workingDirectory.toFile());
        }
        
        if (this.exitValues != null && !this.exitValues.isEmpty()) {
            executor.exitValues(IntRangeHelper.toExpandedArray(this.exitValues));
        } else {
            // we should ignore the exit values entirely
            executor.exitValueAny();
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

        // is this a powershell command?
        if (exeFile.getFileName().toString().toLowerCase().endsWith(".ps1")) {
            // we need to actually execute powershell.exe
            final Path powershellExe = new Which(context)
                .command("powershell")
                .run();

            finalCommand.add(powershellExe.toAbsolutePath().toString());
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
            final StartedProcess startedProcess = executor.start();

            // register process for reaping (cleaning up...)
            ProcessReaper.INSTANCE.register(startedProcess.getProcess());
            try {
                ProcessResult processResult = startedProcess.getFuture().get();

                return new Exec.Result(this, processResult.getExitValue());
            } finally {
                ProcessReaper.INSTANCE.unregister(startedProcess.getProcess());
            }
        } catch (Throwable t) {
            if (t instanceof ExecutionException) {
                ExecutionException ee = (ExecutionException)t;
                if (ee.getCause() instanceof InvalidExitValueException) {
                    // this is actually what we want to process
                    t = ee.getCause();
                }
            }

            if (t instanceof InvalidExitValueException) {
                InvalidExitValueException ievae = (InvalidExitValueException)t;

                // this can happen IF we're in the process of being shutdown and we actually don't want to throw an exception
                if (ProcessReaper.INSTANCE.isShuttingDown()) {
                    log.trace("Shutting down, ignoring invalid exit code on exec()");
                    return new Exec.Result(this, ievae.getExitValue());
                }

                throw new UnexpectedExitValueException("Process exited with unexpected value", this.exitValues, ievae.getExitValue());
            }

            throw new BlazeException("Unable to cleanly execute process", t);
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

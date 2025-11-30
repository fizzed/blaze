package com.fizzed.blaze.system;

import com.fizzed.blaze.Context;
import com.fizzed.blaze.core.*;
import com.fizzed.blaze.util.*;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.TimeUnit;

import static com.fizzed.blaze.util.IntRange.intRange;
import static java.util.Arrays.asList;

abstract public class Exec extends Action<Exec.Result,Integer> implements VerbosityMixin<Exec>, PathsMixin<Exec>, PipeErrorMixin<Exec> {

    static public class Result extends com.fizzed.blaze.core.Result<Exec,Integer,Result> {
        public Result(Exec action, Integer value) {
            super(action, value);
        }
    }

    protected final VerboseLogger log;
    protected final Map<String,String> environment;
    // to re-use command for ssh, it needs to be a string as a local Path != remote path
    protected String command;
    protected Path workingDirectory;
    final protected List<String> arguments;
    protected StreamableInput pipeInput;
    protected StreamableOutput pipeOutput;
    protected StreamableOutput pipeError;
    protected boolean pipeErrorToOutput;
    final protected List<IntRange> exitValues;
    protected long timeoutMillis = -1L;
    protected boolean sudo;
    protected boolean shell;

    public Exec(Context context) {
        super(context);
        this.log = new VerboseLogger(this);
        this.environment = new LinkedHashMap<>();
        this.arguments = new ArrayList<>();
        this.pipeInput = Streamables.standardInput();
        this.pipeOutput = Streamables.standardOutput();
        this.pipeError = Streamables.standardError();
        this.exitValues = new ArrayList<>();
        this.exitValues.add(intRange(0, 0));
        this.sudo = false;
        this.shell = false;
    }

    // ONLY THING THAT FIXED CODE COMPLETION WAS TO INCLUDE MIXIN METHODS HERE TOO

    @Override
    public VerboseLogger getVerboseLogger() {
        return this.log;
    }

    public Exec sudo(boolean sudo) {
        this.sudo = sudo;
        return this;
    }

    public Exec shell(boolean shell) {
        this.shell = shell;
        return this;
    }

    public Exec command(Path command) {
        Objects.requireNonNull(command, "command was null");
        this.command = command.toString();
        return this;
    }

    public Exec command(File command) {
        Objects.requireNonNull(command, "command was null");
        this.command = command.toPath().toString();
        return this;
    }

    public Exec command(String command) {
        Objects.requireNonNull(command, "command was null");
        this.command = command;
        return this;
    }

    public Exec arg(Object argument) {
        // was this accidentally a collection or array?
        if (argument != null && (argument instanceof Collection || argument.getClass().isArray())) {
            throw new IllegalArgumentException("Use args() instead of arg() with a collection or array");
        }
        this.arguments.add(ObjectHelper.nonNullToString(argument));
        return this;
    }

    public Exec args(Collection<?> arguments) {
        this.arguments.addAll(ObjectHelper.nonNullToStringList(arguments));
        return this;
    }

    public Exec args(Object... arguments) {
        this.arguments.addAll(ObjectHelper.nonNullToStringList(arguments));
        return this;
    }

    public Exec env(String name, String value) {
        this.environment.put(name, value);
        return this;
    }

    public Exec env(Map<String,String> environment) {
        this.environment.putAll(environment);
        return this;
    }

    public Exec workingDir(Path path) {
        Objects.requireNonNull(path, "path cannot be null");
        this.workingDirectory = path;
        return this;
    }

    public Exec workingDir(File path) {
        Objects.requireNonNull(path, "path cannot be null");
        this.workingDirectory = path.toPath();
        return this;
    }

    public Exec workingDir(String path) {
        Objects.requireNonNull(path, "path cannot be null");
        this.workingDirectory = Paths.get(path);
        return this;
    }

    public Exec timeout(long timeout, TimeUnit units) {
        this.timeout(TimeUnit.MILLISECONDS.convert(timeout, units));
        return this;
    }

    public Exec exitValue(Integer exitValue) {
        return exitValues(new Integer[] { exitValue });
    }

    public Exec exitValuesAny() {
        this.exitValues.clear();
        return this;
    }

    public Exec exitValues(Integer... exitValues) {
        this.exitValues.clear();
        for (Integer exitValue : exitValues) {
            this.exitValues.add(intRange(exitValue, exitValue));
        }
        return this;
    }

    public Exec exitValues(IntRange... exitValues) {
        this.exitValues.clear();
        this.exitValues.addAll(asList(exitValues));
        return this;
    }

    public Exec timeout(long timeoutInMillis) {
        this.timeoutMillis = timeoutInMillis;
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

    public Exec pipeErrorToOutput() {
        return this.pipeErrorToOutput(true);
    }

    public Exec pipeErrorToOutput(boolean pipeErrorToOutput) {
        this.pipeErrorToOutput = pipeErrorToOutput;
        return this;
    }

    /**
     * Helper method to make it easier to exec a program and capture its output. By default this will still also
     * print out the program output to the console (stdout). You can see {@link #runCaptureOutput(boolean)} to disable this.
     * @return The captured output stream
     * @throws BlazeException
     */
    public CaptureOutput runCaptureOutput() throws BlazeException {
        CaptureOutput captureOutput = null;
        StreamableOutput output = getPipeOutput();

        // already set as capture output?
        if (output != null && output instanceof CaptureOutput) {
            captureOutput = (CaptureOutput)output;
        } else {
            captureOutput = Streamables.captureOutput();
            this.pipeOutput(captureOutput);
        }

        this.run();

        return captureOutput;
    }

    /**
     * Executes the command defined in the current context while capturing its output.
     * Optionally, the standard output can be included in the captured output.
     *
     * @param includeStdOut a boolean indicating whether the standard output will also receive a copy of the captured output
     * @return the captured output of the executed command encapsulated in a {@link CaptureOutput} instance
     * @throws BlazeException if an error occurs while executing the command
     */
    public CaptureOutput runCaptureOutput(boolean includeStdOut) throws BlazeException {
        CaptureOutput captureOutput = null;
        StreamableOutput output = getPipeOutput();

        // already set as capture output?
        if (output != null && output instanceof CaptureOutput) {
            captureOutput = (CaptureOutput)output;
        } else {
            captureOutput = Streamables.captureOutput(includeStdOut);
            this.pipeOutput(captureOutput);
        }

        this.run();

        return captureOutput;
    }

}
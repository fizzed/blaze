package com.fizzed.blaze.util;

import org.zeroturnaround.exec.ProcessExecutor;
import org.zeroturnaround.exec.ProcessResult;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class BlazeRunner {

    static public ProcessResult invokeWithCurrentJvmHome(File blazeScriptFile, List<String> blazeArgs, List<String> scriptArgs) throws IOException, InterruptedException, TimeoutException {
        return invokeWithCurrentJvmHome(blazeScriptFile, blazeArgs, scriptArgs, null);
    }

    static public ProcessResult invokeWithCurrentJvmHome(File blazeScriptFile, List<String> blazeArgs, List<String> scriptArgs, File workingDirectory) throws IOException, InterruptedException, TimeoutException {
        // get current classpath
        String classpath = System.getProperty("java.class.path");

        // build a temporary home directory (that classes will be compiled to)
        //Path tempHomeDir = Paths.get(System.getProperty("java.io.tmpdir")).resolve("blaze-test-"+ UUID.randomUUID());
        //Files.createDirectories(tempHomeDir);
        try {
            final List<String> commands = new ArrayList<>();

            commands.add("java");
            commands.add("-cp");
            commands.add(classpath);
            commands.add(com.fizzed.blaze.cli.Bootstrap.class.getCanonicalName());

            if (blazeArgs != null) {
                commands.addAll(blazeArgs);
            }

            if (blazeScriptFile != null) {
                commands.add(blazeScriptFile.getAbsolutePath());
            }

            if (scriptArgs != null) {
                commands.addAll(scriptArgs);
            }

            return new ProcessExecutor()
                .command(commands)
                .redirectOutput(System.out)
                .readOutput(true)
                .directory(workingDirectory)
                //.environment("HOME", tempHomeDir.toAbsolutePath().toString())
                .timeout(10, TimeUnit.SECONDS)
                .execute();
        } finally {
            //Systems.remove(tempHomeDir).recursive().force().run();
        }
    }

}
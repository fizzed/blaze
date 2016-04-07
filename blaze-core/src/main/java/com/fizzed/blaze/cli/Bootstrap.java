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
package com.fizzed.blaze.cli;

import com.fizzed.blaze.Version;
import com.fizzed.blaze.core.Blaze;
import com.fizzed.blaze.core.MessageOnlyException;
import com.fizzed.blaze.core.NoSuchTaskException;
import com.fizzed.blaze.core.DependencyResolveException;
import com.fizzed.blaze.core.WrappedBlazeException;
import com.fizzed.blaze.internal.InstallHelper;
import com.fizzed.blaze.util.Timer;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Deque;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Bootstrap {
    
    static public void main(String[] args) throws IOException {
        new Bootstrap().run(args);
    }
    
    protected Path blazeFile = null;
    protected Path blazeDir = null;
    protected List<String> tasks;
    
    public Bootstrap() {
        this.tasks = new ArrayList<>();
    }

    public void run(String[] args) throws IOException {
        run(new ArrayDeque(Arrays.asList(args)));
    }
    
    @SuppressWarnings("ThrowableResultIgnored")
    public void run(Deque<String> args) throws IOException {
        Thread.currentThread().setName(getName());

        boolean listTasks = false;

        while (!args.isEmpty()) {
            String arg = args.remove();
            
            if (this.interceptArgument(arg, args)) {
                continue;       // move on
            }
            
            if (arg.startsWith("-D")) {
                // strip -D then split on =
                String[] tokens = arg.substring(2).split("=");
                if (tokens.length == 1) {
                    systemProperty(tokens[0], "");
                } else {
                    systemProperty(tokens[0], tokens[1]);
                }
            } else if (arg.equals("-v") || arg.equals("--version")) {
                printVersion();
                System.exit(0);
            } else if (arg.equals("-q") || arg.equals("-qq") || arg.equals("-x") || arg.equals("-xx") || arg.equals("-xxx")) {
                configureLogging(arg);
            } else if (arg.equals("-h") || arg.equals("--help")) {
                printHelp();
                System.exit(0);
            } else if (arg.equals("-f") || arg.equals("--file")) {
                String nextArg = nextArg(args, arg, "<file>");
                blazeFile = Paths.get(nextArg);
            } else if (arg.equals("-d") || arg.equals("--dir")) {
                String nextArg = nextArg(args, arg, "<dir>");
                blazeDir = Paths.get(nextArg);
            } else if (arg.equals("-i") || arg.equals("--install")) {
                String nextArg = nextArg(args, arg, "<dir>");
                Path installDir = Paths.get(nextArg);
                
                try {
                    List<Path> installedFiles = InstallHelper.installBlazeBinaries(installDir);
                    for (Path installedFile : installedFiles) {
                        System.out.println("Installed " + installedFile);
                    }
                    System.exit(0);
                } catch (MessageOnlyException e) {
                    System.err.println("[ERROR] " + e.getMessage());
                    System.exit(1);
                }
            } else if (arg.equals("-l") || arg.equals("--list")) {
                listTasks = true;
            } else if (arg.startsWith("-")) {
                System.err.println("[ERROR] Unsupported command line switch [" + arg + "]; " + getName() + " -h for more info");
                System.exit(1);
            } else {
                // this may be a task to run - special case for first occurrence
                // which may be a script to run
                if (tasks.isEmpty()) {
                    Path maybeBlazeFile = Paths.get(arg);
                    if (Files.exists(maybeBlazeFile) && Files.isRegularFile(maybeBlazeFile)) {
                        blazeFile = maybeBlazeFile;
                        continue;
                    }
                }
                // otherwise this is a task to run
                tasks.add(arg);
            }
        }
        
        // trigger logger to be bound!
        Logger log = LoggerFactory.getLogger(Bootstrap.class);
        
        Timer timer = new Timer();
        try {
            // build blaze
            Blaze blaze = this.buildBlaze();

            if (listTasks) {
                logTasks(log, blaze);
                System.exit(0);
            } else {
                try {
                    log.debug("tasks to execute: {}", tasks);
                    blaze.executeAll(tasks);
                } catch (NoSuchTaskException e) {
                    // do not log stack trace
                    log.error(e.getMessage());
                    logTasks(log, blaze);
                    System.exit(1);
                }
            }
        } catch (MessageOnlyException | DependencyResolveException e) {
            // do not log stack trace
            log.error(e.getMessage());
            System.exit(1);
        } catch (Throwable t) {
            // unwrap a wrapped exception (much cleaner)
            if (t instanceof WrappedBlazeException) {
                WrappedBlazeException wbe = (WrappedBlazeException)t;
                t = wbe.getCause();
            }
            // hmmm... definitely something unexpected so log stack trace
            log.error(t.getMessage(), t);
            System.exit(1);
        }
        
        // only log time if no exception
        log.info("Blazed in {} ms", timer.stop().millis());
    }
    
    // all overrideable by subclasses
    public String getName() {
        return "blaze";
    }
    
    public String nextArg(Deque<String> args, String arg, String valueDescription) {
        if (args.isEmpty()) {
            System.err.println("[ERROR] " + arg + " argument requires next arg to be a " + valueDescription);
            System.exit(1);
        }
        return args.remove();
    }
    
    public void printVersion() {
        System.out.println(getName() + ": v" + Version.getLongVersion());
        System.out.println(" by Fizzed, Inc. (http://fizzed.com)");
        System.out.println(" at https://github.com/fizzed/blaze");
    }
    
    public void printHelp() {
        System.out.println(getName() + ": [options] <task> [<task> ...]");
        System.out.println("-f|--file <file>   Use this " + getName() + " file instead of default");
        System.out.println("-d|--dir <dir>     Search this dir for " + getName() + " file instead of default (-f supercedes)");
        System.out.println("-l|--list          Display list of available tasks");
        System.out.println("-q                 Only log " + getName() + " warnings to stdout (script logging is still info level)");
        System.out.println("-qq                Only log warnings to stdout (including script logging)");
        System.out.println("-x[x...]           Increases verbosity of logging to stdout");
        System.out.println("-v|--version       Display version and then exit");
        System.out.println("-Dname=value       Sets a System property as name=value");
        System.out.println("-i|--install <dir> Install blaze or blaze.bat to directory");
    }
    
    public Blaze buildBlaze() {
        return new Blaze.Builder()
            .file(blazeFile)
            .directory(blazeDir)
            .build();
    }
    
    public void systemProperty(String name, String value) {
        System.setProperty(name, value);
    }
    
    public boolean interceptArgument(String arg, Deque<String> args) {
        // really meant for subclasses to be able to handl the argument
        return false;
    }
    
    public void configureLogging(String arg) {
        String level = "info";
        String scriptLevel = "info";

        if (arg.equals("-q")) {
            level = "warn";
        } else if (arg.equals("-qq")) {
            level = scriptLevel = "warn";
        } else if (arg.equals("-x")) {
            level = scriptLevel = "debug";
        } else if (arg.equals("-xx")) {
            level = scriptLevel = "trace";
        } else if (arg.equals("-xxx")) {
            level = scriptLevel = "trace";
            // but also set another system property which really turns on even MORE debugging
            System.setProperty("blaze.superdebug", "true");
        }

        JdkLoggerHelper.setRootLevel(level);
        JdkLoggerHelper.setLevel("script", scriptLevel);

        System.setProperty("org.slf4j.simpleLogger.defaultLogLevel", level);
        System.setProperty("org.slf4j.simpleLogger.log.script", scriptLevel);

        // if using logback
        //Logger root = (Logger) LoggerFactory.getLogger(org.slf4j.Logger.ROOT_LOGGER_NAME);
        /**
        Logger root = (Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
        if (arg.length() == 2) {
        root.setLevel(Level.DEBUG);
        } else {
        root.setLevel(Level.INFO);
        }
         */
    }
    
    public void logTasks(Logger log, Blaze blaze) {
        System.out.println("tasks =>");
        List<String> ts = blaze.script().tasks();
        ts.stream().forEach((t) -> {
            System.out.println("  " + t);
        });
    }
}

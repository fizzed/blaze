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
import com.fizzed.blaze.util.DependencyResolveException;
import com.fizzed.blaze.util.Timer;
import java.io.File;
import java.io.IOException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Bootstrap {

    static public void main(String[] args) throws IOException {
        JdkLoggerHelper.configure();
        
        Thread.currentThread().setName("blaze");
        
        // process command line args
        ArrayDeque<String> argString = new ArrayDeque(Arrays.asList(args));

        File blazeFile = null;
        File blazeDir = null;
        List<String> tasks = new ArrayList<>();

        boolean listTasks = false;

        while (!argString.isEmpty()) {
            String arg = argString.remove();
            if (arg.equals("-v") || arg.equals("--version")) {
                System.out.println("blaze: v" + Version.getLongVersion());
                System.out.println(" by Fizzed, Inc. (http://fizzed.com)");
                System.out.println(" at https://github.com/fizzed/blaze");
                System.exit(0);
            } else if (arg.equals("-q") || arg.equals("-qq") || arg.equals("-x") || arg.equals("-xx") || arg.equals("-xxx")) {
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
            } else if (arg.equals("-h") || arg.equals("--help")) {
                System.out.println("blaze: [options] <task> [<task> ...]");
                System.out.println("-f|--file <file>  Use this blaze file instead of default");
                System.out.println("-d|--dir <dir>    Search this dir for blaze file instead of default (-f supercedes)");
                System.out.println("-l|--list         Display list of available tasks");
                System.out.println("-q                Only log blaze warnings to stdout (script logging is still info level)");
                System.out.println("-qq               Only log warnings to stdout (including script logging)");
                System.out.println("-x[x...]          Increases verbosity of logging to stdout");
                System.out.println("-v|--version      Display version and then exit");
                System.exit(0);
            } else if (arg.equals("-f") || arg.equals("--file")) {
                if (argString.isEmpty()) {
                    System.err.println("[ERROR] -f|--file parameter requires next arg to be file");
                    System.exit(1);
                }
                blazeFile = new File(argString.remove());
            } else if (arg.equals("-d") || arg.equals("--dir")) {
                if (argString.isEmpty()) {
                    System.err.println("[ERROR] -d|--dir parameter requires next arg to be directory");
                    System.exit(1);
                }
                blazeDir = new File(argString.remove());
            } else if (arg.equals("-l") || arg.equals("--list")) {
                listTasks = true;
            } else if (arg.startsWith("-")) {
                System.err.println("[ERROR] Unsupported command line switch [" + arg + "]; blaze -h for more info");
                System.exit(1);
            } else {
                // this arg must be a task to run
                tasks.add(arg);
            }
        }
        
        // trigger logger to be bound!
        Logger log = LoggerFactory.getLogger(Bootstrap.class);
        
        // create but do not build yet
        Blaze.Builder blazeBuilder = Blaze.builder()
            .file(blazeFile)
            .directory(blazeDir);
        
        Timer timer = new Timer();
        try {
            // build blaze
            Blaze blaze = blazeBuilder.build();

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
        } catch (Exception e) {
            // hmmm... definitely something unexpected so log stack trace
            log.error(e.getMessage(), e);
            System.exit(1);
        }
        
        // only log time if no exception
        log.info("Blazed in {} ms", timer.stop().millis());
    }
    
    static private void logTasks(Logger log, Blaze blaze) {
        System.out.println(blaze.context().scriptFile() + " tasks =>");
        List<String> ts = blaze.script().tasks();
        ts.stream().forEach((t) -> {
            System.out.println("  " + t);
        });
    }
}

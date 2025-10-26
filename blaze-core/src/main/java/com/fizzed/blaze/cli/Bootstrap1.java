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
import com.fizzed.blaze.core.*;
import com.fizzed.blaze.internal.InstallHelper;
import com.fizzed.blaze.logging.LogLevel;
import com.fizzed.blaze.logging.LoggerConfig;
import com.fizzed.blaze.util.Timer;
import java.io.IOException;
import java.nio.file.Path;
import java.util.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Bootstrap1 {

    @SuppressWarnings("ThrowableResultIgnored")
    public void run(String[] args) throws IOException {
        Thread.currentThread().setName(getName());

        // is command line completion requested?

        if (BashCompleter.isRequested(args) ) {
            // this will take over handling and also call System.exit()
            new BashCompleter().run(args);
        }

        // regular handling of blaze command

        final BlazeArguments arguments;
        try {
            arguments = BlazeArguments.parse(args);
        } catch (IllegalArgumentException | MessageOnlyException e) {
            System.err.println("[ERROR] " + e.getMessage());
            System.exit(1);
            return;
        }

        if (arguments.isShowVersion()) {
            this.printVersion();
            System.exit(0);
            return;
        }

        if (arguments.isShowHelp()) {
            this.printHelp();
            System.exit(0);
            return;
        }

        if (arguments.getInstallDir() != null) {
            try {
                List<Path> installedFiles = InstallHelper.installBlazeBinaries(arguments.getInstallDir());
                for (Path installedFile : installedFiles) {
                    System.out.println("Installed " + installedFile);
                }
                System.exit(0);
            } catch (IllegalArgumentException | MessageOnlyException e) {
                System.err.println("[ERROR] " + e.getMessage());
                System.exit(1);
            }
            return;
        }

        // configure logging (either default or provided level)
        this.configureLogging(arguments.getLoggingLevel());
        
        // trigger logger to be bound!
        Logger log = LoggerFactory.getLogger(Bootstrap1.class);

        // any system properties to set?
        if (arguments.getSystemProperties() != null) {
            for (Map.Entry<String,String> entry : arguments.getSystemProperties().entrySet()) {
                log.debug("Setting system property: {}={}", entry.getKey(), entry.getValue());
                this.systemProperty(entry.getKey(), entry.getValue());
            }
        }
        
        Timer timer = new Timer();
        try {
            if (arguments.isGenerateMavenProject()) {
                // we do NOT need to compile the script to generate the maven project and this helps if a user has
                // some syntax issues with their script that would prevent building the POM
                Blaze blaze = this.buildBlaze(arguments, false);
                new MavenProjectGenerator().setBlaze(blaze).generate();
                System.exit(0);
                return;
            }

            // build & compile blaze script
            Blaze blaze = this.buildBlaze(arguments, true);

            if (arguments.isListTasks()) {
                this.printTasks(blaze);
                System.exit(0);
                return;
            }

            try {
                log.debug("Tasks to execute: {}", arguments.getTasks());
                blaze.executeAll(arguments.getTasks());
            } catch (NoSuchTaskException e) {
                // if no task was supplied at all, the default task was missing and its stupid to log this message
                if (!arguments.getTasks().isEmpty()) {
                    // do not log stack trace
                    log.error(e.getMessage());
                } else {
                    log.error("You must specify one or more tasks to execute.");
                }
                this.printTasks(blaze);
                System.exit(1);
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
        log.info("Blazed in {}", timer.stop());
    }
    
    // all overrideable by subclasses
    public String getName() {
        return "blaze";
    }

    //
    // Utility Methods
    //
    
    public void printVersion() {
        System.out.println(getName() + " v" + Version.getLongVersion());
        System.out.println("  by Fizzed, Inc. (http://fizzed.com)");
        System.out.println("  at https://github.com/fizzed/blaze");
    }
    
    public void printHelp() {
        // help will consist of printing the version
        this.printVersion();
        System.out.println();
        System.out.println("Usage =>");
        System.out.println("  " + getName() + " [options] <task> [arg ...] [<task> ...] [arg ...]");
        System.out.println();
        System.out.println("Options =>");
        System.out.println("  -f|--file <file>           Use this " + getName() + " file instead of default");
        System.out.println("  -d|--dir <dir>             Search this dir for " + getName() + " file instead of default (-f supersedes)");
        System.out.println("  -l|--list                  Display list of available tasks");
        System.out.println("  -q                         Only log " + getName() + " warnings to stdout (script logging is still info level)");
        System.out.println("  -qq                        Only log warnings to stdout (including script logging)");
        System.out.println("  -x[x...]                   Increases verbosity of logging to stdout");
        System.out.println("  -v|--version               Display version and then exit");
        System.out.println("  -Dname=value               Sets a System property as name=value");
        System.out.println("  --generate-maven-project   Generate a maven project pom.xml in the same dir as your blaze script for IDE support");
        System.out.println("  -i|--install <dir>         Install blaze or blaze.bat to directory");
        System.out.println();
        System.out.println("Tasks =>");
        System.out.println("  Run with --list to display a list of available tasks");
        System.out.println();
    }

    public void printTasks(Blaze blaze) {
        final String taskListText = new TaskListRenderer().render(blaze);
        System.out.println("Run and execute one or more tasks and their arguments.");
        System.out.println();;
        System.out.println("Usage =>");
        System.out.println("  " + getName() + " <task> [arg ...] [<task> ...] [arg ...]");
        System.out.println();
        System.out.println(taskListText);       // taskListText ends in newline, so this also adds another newline
    }

    public Blaze buildBlaze(BlazeArguments arguments, boolean buildScript) {
        return new Blaze.Builder()
            .file(arguments.getBlazeFile())
            .directory(arguments.getBlazeDir())
            .configProperties(arguments.getConfigProperties())
            .build(buildScript);
    }
    
    public void systemProperty(String name, String value) {
        System.setProperty(name, value);
    }
    
    public void configureLogging(int loggingLevel) {
        // default logging level of 0
        String level = "info";
        String scriptLevel = "info";

        switch (loggingLevel) {
            case -3:    // special case, disable everything!
                level = scriptLevel = "off";
                break;
            case -2:    // -qq
                level = scriptLevel = "warn";
                break;
            case -1:    // -q
                level = "warn";
                break;
            case 1:     // -x
                level = scriptLevel = "debug";
                break;
            case 2:     // -xx
                level = scriptLevel = "trace";
                break;
            case 3:     // -xxx
                level = scriptLevel = "trace";
                // but also set another system property which really turns on even MORE debugging
                System.setProperty("blaze.superdebug", "true");
                break;
        }

        LoggerConfig.setDefaultLogLevel(LogLevel.valueOf(level.toUpperCase()));
        LoggerConfig.setLogLevel("script", LogLevel.valueOf(scriptLevel.toUpperCase()));
        LoggerConfig.setLogLevel("org.zeroturnaround", LogLevel.OFF);



        JdkLoggerHelper.setRootLevel(level);
        JdkLoggerHelper.setLevel("script", scriptLevel);

        System.setProperty("org.slf4j.simpleLogger.defaultLogLevel", level);
        System.setProperty("org.slf4j.simpleLogger.log.script", scriptLevel);
        // disable logging for zeroturnaround
        System.setProperty("org.slf4j.simpleLogger.log.org.zeroturnaround", "off");
    }



    /*public void logTasks(Logger log, Blaze blaze) {
        System.out.println("Run and execute one or more tasks.");
        System.out.println();
        System.out.println("Usage =>");
        System.out.println("  blaze.jar <task> [<task> ...] [args]");

        List<BlazeTaskGroup> gs = blaze.getTaskGroups();
        List<BlazeTask> ts = blaze.getTasks();
        
        // calculate max width of task name
        int width = 0;
        for (BlazeTask t : ts) {
            width =  Math.max(t.getName().length(), width);
        }

        // collect all the group ids that are present on tasks
        final Set<String> groupIds = ts.stream()
            .map(BlazeTask::getGroup)
            .collect(Collectors.toSet());

        // group mode if more than one group id is present
        if (groupIds.size() > 1) {
            // is the default group used?
            if (groupIds.contains(null)) {{
                // add the default group to the list
                gs.add(new BlazeTaskGroup(null, "General", TaskGroup.GENERAL_GROUP_ORDER));
            }}

            // remove task groups that are not used
            gs.removeIf(g -> !groupIds.contains(g.getId()));

            // add task groups that are used but not present in the list
            for (String id : groupIds) {
                if (gs.stream().noneMatch(g -> Objects.equals(g.getId(), id))) {
                    gs.add(new BlazeTaskGroup(id, null, TaskGroup.DEFAULT_ORDER));
                }
            }

            // sort the groups by order
            Collections.sort(gs);

            for (BlazeTaskGroup g : gs) {
                System.out.println();
                System.out.println(ofNullable(g.getName()).orElse(g.getId()) + " tasks =>");

                // build a list of tasks for this group
                final List<BlazeTask> tasksInGroup = ts.stream()
                    .filter(t -> Objects.equals(g.getId(), t.getGroup()))
                    .collect(Collectors.toList());

                Collections.sort(tasksInGroup);

                for (BlazeTask t : tasksInGroup) {
                    if (t.getDescription() != null) {
                        System.out.println("  " + padRight(t.getName(), width + 10) + t.getDescription());
                    } else {
                        System.out.println("  " + t.getName());
                    }
                }
            }
        } else {
            // non-group mode
            System.out.println();
            System.out.println("Tasks =>");

            Collections.sort(ts);

            // output task name & description w/ padding
            for (BlazeTask t : ts) {
                if (t.getDescription() != null) {
                    System.out.println("  " + padRight(t.getName(), width+10) + t.getDescription());
                } else {
                    System.out.println("  " + t.getName());
                }
            }
        }

        System.out.println();
    }*/

}
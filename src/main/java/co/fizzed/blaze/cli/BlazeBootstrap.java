/*
 * Copyright 2014 Fizzed Inc.
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
package co.fizzed.blaze.cli;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import co.fizzed.blaze.Version;
import co.fizzed.blaze.core.Blaze;
import co.fizzed.blaze.core.UsageException;
import co.fizzed.blaze.task.Task;
import java.io.File;
import java.io.IOException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;
import javax.script.ScriptException;
import org.slf4j.LoggerFactory;

/**
 *
 * @author joelauer
 */
public class BlazeBootstrap {
    
    static public void main(String[] args) throws Exception, IOException, ScriptException, Throwable {
        // process command line args
        ArrayDeque<String> argString = new ArrayDeque(Arrays.asList(args));
        
        Blaze blaze = new Blaze();
        
        boolean listTasks = false;
        
        while (!argString.isEmpty()) {
            String arg = argString.remove();
            if (arg.equals("-V") || arg.equals("--version")) {
                System.out.println("blaze: v" + Version.getLongVersion());
                System.out.println(" by Fizzed (http://fizzed.co)");
                System.exit(0);
            } else if (arg.equals("-v") || arg.equals("-vv")) {
                Logger root = (Logger)LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
                if (arg.length() == 2) {
                    root.setLevel(Level.DEBUG);
                } else {
                    root.setLevel(Level.ALL);
                }
            } else if (arg.equals("-h") || arg.equals("--help")) {
                System.out.println("blaze: [options] <task> [<task> ...]");
                System.out.println("-f|--file <projectFile>     Use this project file instead (default is blaze.js)");
                System.out.println("-l|--list                   Display list of runnable tasks in project");
                System.out.println("-V|--version                Display version");
                System.exit(0);
            } else if (arg.equals("-f") || arg.equals("--file")) {
                if (argString.isEmpty()) {
                    System.err.println("-f|--file parameter requires next arg to be file");
                    System.exit(1);
                }
                File f = new File(argString.remove());
                if (!f.exists()) {
                    System.err.println("Project file [" + f + "] does not exist");
                    System.exit(1);
                }
                blaze.setProjectFile(f);
            } else if (arg.equals("-l") || arg.equals("--list")) {
                listTasks = true;
            } else if (arg.startsWith("-")) {
                System.err.println("Unsupported command line switch [" + arg + "]; blaze -h for more info");
                System.exit(1);
            } else {
                // otherwise assume this arg & rest of line are all tasks to be run
                argString.push(arg);
                break;
            }
        }
        
        try {
            if (listTasks) {
                blaze.load();
                System.out.println("Runnable tasks:");
                Map<String,Task> tasks = blaze.getContext().getTasks();
                tasks.keySet().stream().forEach((s) -> { System.out.println(" " + s); });
                System.exit(0);
            } else {
                // loads and runs project with argString as all tasks
                blaze.run(new ArrayList<>(argString));
            }
        } catch (UsageException e) {
            System.err.println(e.getMessage());
            System.exit(1);
        } catch (Exception e) {
            System.err.println(e.getMessage());
            e.printStackTrace(System.err);
            System.exit(1);
        }
    }
}

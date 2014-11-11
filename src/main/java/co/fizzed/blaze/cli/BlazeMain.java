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

import co.fizzed.blaze.core.Context;
import co.fizzed.blaze.core.Settings;
import co.fizzed.blaze.task.Task;
import co.fizzed.blaze.task.TaskFactory;
import java.io.File;
import java.io.FileReader;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import javax.script.Bindings;
import javax.script.Invocable;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;

/**
 *
 * @author joelauer
 */
public class BlazeMain {
    
    static public String DEFAULT_FILE = "blaze.js";
    
    static public void main(String[] args) throws Exception {
        File tasksFile = new File(DEFAULT_FILE);
        if (!tasksFile.exists()) {
            System.err.println("Unable to find " + DEFAULT_FILE + " in current dir");
            System.exit(1);
        }
        
        ScriptEngineManager manager = new ScriptEngineManager();
        ScriptEngine engine = manager.getEngineByName("nashorn");
        
        // export some objects as global to script
        Context context = new Context(engine);
        Settings.populateDefaults(context, context.getSettings());

        // expose funcations as global variables to script
        Bindings bindings = engine.getBindings(ScriptContext.GLOBAL_SCOPE);
        bindings.put("$A", context.getActions());
        bindings.put("$U", context.getUtils());
        
        Bindings engineBindings = engine.getBindings(ScriptContext.ENGINE_SCOPE);
        engineBindings.put("$S", context.getSettings());
        //engineBindings.put("$T", context.getTasks());
        Map<String,Task> tasks = new LinkedHashMap<>();
        engineBindings.put("$T", tasks);
        engineBindings.put("Task", new TaskFactory(context));
        
        engine.eval(new FileReader(tasksFile));
 
        // does script define a default task?
        String taskToRun = null;
        if (args.length > 0) {
            taskToRun = args[0];
        }
        
        if (taskToRun == null) {
            System.err.println("No task specified (either as default in " + tasksFile.getName() + " or via command line)");
            System.exit(1);
        }
        
        try {
            Task task = tasks.get(taskToRun);
            if (task == null) {
                System.err.println("Task [" + taskToRun + "] does not exist");
                System.exit(1);
            }
            task.call();
        } catch (RuntimeException e) {
            if (e.getCause() != null) {
                e.getCause().printStackTrace(System.err);
            } else {
                e.printStackTrace(System.err);
            }
        } catch (Exception e) {
            e.printStackTrace(System.err);
        }
        
        // cleanup executor service (otherwise app won't exit)
        System.out.println("Shutting down executor thread pool");
        context.getActions().executors.shutdown();
    }
}

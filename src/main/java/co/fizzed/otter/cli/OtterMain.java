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
package co.fizzed.otter.cli;

import co.fizzed.otter.core.Context;
import co.fizzed.otter.core.Settings;
import java.io.File;
import java.io.FileReader;
import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import jdk.nashorn.internal.runtime.ScriptFunction;

/**
 *
 * @author joelauer
 */
public class OtterMain {
    
    static public void main(String[] args) throws Exception {
        File tasksFile = new File("otter.js");
        if (!tasksFile.exists()) {
            System.err.println("Unable to find tasks.js in current dir");
            System.exit(1);
        }
        
        ScriptEngine engine = new ScriptEngineManager().getEngineByName("nashorn");
        
        // export some objects as global to script
        Context context = new Context(engine);
        Settings.populateDefaults(context, context.getSettings());

        // expose Functions object as a global variable to the engine
        engine.put("$S", context.getSettings());
        engine.put("$T", context.getTasks());
        engine.put("$A", context.getActions());
        engine.put("$U", context.getUtils());
        
        Invocable invocable = (Invocable) engine; 
        engine.eval(new FileReader(tasksFile));
        
        /**
        // list all tasks
        System.out.println("Getting list of all tasks...");
        for (String s : context.getTasks().keySet()) {
            System.out.println(s);
        }
        */
        
        /**
        ScriptObjectMirror sobj = 
        System.out.println(sobj);
        for (Map.Entry<String,Object> entry : sobj.entrySet()) {
            System.out.println("Field: " + entry.getKey() + " -> " + entry.getValue());
        }
        
        // debug all functions on an object...
        /**
        for (Field f : context.getFunctions().getClass().getFields()) {
            System.out.println("Field: " + f.getName());
        }
        */

        // does script define a default task?
        String taskToRun = null;
        if (args.length > 0) {
            taskToRun = args[0];
        }
        
        if (taskToRun == null) {
            System.err.println("No task specified (either as default in tasks.js or via command line)");
            System.exit(1);
        }

        //Arrays.copyOfRange(args, 1, args.length);
        
        try {
            ScriptFunction o = (ScriptFunction)context.getTasks().getMember(taskToRun);
            if (o == null) {
                System.err.println("Task [" + taskToRun + "] does not exist");
                System.exit(1);
            }
            engine.eval("$T."+taskToRun+"()");
        } catch (RuntimeException e) {
            if (e.getCause() != null) {
                e.getCause().printStackTrace(System.err);
            } else {
                e.printStackTrace(System.err);
            }
        } catch (Exception e) {
            e.printStackTrace(System.err);
        }
    }
}

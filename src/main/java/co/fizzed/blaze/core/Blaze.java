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

package co.fizzed.blaze.core;

import co.fizzed.blaze.task.Task;
import co.fizzed.blaze.task.TaskFactory;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;
import javax.script.Bindings;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

/**
 *
 * @author joelauer
 */
public class Blaze {
    private static final Logger logger = LoggerFactory.getLogger(Blaze.class);
    
    static public String DEFAULT_FILE = "blaze.js";
    
    private File projectFile;
    
    // once loaded
    private ScriptEngineManager scriptManager;
    private ScriptEngine scriptEngine;
    private Context context;
    
    public Blaze() {
        // relative to current dir by default
        this.projectFile = new File(DEFAULT_FILE);
    }

    public File getProjectFile() {
        return projectFile;
    }

    public void setProjectFile(File projectFile) {
        this.projectFile = projectFile;
    }

    public ScriptEngineManager getScriptManager() {
        return scriptManager;
    }

    public ScriptEngine getScriptEngine() {
        return scriptEngine;
    }

    public Context getContext() {
        return context;
    }

    public void load() throws IOException, ScriptException {
        if (this.scriptEngine != null) {
            throw new IOException("Blaze project file [" + projectFile + "] already loaded");
        }
        
        if (projectFile == null || !projectFile.exists() || !projectFile.isFile()) {
            throw new IOException("Blaze project file [" + projectFile + "] does not exist");
        }
        
        this.scriptManager = new ScriptEngineManager();
        this.scriptEngine = scriptManager.getEngineByName("nashorn");
        
        // export some objects as global to script
        context = new Context(scriptEngine);
        Settings.populateDefaults(context, context.getSettings());

        // expose functions as global variables to script
        Bindings bindings = scriptEngine.getBindings(ScriptContext.GLOBAL_SCOPE);
        bindings.put("$A", context.getActions());
        bindings.put("$U", context.getUtils());
        // passthru logging to project...
        bindings.put("log", logger);
        
        Bindings engineBindings = scriptEngine.getBindings(ScriptContext.ENGINE_SCOPE);
        engineBindings.put("$S", context.getSettings());
        engineBindings.put("$T", context.getTasks());
        engineBindings.put("Task", new TaskFactory(context));
        
        scriptEngine.eval(new FileReader(projectFile));
        
        // on success set the base dir of the context
        context.setBaseDir(projectFile.getParentFile());
    }
    
    public void run(List<String> tasksToRun) throws UsageException, IOException, ScriptException, Exception, Throwable {
        MDC.put("task", "blaze");
        logger.info("Running project " + this.projectFile);
        
        if (tasksToRun == null || tasksToRun.isEmpty()) {
            throw new UsageException("No tasks were requested to be run");
        }
        
        // load if needed
        if (this.scriptEngine == null) {
            load();
        }
        
        // verify all the tasks requested exist
        for (String taskToRun : tasksToRun) {
            if (!context.getTasks().containsKey(taskToRun)) {
                throw new UsageException("Task [" + taskToRun + "] is not defined in project file [" + this.projectFile + "]");
            }
        }
        
        try {
            for (String taskToRun : tasksToRun) {
                Task task = context.getTasks().get(taskToRun);
                if (task == null) {
                    throw new UsageException("Task [" + taskToRun + "] is not defined [" + this.projectFile + "]");
                }
                task.call();
            }
        } catch (RuntimeException e) {
            // unwrap inner exception if possible
            if (e.getCause() != null) {
                throw e.getCause();
            } else {
                throw e;
            }
        } catch (Exception e) {
            throw e;
        }
        
        // cleanup executor service (otherwise app won't exit)
        logger.debug("Shutting down executor thread pool...");
        context.getActions().executors.shutdown();
    }
    
}

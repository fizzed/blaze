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
import co.fizzed.blaze.task.TaskMap;
import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import javax.script.ScriptEngine;

/**
 *
 * @author joelauer
 */
public class Context {
    
    private final ScriptEngine engine;
    private final Settings settings;
    private final Map<String,Task> tasks;
    private final Actions actions;
    private final Utils utils;
    
    // runtime stuff
    private File baseDir;
    
    public Context(ScriptEngine engine) {
        this.engine = engine;
        this.tasks = new TaskMap();
        this.settings = new Settings(this);
        this.actions = new Actions(this);
        this.utils = new Utils(this);
        // defaults to current directory
        this.baseDir = new File(".");
    }

    public ScriptEngine getEngine() {
        return engine;
    }

    public Map<String,Task> getTasks() {
        return tasks;
    }

    public Settings getSettings() {
        return settings;
    }

    public Actions getActions() {
        return actions;
    }

    public Utils getUtils() {
        return utils;
    }

    public File getBaseDir() {
        return baseDir;
    }

    public void setBaseDir(File baseDir) {
        this.baseDir = baseDir;
    }
    
    public File resolveWithBaseDir(Path path) {
        return this.baseDir.toPath().resolve(path).toFile();
    }
    
    public File resolveWithBaseDir(File file) {
        if (file.isAbsolute()) {
            return file;
        } else {
            return new File(baseDir, file.getPath());
        }
    }
    
}

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
package com.fizzed.blaze.core;

import com.fizzed.blaze.internal.ContextImpl;
import com.fizzed.blaze.Config;
import com.fizzed.blaze.Context;
import com.fizzed.blaze.internal.DependencyHelper;
import com.fizzed.blaze.internal.ClassLoaderHelper;
import static com.fizzed.blaze.internal.ClassLoaderHelper.currentThreadContextClassLoader;
import com.fizzed.blaze.internal.ConfigHelper;
import com.fizzed.blaze.internal.DefaultScriptFileLocator;
import com.fizzed.blaze.internal.EngineHelper;
import com.fizzed.blaze.internal.FileHelper;
import com.fizzed.blaze.jdk.TargetObjectScript;
import com.fizzed.blaze.util.Timer;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The main Blaze builder and runtime environment.
 * 
 * @author joelauer
 */
public class Blaze {
    static private final Logger log = LoggerFactory.getLogger(Blaze.class);
    
    static public final List<Path> SEARCH_RELATIVE_DIRECTORIES = Arrays.asList(
        Paths.get("blaze"),
        Paths.get(".blaze")
    );
    
    static public class Builder {
        
        private Path directory;
        private Path file;
        private Object scriptObject;
        private List<Dependency> collectedDependencies;
        private ScriptFileLocator scriptFileLocator;
        private DependencyResolver dependencyResolver;
        
        
        public Builder() {
            this.scriptFileLocator = new DefaultScriptFileLocator();
            this.dependencyResolver = DependencyResolvers.load();
        }
        
        public Builder directory(Path directory) {
            this.directory = directory;
            return this;
        }
        
        public Builder directory(String directory) {
            directory((directory != null ? Paths.get(directory) : null));
            return this;
        }
        
        public Builder directory(File directory) {
            directory((directory != null ? directory.toPath() : null));
            return this;
        }
        
        public Path getDirectory() {
            return this.directory;
        }
        
        public Builder file(Path file) {
            this.file = file;
            return this;
        }
        
        public Builder file(String file) {
            file((file != null ? Paths.get(file) : null));
            return this;
        }
        
        public Builder file(File file) {
            file((file != null ? file.toPath() : null));
            return this;
        }
        
        public Path getFile() {
            return this.file;
        }

        public Builder scriptObject(Object scriptObject) {
            this.scriptObject = scriptObject;
            return this;
        }
        
        public Object getScriptObject() {
            return scriptObject;
        }
        
        public Builder scriptFileLocator(ScriptFileLocator scriptFileLocator) {
            this.scriptFileLocator = scriptFileLocator;
            return this;
        }

        public ScriptFileLocator getScriptFileLocator() {
            return scriptFileLocator;
        }
        
        public Builder dependencyResolver(DependencyResolver dependencyResolver) {
            this.dependencyResolver = dependencyResolver;
            return this;
        }
        
        public DependencyResolver getDependencyResolver() {
            return this.dependencyResolver;
        }

        public List<Dependency> getCollectedDependencies() {
            return collectedDependencies;
        }

        public Builder collectedDependencies(List<Dependency> collectedDependencies) {
            this.collectedDependencies = collectedDependencies;
            return this;
        }
        
        // set during build process (maybe refactor how this is done eventually...)
        private Path detectedBaseDir;
        private Path detectedScriptFile;
        private Config config;
        private String scriptExtension;
        private Context context;
        private List<Dependency> dependencies;
        private List<File> dependencyJarFiles;
        private Engine engine;
        private Script script;

        public List<Dependency> getDependencies() {
            return dependencies;
        }

        public List<File> getDependencyJarFiles() {
            return dependencyJarFiles;
        }
        
        public void locate() {
            // no need to resolve a script if a target object is already provided
            if (this.scriptObject != null) {
                return;
            }
            
            if (this.file != null) {
                detectedScriptFile = this.file;
            } else {
                detectedScriptFile = this.scriptFileLocator.locate(this.directory);
            } 
             
            // at this point we should have a file - verify it exists and works
            if (Files.notExists(detectedScriptFile)) {
                throw new MessageOnlyException("Blaze file " + detectedScriptFile + " not found. Perhaps this is not a Blaze project?");
            }
            
            if (!Files.isRegularFile(detectedScriptFile)) {
                throw new MessageOnlyException("Blaze file " + detectedScriptFile + " is not a file. Perhaps this is not a Blaze project?");
            }
            
            // cleanup look of detected script so something like "./../dir/blaze.java" -> "blaze.java"
            detectedScriptFile = FileHelper.relativizeToJavaWorkingDir(detectedScriptFile);
            
            // base directory will always be the parent of the script
            detectedBaseDir = detectedScriptFile.getParent();
            
            if (detectedBaseDir != null && detectedBaseDir.equals(Paths.get("."))) {
                detectedBaseDir = null;
            }

            log.trace("Using blaze dir {} and file {}", detectedBaseDir, detectedScriptFile);
        }
        
        public void configure() {
            if (detectedScriptFile == null) {
                locate();
            }
            
            Path configFile = null;
            
            // a script may not actually have been detected
            if (this.detectedScriptFile != null) {
                configFile = ConfigHelper.path(detectedBaseDir, detectedScriptFile);
                
                scriptExtension = FileHelper.fileExtension(detectedScriptFile);
            }
            
            config = ConfigHelper.create(configFile);

            context = new ContextImpl(
                (detectedBaseDir != null ? detectedBaseDir : null),
                null,    
                detectedScriptFile,
                config);
            
            ContextHolder.set(context);
        }
        
        public void resolveDependencies() {
            if (context == null) {
                configure();
            }
            
            //
            // any dependencies that need to be resolved (in case engine itself is a dependency)
            //
            log.info("Resolving dependencies...");
            Timer dependencyTimer = new Timer();
            
            // save which dependencies are already resolved
            List<Dependency> resolvedDependencies
                    = (collectedDependencies != null ? collectedDependencies : DependencyHelper.alreadyBundled());
            
            // any well known engines to include?
            List<Dependency> wellKnownEngineDependencies = DependencyHelper.wellKnownEngineDependencies(scriptExtension);
            
            // did script declare any dependencies we need to include?
            List<Dependency> applicationDependencies = DependencyHelper.applicationDependencies(config);
            
            // build dependencies to resolve (need collected so correct versions are picked)
            dependencies = new ArrayList<>();
            
            DependencyHelper.collect(dependencies, resolvedDependencies);
            DependencyHelper.collect(dependencies, wellKnownEngineDependencies);
            DependencyHelper.collect(dependencies, applicationDependencies);
            
            // smart resolving...
            try {
                if (dependencies.isEmpty()) {
                    log.debug("No dependencies to resolve (skipping resolver)");
                } else if (dependencies.size() == resolvedDependencies.size()) {
                    log.debug("We already have the dependencies we need (skipping resolver)");
                } else {
                    try {
                        // resolve dependencies against collected dependencies
                        dependencyJarFiles = dependencyResolver.resolve(context, resolvedDependencies, dependencies);
                    } catch (DependencyResolveException e) {
                        throw e;
                    } catch (IOException | ParseException e) {
                        throw new BlazeException("Unable to cleanly resolve dependencies", e);
                    }
                }
            } finally {
                log.info("Resolved dependencies in {} ms", dependencyTimer.stop().millis());
            }
        }
        
        public void loadDependencies() {
            if (dependencies == null) {
                resolveDependencies();
            }
            
            if (dependencyJarFiles != null) {
                final ClassLoader classLoader = currentThreadContextClassLoader();
                dependencyJarFiles.stream().forEach((jarFile) -> {
                    if (ClassLoaderHelper.addClassPath(classLoader, jarFile)) {
                        log.debug("Added {} to classpath", jarFile.getName());
                        log.debug(" => {}", jarFile);
                    }
                });
            }
        }
        
        public void compileScript() {
            // if we are simply wrapping an object, no need to compile
            if (this.scriptObject != null) {
                script = new TargetObjectScript(this.scriptObject);
                return;
            }
            
            //
            // find and prepare by extension
            //
            log.info("Compiling script...");
            Timer engineTimer = new Timer();
            
            engine = EngineHelper.findByFileExtension(scriptExtension, dependencyJarFiles != null && !dependencyJarFiles.isEmpty());
            
            if (engine == null) {
                throw new BlazeException("Unable to find script engine for file extension " + scriptExtension + ". Maybe bad file extension or missing dependency?");
            }

            log.debug("Using script engine {}", engine.getClass().getCanonicalName());
            
            if (!engine.isInitialized()) {
                engine.init(context);
            }
            
            script = engine.compile(context);
            
            log.info("Compiled script in {} ms", engineTimer.stop().millis());
        }
        
        public Blaze build() {
            loadDependencies();     // also calls locate(), configure(), and resolveDependencies()
            
            compileScript();
            
            return new Blaze(context, dependencies, engine, script);
        }
    }
    
    final private Context context;
    final private List<Dependency> dependencies;
    final private Engine engine;
    final private Script script;
    
    private Blaze(Context context, List<Dependency> dependencies, Engine engine, Script script) {
        this.context = context;
        this.dependencies = dependencies;
        this.engine = engine;
        this.script = script;
    }

    public Context context() {
        return context;
    }

    public List<Dependency> dependencies() {
        return dependencies;
    }
    
    public Engine engine() {
        return engine;
    }

    public Script script() {
        return script;
    }
    
    public List<String> tasks() throws BlazeException {
        return this.script.tasks();
    }
    
    public void execute() throws Exception {
        execute(null);
    }
    
    public void execute(String task) throws Exception {
        if (task == null || task.equals("")) {
            task = context.config().value(Config.KEY_DEFAULT_TASK).getOr(Config.DEFAULT_TASK);
        }
        
        String scriptName = (context.scriptFile() != null ? context.scriptFile().toString() : "");
        
        log.info("Executing {}:{}...", scriptName, task);
        Timer executeTimer = new Timer();
        
        this.script.execute(task);
        
        log.info("Executed {}:{} in {} ms", scriptName, task, executeTimer.stop().millis());
    }
    
    public void executeAll(List<String> tasks) throws Exception {
        // default task?
        if (tasks == null || tasks.isEmpty()) {
            execute(null);
        } else {
            for (String task : tasks) {
                execute(task);
            }
        }
    }
}
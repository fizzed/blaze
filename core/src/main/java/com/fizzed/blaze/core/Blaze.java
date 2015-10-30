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

import com.fizzed.blaze.Config;
import com.fizzed.blaze.Context;
import com.fizzed.blaze.internal.DependencyHelper;
import com.fizzed.blaze.internal.ClassLoaderHelper;
import com.fizzed.blaze.internal.ConfigHelper;
import com.fizzed.blaze.internal.Dependency;
import com.fizzed.blaze.internal.DependencyResolveException;
import com.fizzed.blaze.internal.DependencyResolver;
import com.fizzed.blaze.internal.IvyDependencyResolver;
import com.fizzed.blaze.internal.EngineHelper;
import com.fizzed.blaze.internal.FileHelper;
import com.fizzed.blaze.internal.Timer;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author joelauer
 */
public class Blaze {
    static private final Logger log = LoggerFactory.getLogger(Blaze.class);
    
    static public class Builder {
        
        private File directory;
        private File file;
        private List<Dependency> collectedDependencies;
        private DependencyResolver dependencyResolver;
        
        private Builder() {
            this.dependencyResolver = new IvyDependencyResolver();
        }
        
        public Builder directory(String directory) {
            this.directory = new File(directory);
            return this;
        }
        
        public Builder directory(File directory) {
            this.directory = directory;
            return this;
        }
        
        public File getDirectory() {
            return this.directory;
        }
        
        public Builder file(String file) {
            this.file = new File(file);
            return this;
        }
        
        public Builder file(File file) {
            this.file = file;
            return this;
        }
        
        public File getFile() {
            return this.file;
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
        private File detectedBaseDir;
        private File detectedScriptFile;
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
            if (this.file != null) {
                detectedBaseDir = this.file.getParentFile();
                detectedScriptFile = this.file;
            } else {
                // otherwise fallback to trying to figure it out ourselves
                if (this.directory == null) {
                    this.directory = new File(".");
                }
                
                // search for file named "blaze.<ext>"
                File[] blazeFiles = this.directory.listFiles(
                    (File f) -> f.getName().startsWith("blaze.")
                        && !f.getName().endsWith(".conf") && !f.getName().endsWith(".jar"));
                
                if (blazeFiles.length == 0) {
                    throw new MessageOnlyException("Unable to find a blaze file (e.g. blaze.js). Perhaps this is not a Blaze project?");
                }
                
                if (blazeFiles.length > 1) {
                    throw new MessageOnlyException("More than one blaze file found. Either delete the extra files use -f parameter");
                }
                
                detectedBaseDir = this.directory;
                detectedScriptFile = blazeFiles[0];
            }
            
            // at this point we should have a file - verify it exists and works
            if (!detectedScriptFile.exists()) {
                throw new MessageOnlyException("Blaze file " + detectedScriptFile + " not found. Perhaps this is not a Blaze project?");
            }
            
            if (!detectedScriptFile.isFile()) {
                throw new MessageOnlyException("Blaze file " + detectedScriptFile + " is not a file. Perhaps this is not a Blaze project?");
            }
            
            
            if (detectedBaseDir != null && detectedBaseDir.getPath().equals(".")) {
                detectedBaseDir = null;
            }
            
            detectedScriptFile = FileHelper.relativizeToJavaWorkingDir(detectedScriptFile);
            
            log.trace("Using blaze dir {} and file {}", detectedBaseDir, detectedScriptFile);
        }
        
        public void configure() {
            if (detectedScriptFile == null) {
                locate();
            }
            
            File configFile = ConfigHelper.file(detectedBaseDir, detectedScriptFile);
            
            config = ConfigHelper.create(configFile);
            
            scriptExtension = FileHelper.fileExtension(detectedScriptFile);
            
            context = new ContextImpl((detectedBaseDir != null ? detectedBaseDir.toPath() : null), detectedScriptFile.toPath(), config);
            
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
            
            if (!dependencies.isEmpty()) {
                try {
                    // resolve dependencies against collected dependencies
                    dependencyJarFiles = dependencyResolver.resolve(context, resolvedDependencies, dependencies);
                } catch (DependencyResolveException e) {
                    throw e;
                } catch (IOException | ParseException e) {
                    throw new BlazeException("Unable to cleanly resolve dependencies", e);
                }
            }
            
            log.info("Resolved dependencies in {} ms", dependencyTimer.stop().millis());
        }
        
        public void loadDependencies() {
            if (dependencies == null) {
                resolveDependencies();
            }
            
            if (dependencyJarFiles != null) {
                dependencyJarFiles.stream().forEach((jarFile) -> {
                    int changed
                            = ClassLoaderHelper.addFileToClassPath(jarFile, Thread.currentThread().getContextClassLoader());
                    if (changed > 0) {
                        log.info("Adding {} to classpath", jarFile.getName());
                        log.debug(" => {}", jarFile);
                    }
                });
            }
        }
        
        public void compileScript() {
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
    
    static public Builder builder() {
        return new Builder();
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
    
    public void execute() throws BlazeException {
        execute(null);
    }
    
    public void execute(String task) throws BlazeException {
        if (task == null || task.equals("")) {
            task = context.config().find(Config.KEY_DEFAULT_TASK).or(Config.DEFAULT_TASK);
        }
        
        log.info("Executing {}:{}...", context.scriptFile(), task);
        Timer executeTimer = new Timer();
        
        this.script.execute(task);
        
        log.info("Executed {}:{} in {} ms", context.scriptFile(), task, executeTimer.stop().millis());
    }
    
    public void executeAll(List<String> tasks) throws BlazeException {
        // default task?
        if (tasks == null || tasks.isEmpty()) {
            execute(null);
        } else {
            tasks.stream().forEach((task) -> {
                execute(task);
            });
        }
    }
    
}

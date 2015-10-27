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
import com.fizzed.blaze.util.DependencyHelper;
import com.fizzed.blaze.util.ClassLoaderHelper;
import com.fizzed.blaze.util.ConfigHelper;
import com.fizzed.blaze.util.Dependency;
import com.fizzed.blaze.util.DependencyResolveException;
import com.fizzed.blaze.util.DependencyResolver;
import com.fizzed.blaze.util.IvyDependencyResolver;
import com.fizzed.blaze.util.EngineHelper;
import com.fizzed.blaze.util.FileHelper;
import com.fizzed.blaze.util.Timer;
import java.io.File;
import java.io.IOException;
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
        
        public Blaze build() {
            File blazeDir = null;
            File blazeFile = null;
            
            // if file was set it supercedes all
            if (this.file != null) {
                blazeDir = this.file.getParentFile();
                blazeFile = this.file;
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
                    throw new BlazeException("Unable to find a blaze file (e.g. blaze.js). Perhaps this is not a Blaze project?");
                }
                
                if (blazeFiles.length > 1) {
                    throw new BlazeException("More than one blaze file found. Either delete the extra files use -f parameter");
                }
                
                blazeDir = this.directory;
                blazeFile = blazeFiles[0];
            }
            
            // at this point we should have a file - verify it exists and works
            if (!blazeFile.exists()) {
                throw new BlazeException("Blaze file " + blazeFile + " not found. Perhaps this is not a Blaze project?");
            }
            
            if (!blazeFile.isFile()) {
                throw new BlazeException("Blaze file " + blazeFile + " is not a file. Perhaps this is not a Blaze project?");
            }
            
            
            if (blazeDir != null && blazeDir.getPath().equals(".")) {
                blazeDir = null;
            }
            
            blazeFile = FileHelper.relativizeToJavaWorkingDir(blazeFile);
            
            log.trace("Using blaze dir {} and file {}", blazeDir, blazeFile);
            
            
            //
            // configuration
            //
            File configFile = ConfigHelper.file(blazeDir, blazeFile);
            
            Config config = ConfigHelper.create(configFile);

            
            //
            // context (and set to get thread)
            //
            Context context = new ContextImpl((blazeDir != null ? blazeDir.toPath() : null), blazeFile.toPath(), config);
            ContextHolder.set(context);
            
            
            String fileExtension = FileHelper.fileExtension(blazeFile);
            
            
            //
            // any dependencies that need to be resolved (in case engine itself is a dependency)
            //
            log.info("Resolving dependencies...");
            Timer dependencyTimer = new Timer();
            
            // save which dependencies are already resolved
            List<Dependency> resolvedDependencies
                    = (collectedDependencies != null ? collectedDependencies : DependencyHelper.alreadyBundled());
            
            // any well known engines to include?
            List<Dependency> wellKnownEngineDependencies = DependencyHelper.wellKnownEngineDependencies(fileExtension);
            
            // did script declare any dependencies we need to include?
            List<Dependency> applicationDependencies = DependencyHelper.applicationDependencies(config);
            
            // build dependencies to resolve (need collected so correct versions are picked)
            List<Dependency> dependencies = new ArrayList<>();
            DependencyHelper.collect(dependencies, resolvedDependencies);
            DependencyHelper.collect(dependencies, wellKnownEngineDependencies);
            DependencyHelper.collect(dependencies, applicationDependencies);
            
            int classPathChanges = 0;
            
            if (!dependencies.isEmpty()) {
                try {
                    // resolve dependencies against collected dependencies
                    List<File> jarFiles = dependencyResolver.resolve(context, resolvedDependencies, dependencies);
                    
                    if (jarFiles != null) {
                        for (File jarFile : jarFiles) {
                            int changed 
                                    = ClassLoaderHelper.addFileToClassPath(jarFile, Thread.currentThread().getContextClassLoader());

                            if (changed > 0) {
                                log.info("Adding {} to classpath", jarFile.getName());
                                log.debug(" => {}", jarFile);
                            }

                            classPathChanges += changed;
                        }
                    }
                } catch (DependencyResolveException e) {
                    throw e;
                } catch (IOException | ParseException e) {
                    throw new BlazeException("Unable to cleanly resolve dependencies", e);
                }
            }
            
            log.info("Resolved dependencies in {} ms", dependencyTimer.stop().millis());
            
            
            //
            // find and prepare by extension
            //
            log.info("Compiling script...");
            Timer engineTimer = new Timer();
            
            Engine engine = EngineHelper.findByFileExtension(fileExtension, (classPathChanges > 0));
            
            if (engine == null) {
                throw new BlazeException("Unable to find script engine for file extension " + fileExtension + ". Maybe bad file extension or missing dependency?");
            }

            log.debug("Using script engine {}", engine.getClass().getCanonicalName());
            
            if (!engine.isInitialized()) {
                engine.init(context);
            }
            
            Script script = engine.compile(context);
            
            log.info("Compiled script in {} ms", engineTimer.stop().millis());
            
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
            task = context.config().getString(Config.KEY_DEFAULT_TASK, Config.DEFAULT_TASK);
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

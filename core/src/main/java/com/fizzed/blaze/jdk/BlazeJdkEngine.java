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
package com.fizzed.blaze.jdk;

import com.fizzed.blaze.Context;
import com.fizzed.blaze.core.BlazeException;
import com.fizzed.blaze.core.ContextImpl;
import com.fizzed.blaze.core.Engine;
import com.fizzed.blaze.core.MessageOnlyException;
import com.fizzed.blaze.core.Script;
import com.fizzed.blaze.internal.AbstractEngine;
import com.fizzed.blaze.internal.ClassLoaderHelper;
import com.fizzed.blaze.internal.ConfigHelper;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.tools.Diagnostic;
import javax.tools.DiagnosticCollector;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BlazeJdkEngine extends AbstractEngine<Script> {
    static private final Logger log = LoggerFactory.getLogger(BlazeJdkEngine.class);

    @Override
    public String getFileExtension() {
        return ".java";
    }
    
    @Override
    public void init(Context initialContext) throws BlazeException {
        super.init(initialContext);
    }

    @Override
    public Script compile(Context context) throws BlazeException {
        // what class would we be producing?
        String className = context.scriptFile().toFile().getName().replace(".java", "");
        
        Path classesPath = null;
        Path expectedClassFile = null;
        long sourceLastModified = 0;
        long compiledLastModified = 0;
        
        try {
            // directory to output classs to semi-permanently
            classesPath = ConfigHelper.semiPersistentClassesPath(context);
            
            sourceLastModified = Files.getLastModifiedTime(context.scriptFile()).toMillis();
            
            // do we need to recompile?
            expectedClassFile = classesPath.resolve(className + ".class");
            
            if (Files.exists(expectedClassFile)) {
                compiledLastModified = Files.getLastModifiedTime(expectedClassFile).toMillis();
            }
        } catch (IOException e) {
            throw new BlazeException("Unable to get or create path to compile classes", e);
        }
        
        if (sourceLastModified <= compiledLastModified) {
            log.info("No need to recompile!");
        } else {
            javac(context, classesPath);
        }
        
        // add directory it was compiled to classpath
        int changes = ClassLoaderHelper.addFileToClassPath(classesPath, Thread.currentThread().getContextClassLoader());
        if (changes > 0) {
            log.info("Adding {} to classpath", classesPath);
        }
        
        // create new instance of this class
        try {
            Class<?> type = Class.forName(className);
            
            Object object = type.newInstance();
            
            //log.debug("class {}", object.getClass());
            
            return new BlazeJdkScript(this, object);
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException e) {
            throw new BlazeException("Unable to load class '" + className + "'", e);
        }
    }
    
    public void javac(Context context, Path classesPath) throws BlazeException {
        // classpath may have been adjusted at runtime, build a new one
        StringBuilder classpath = new StringBuilder();
        
        URL[] classpathUrls = ((URLClassLoader)(Thread.currentThread().getContextClassLoader())).getURLs();
        for (URL url : classpathUrls) {
            if (classpath.length() > 0) {
                classpath.append(File.pathSeparator);
            }
            
            try {
                classpath.append(new File(url.toURI()).getAbsolutePath());
            } catch (Exception e) {
                throw new BlazeException("Unable to build javac classpath", e);
            }
        }

        List<String> javacOptions = new ArrayList<>();

        javacOptions.add("-source");
        javacOptions.add("1.8");
        javacOptions.add("-target");
        javacOptions.add("1.8");
        
        // classpath to compile java file with
        javacOptions.add("-cp");
        javacOptions.add(classpath.toString());

        // directory to output compiles classes
        javacOptions.add("-d");
        javacOptions.add(classesPath.toString());

        javacOptions.add("-Xlint:unchecked");
        
        //
        // java -> class
        //
        JavaCompiler compiler = loadJavaCompiler(context);

        DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<>();

        StandardJavaFileManager fileManager = compiler.getStandardFileManager(null, null, null);
        
        Iterable<? extends JavaFileObject> compilationUnits =
                fileManager.getJavaFileObjectsFromFiles(Arrays.asList(context.scriptFile().toFile()));

        JavaCompiler.CompilationTask task
                = compiler.getTask(null, null, diagnostics, javacOptions, null, compilationUnits);

        boolean success = task.call();
        
        if (!success) {
            log.info("---- Compilation Error ----");
        }
        
        // e.g. [ERROR] /home/joelauer/workspace/fizzed/java-blaze/core/src/main/java/com/fizzed/blaze/jdk/BlazeJdkEngine.java:[163,60] ';' expected
        for (Diagnostic diagnostic : diagnostics.getDiagnostics()) {
            JavaFileObject jfo = (JavaFileObject)diagnostic.getSource();
            
            File javaFile = new File(jfo.toUri());
            
            // build message
            String diagnosticMessage = new StringBuilder()
                .append(javaFile)
                .append(":[")
                .append(diagnostic.getLineNumber())
                .append(",")
                .append(diagnostic.getColumnNumber())
                .append("] ")
                .append(diagnostic.getMessage(null))
                .toString();
                    
            switch (diagnostic.getKind()) {
                case ERROR:
                    log.error(diagnosticMessage);
                    break;
                case MANDATORY_WARNING:
                case WARNING:
                    log.warn(diagnosticMessage);
                    break;
                case OTHER:
                case NOTE:
                    log.info(diagnosticMessage);
                    break;
            }
        }
        
        if (!success) {
            throw new MessageOnlyException("Unable to compile " + context.scriptFile());
        }
    }
    
    static public JavaCompiler loadJavaCompiler(Context context) {
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        
        if (compiler == null) {
            // try to fallback to the eclipse compiler (if its on classpath)
            Class<?> eclipseJavaCompilerClass = null;
            try {
                eclipseJavaCompilerClass = Class.forName("org.eclipse.jdt.internal.compiler.tool.EclipseCompiler");

                compiler = (JavaCompiler)eclipseJavaCompilerClass.newInstance();
            } catch (InstantiationException | IllegalAccessException | ClassNotFoundException e) {
                // do nothing
            }
        }
        
        if (compiler == null) {
            throw new MessageOnlyException("Unable to compile " + context.scriptFile() + " to a class file.\n"
                + " The system java compiler is missing (are you running a JRE rather than a JDK?)\n"
                + " Either run this with a JDK or add \"org.eclipse.jdt.core.compiler:ecj:<version>\" to blaze.dependencies.");
        }
        
        log.debug("Using java compiler {}", compiler.getClass().getCanonicalName());
        
        return compiler;
    }
    
}

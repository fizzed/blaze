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

import com.fizzed.blaze.BlazeException;
import com.fizzed.blaze.Context;
import com.fizzed.blaze.Engine;
import com.fizzed.blaze.Script;
import com.fizzed.blaze.util.AbstractEngine;
import com.fizzed.blaze.util.ClassLoaderHelper;
import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.tools.Diagnostic;
import javax.tools.DiagnosticCollector;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;
import org.kohsuke.MetaInfServices;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@MetaInfServices(Engine.class)
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
        String className = context.file().getName().replace(".java", "");
        
        // directory to output to
        //File outputDir = this.initialContext.withBaseDir(Paths.get("target", "blaze", "classes"));
        File classesDir = Paths.get("target", "blaze", "classes").toFile();
        classesDir.mkdirs();
        
        // do we need to recompile?
        File expectedClassFile = new File(classesDir, className + ".class");
        if (expectedClassFile.exists() && expectedClassFile.lastModified() >= context.file().lastModified()) {
            log.info("No need to recompile!");
        } else {
            javac(context, classesDir);
        }
        
        // add directory it was compiled to classpath
        int changes = ClassLoaderHelper.addFileToClassPath(classesDir, Thread.currentThread().getContextClassLoader());
        if (changes > 0) {
            log.info("Adding {} to classpath", classesDir);
        }
        
        // create new instance of this class
        try {
            Class<?> type = Class.forName(className);
            
            Object object = type.newInstance();
            
            log.debug("class {}", object.getClass());
            
            return new BlazeJdkScript(this, object);
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException e) {
            throw new BlazeException("Unable to load " + className, e);
        }
    }
    
    public void javac(Context context, File classesDir) throws BlazeException {
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

        // classpath to compile java file with
        javacOptions.add("-cp");
        javacOptions.add(classpath.toString());

        // directory to output compiles classes
        javacOptions.add("-d");
        javacOptions.add(classesDir.getPath());

        javacOptions.add("-Xlint:unchecked");
        
        //
        // java -> class
        //
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();

        DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<>();

        StandardJavaFileManager fileManager = compiler.getStandardFileManager(null, null, null);
        
        Iterable<? extends JavaFileObject> compilationUnits =
                fileManager.getJavaFileObjectsFromFiles(Arrays.asList(context.file()));

        JavaCompiler.CompilationTask task
                = compiler.getTask(null, null, diagnostics, javacOptions, null, compilationUnits);

        boolean success = task.call();
        
        for (Diagnostic diagnostic : diagnostics.getDiagnostics()) {
            //log.deb1ug("java file: {}", jfo.toUri());
            log.error("source: {}", diagnostic.getSource());
            log.error("line num: {}", diagnostic.getLineNumber());
            log.error("col num: {}", diagnostic.getColumnNumber());
            log.error("code: {}", diagnostic.getCode());
            log.error("kind: {}", diagnostic.getKind());
            log.error("pos: {}", diagnostic.getPosition());
            log.error("start pos: {}", diagnostic.getStartPosition());
            log.error("end pos: {}", diagnostic.getEndPosition());
            log.error("message: {}", diagnostic.getMessage(null));
        }
        
        if (!success) {
            throw new BlazeException("Unable to compile " + context.file());
        }
    }
    
}

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
import com.fizzed.blaze.core.MessageOnlyException;
import com.fizzed.blaze.core.AbstractEngine;
import com.fizzed.blaze.core.Dependency;
import com.fizzed.blaze.internal.ClassLoaderHelper;
import static com.fizzed.blaze.internal.ClassLoaderHelper.currentThreadContextClassLoader;
import com.fizzed.blaze.internal.ConfigHelper;
import com.fizzed.blaze.internal.FileHelper;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Path;
import java.security.NoSuchAlgorithmException;
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

public class BlazeJdkEngine extends AbstractEngine<BlazeJdkScript> {
    static private final Logger log = LoggerFactory.getLogger(BlazeJdkEngine.class);

    static public final List<String> EXTS = Arrays.asList(".java");
    
    @Override
    public String getName() {
        return "java";
    }
    
    @Override
    public List<String> getFileExtensions() {
        return EXTS;
    }
    
    @Override
    public void init(Context initialContext) throws BlazeException {
        super.init(initialContext);
    }

    @Override
    public BlazeJdkScript compile(Context context) throws BlazeException {
        // what class would we be producing?
        String className = context.scriptFile().toFile().getName().replace(".java", "");
        
        ClassLoader classLoader = currentThreadContextClassLoader();
        Path classesDir = null;
        Path expectedClassFile = null;
        String scriptHash = null;
        boolean compile = true;
        
        try {
            // directory to save compile classes on a semi-reliable basis
            classesDir = ConfigHelper.userBlazeEngineScriptClassesDir(context, getName());
            log.trace("Using classes dir {}", classesDir);
            
            expectedClassFile = classesDir.resolve(className + ".class");
            
            // to check if we need to recompile we use an md5 hash of the source file
            scriptHash = FileHelper.md5hash(context.scriptFile());
            
            if (FileHelper.verifyHashFileFor(expectedClassFile, scriptHash)) {
                compile = false;
            }
        } catch (NoSuchAlgorithmException | IOException e) {
            throw new BlazeException("Unable to get or create path to compile classes", e);
        }
        
        if (!compile) {
            log.debug("Script has not changed, using previous compiled version");
        } else {
            javac(classLoader, context, classesDir);
            
            try {
                // save the hash for future use
                FileHelper.writeHashFileFor(expectedClassFile, scriptHash);
            } catch (IOException e) {
                throw new BlazeException("Unable to save script hash", e);
            }
        }
        
        // add directory it was compiled to classpath
        if (ClassLoaderHelper.addClassPath(classLoader, classesDir)) {
            log.debug("Added {} to classpath", classesDir);
        }
        
        // create new instance of this class
        try {
            Class<?> type = classLoader.loadClass(className);
            
            Object targetObject = type.getConstructor().newInstance();

            return new BlazeJdkScript(targetObject);
        } catch (ClassNotFoundException | InstantiationException | IllegalArgumentException |
                IllegalAccessException | NoSuchMethodException | InvocationTargetException | SecurityException e) {
            throw new BlazeException("Unable to load class '" + className + "'", e);
        }
    }
    
    public void javac(ClassLoader classLoader, Context context, Path classesDir) throws BlazeException {
        // java compiler requires a classpath to build with - use the existing
        // runtime classpath (not what we started with, but current one)
        String classpath = ClassLoaderHelper.buildClassPathAsString(classLoader);

        List<String> options = new ArrayList<>();

        // if we don't include source & target, the script will be evaluated in the java version currently running
        // the key is that we'll want to not use a cached class version if the JVM version changes, we'll add that to
        // the MD5 hash we calculate for cached copies of blaze scripts
        int javaMajorVersion = ConfigHelper.getJavaSourceVersion(context);
        String sourceVersion = javaMajorVersion <= 8 ? "1."+javaMajorVersion : Integer.toString(javaMajorVersion);

        options.add("-source");
        options.add(sourceVersion);
        options.add("-target");
        options.add(sourceVersion);
        
        // classpath to compile java file with
        options.add("-cp");
        options.add(classpath);

        // directory to output compiles classes
        options.add("-d");
        options.add(classesDir.toString());

        options.add("-Xlint:unchecked");
        
        //
        // java -> class
        //
        JavaCompiler compiler = loadJavaCompiler(classLoader, context, options);

        DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<>();

        StandardJavaFileManager fileManager = compiler.getStandardFileManager(null, null, null);
        
        Iterable<? extends JavaFileObject> compilationUnits =
                fileManager.getJavaFileObjectsFromFiles(Arrays.asList(context.scriptFile().toFile()));

        JavaCompiler.CompilationTask task
                = compiler.getTask(null, null, diagnostics, options, null, compilationUnits);

        log.trace("javac options: {}", options);
        
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
                    // warnings really aren't meaninful for blaze scripts
                    log.trace(diagnosticMessage);
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
    
    static public boolean isSystemCompilerAvailable() {
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        return compiler != null;
    }
    
    static public List<Dependency> compilerDependencies(String fileExtension) {
        // will we be used?
        if (EXTS.contains(fileExtension)) {
            // is there a system compiler?
            if (!isSystemCompilerAvailable()) {
                // we need the eclipse compiler
                log.debug("System compiler missing (running on JRE?). Adding eclipse compiler");
                return Arrays.asList(new Dependency("org.eclipse.jdt", "ecj", "3.43.0"));
            }
        }
        return null;
    }
    
    static public JavaCompiler loadJavaCompiler(ClassLoader classLoader, Context context,List<String> options) {
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        
        if (compiler == null) {
            // try to fallback to the eclipse compiler (if its on classpath)
            Class<?> eclipseJavaCompilerClass = null;
            try {
                eclipseJavaCompilerClass = classLoader.loadClass("org.eclipse.jdt.internal.compiler.tool.EclipseCompiler");

                compiler = (JavaCompiler)eclipseJavaCompilerClass.newInstance();
                
                // add on special options just for ecj
                options.add("-warn:none");
            } catch (InstantiationException | IllegalAccessException | ClassNotFoundException e) {
                // do nothing
            }
        } else {
            // java compiler
            options.add("-Xlint:none");
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

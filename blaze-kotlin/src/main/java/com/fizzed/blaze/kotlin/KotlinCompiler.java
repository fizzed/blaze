/*
 * Copyright 2016 Fizzed, Inc.
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
package com.fizzed.blaze.kotlin;

import com.fizzed.blaze.core.CompilationException;
import com.fizzed.blaze.internal.ClassLoaderHelper;
import org.jetbrains.kotlin.cli.common.CLIConfigurationKeys;
import org.jetbrains.kotlin.cli.common.config.ContentRootsKt;
import org.jetbrains.kotlin.cli.jvm.compiler.EnvironmentConfigFiles;
import org.jetbrains.kotlin.cli.jvm.compiler.KotlinCoreEnvironment;
import org.jetbrains.kotlin.cli.jvm.compiler.KotlinToJVMBytecodeCompiler;
import org.jetbrains.kotlin.cli.jvm.config.JvmContentRootsKt;
import org.jetbrains.kotlin.com.intellij.openapi.Disposable;
import org.jetbrains.kotlin.com.intellij.openapi.util.Disposer;
import org.jetbrains.kotlin.config.CommonConfigurationKeys;
import org.jetbrains.kotlin.config.CompilerConfiguration;
import org.jetbrains.kotlin.config.JVMConfigurationKeys;
import org.jetbrains.kotlin.utils.PathUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;

/**
 * Compiles .kt files to .class files that are saved on the filesystem
 * for later re-use.  Based loosely on:
 * 
 *   https://github.com/JetBrains/kotlin/blob/d89e907f00e7a93a715d540255d98bbe8da57b3e/compiler/tests/org/jetbrains/kotlin/scripts/ScriptTest.java
 *   https://github.com/JetBrains/kotlin/blob/9a762e0fa282deac1ca48ee15e548325b1e7ab2b/libraries/tools/kotlin-maven-plugin/src/main/java/org/jetbrains/kotlin/maven/ExecuteKotlinScriptMojo.java
 * 
 * @author joelauer
 */
public class KotlinCompiler {
    static private final Logger log = LoggerFactory.getLogger(KotlinCompiler.class);

    private final ClassLoader classLoader;
    
    public KotlinCompiler(ClassLoader classLoader) {
        this.classLoader = classLoader;
    }

    public void compile(Path file, Path classesDir, boolean isScript) throws CompilationException {
        // collect and log errors and warnings as compilation occurs
        CountingSLF4JMessageCollector messageCollector = new CountingSLF4JMessageCollector(log);

        // build kotline compiler configuration
        CompilerConfiguration compilerConfiguration = new CompilerConfiguration();
        compilerConfiguration.put(CLIConfigurationKeys.MESSAGE_COLLECTOR_KEY, messageCollector);
        //compilerConfiguration.put(JVMConfigurationKeys.MODULE_NAME, JvmAbi.DEFAULT_MODULE_NAME);

        /*List<File> jdkClassesRootsFromCurrentJre = ;
        System.out.println("jdkClassesRootsFromCurrentJre: " + jdkClassesRootsFromCurrentJre);

        List<File> jvmClassPath = ClassLoaderHelper.buildJvmClassPath();
        System.out.println("jvmClassPath: " + jvmClassPath);*/

        /*List<File> modFiles;
        try {
            modFiles = Files.list(Paths.get("/usr/lib/jvm/current/jmods/"))
                .map(v -> v.toFile())
                .collect(Collectors.toList());
            System.out.println("modFiles: " + modFiles);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        JvmContentRootsKt.addJvmClasspathRoots(compilerConfiguration, modFiles);*/

        // java 8 requires this for its .jars to compile kotlin, for anything else only the "JDK_HOME" set below was key
        JvmContentRootsKt.addJvmClasspathRoots(compilerConfiguration, PathUtil.getJdkClassesRootsFromCurrentJre());
        JvmContentRootsKt.addJvmClasspathRoots(compilerConfiguration, ClassLoaderHelper.buildClassPathAsFiles(classLoader));
        JvmContentRootsKt.addJvmClasspathRoots(compilerConfiguration, ClassLoaderHelper.buildJvmClassPath());
        ContentRootsKt.addKotlinSourceRoot(compilerConfiguration, file.toAbsolutePath().toString());
        // NOTE: Kotlin v1.0.2+ moved this config key around and will break
        // when we bump up the version down the road. Kotlin is a moving target
        // with changing how its compiler internally is called
        //compilerConfiguration.add(CommonConfigurationKeys.SCRIPT_DEFINITIONS_KEY, StandardScriptDefinition.INSTANCE);

        //List<String> classRoots = PathUtil.getJdkClassesRootsFromCurrentJre().stream().map(v -> v.toString()).collect(Collectors.toList());

        compilerConfiguration.put(JVMConfigurationKeys.FRIEND_PATHS, new ArrayList<>());
        compilerConfiguration.put(CommonConfigurationKeys.MODULE_NAME, "blaze");
        compilerConfiguration.put(JVMConfigurationKeys.OUTPUT_DIRECTORY, classesDir.toFile());
//        compilerConfiguration.put(JVMConfigurationKeys.JDK_HOME, Paths.get("/usr/lib/jvm/current").toFile());
        // java 9, 11+ this is apparently needed for a "modular" jdk
        compilerConfiguration.put(JVMConfigurationKeys.JDK_HOME, Paths.get(System.getProperty("java.home")).toFile());

        Disposable disposable = Disposer.newDisposable();
        try {
            KotlinCoreEnvironment env = KotlinCoreEnvironment.createForProduction(
                disposable, compilerConfiguration, EnvironmentConfigFiles.JVM_CONFIG_FILES);

            env.addKotlinSourceRoots(Collections.singletonList(file.toAbsolutePath().toFile()));
            
            boolean compiled = 
                KotlinToJVMBytecodeCompiler.INSTANCE.compileBunchOfSources(env);

            if (!compiled) {
                throw new CompilationException("Unable to cleanly compile " + file
                    + " (" + messageCollector.getErrors() + " errors, "
                    + messageCollector.getWarnings() + " warnings)");
            }
        } finally {
            Disposer.dispose(disposable);
        }
    }
}
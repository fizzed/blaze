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
package com.fizzed.blaze.kotlin;

//import static org.jetbrains.kotlin.cli.jvm.config.ConfigPackage.addJvmClasspathRoots;
//import static org.jetbrains.kotlin.config.ConfigPackage.addKotlinSourceRoot;
import com.fizzed.blaze.core.CompilationException;
import com.fizzed.blaze.internal.ClassLoaderHelper;
import java.io.File;
import java.util.LinkedList;
import java.util.List;

import org.jetbrains.kotlin.cli.common.CLIConfigurationKeys;
import org.jetbrains.kotlin.cli.common.messages.CompilerMessageLocation;
import org.jetbrains.kotlin.cli.common.messages.CompilerMessageSeverity;
import org.jetbrains.kotlin.cli.common.messages.MessageCollector;
import org.jetbrains.kotlin.cli.jvm.compiler.CommandLineScriptUtils;
import org.jetbrains.kotlin.cli.jvm.compiler.EnvironmentConfigFiles;
import org.jetbrains.kotlin.cli.jvm.compiler.KotlinCoreEnvironment;
import org.jetbrains.kotlin.cli.jvm.compiler.KotlinToJVMBytecodeCompiler;
import org.jetbrains.kotlin.cli.jvm.config.JVMConfigurationKeys;
import org.jetbrains.kotlin.config.CompilerConfiguration;
import org.jetbrains.kotlin.resolve.AnalyzerScriptParameter;
import org.jetbrains.kotlin.utils.PathUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.intellij.openapi.Disposable;
import java.nio.file.Path;
import java.util.concurrent.atomic.AtomicInteger;
import static org.jetbrains.kotlin.cli.jvm.config.JvmContentRootsKt.addJvmClasspathRoots;
import static org.jetbrains.kotlin.config.ContentRootsKt.addKotlinSourceRoot;
import org.jetbrains.kotlin.parsing.JetScriptDefinitionProvider;
//import com.xafero.dynkt.util.ReflUtils;

public class KotlinCompiler implements MessageCollector, Disposable {
    static private final Logger log = LoggerFactory.getLogger(KotlinCompiler.class);

    private final ClassLoader classLoader;
    private final List<String> configPaths;
    private final AtomicInteger compileErrors;
    private final AtomicInteger compileWarnings;

    public KotlinCompiler(ClassLoader classLoader) {
        this.classLoader = classLoader;
        this.configPaths = EnvironmentConfigFiles.JVM_CONFIG_FILES;
        this.compileErrors = new AtomicInteger();
        this.compileWarnings = new AtomicInteger();
    }

    public void compile(Path file, Path classesDir, boolean isScript) throws CompilationException {
        this.compileErrors.set(0);
        this.compileWarnings.set(0);
        CompilerConfiguration configuration = buildCompilerConfiguration(classLoader, file.toFile());
        KotlinCoreEnvironment env = KotlinCoreEnvironment.createForProduction(this, configuration, configPaths);
        
        // marking it as a script dramatically changes what is produced
        if (isScript) {
            JetScriptDefinitionProvider.getInstance(env.getProject()).markFileAsScript(env.getSourceFiles().get(0));
        }
        
        boolean compiled = 
            KotlinToJVMBytecodeCompiler.compileBunchOfSources(env, null, classesDir.toFile(), false);
        
        if (!compiled) {
            throw new CompilationException("Unable to compile with " + this.compileErrors.get()
                    + " errors and " + this.compileWarnings.get() + " warnings.");
        }
    }

    private CompilerConfiguration buildCompilerConfiguration(ClassLoader classLoader, File file) {
        CompilerConfiguration config = new CompilerConfiguration();
        config.put(CLIConfigurationKeys.MESSAGE_COLLECTOR_KEY, this);
        
        // Put arguments as field
        List<AnalyzerScriptParameter> scriptParams = new LinkedList<>();
        scriptParams.addAll(CommandLineScriptUtils.scriptParameters());
        
        config.put(JVMConfigurationKeys.MODULE_NAME, "");
        config.put(JVMConfigurationKeys.SCRIPT_PARAMETERS, scriptParams);
        
        addJvmClasspathRoots(config, PathUtil.getJdkClassesRoots());
        
        addJvmClasspathRoots(config, ClassLoaderHelper.buildClassPathAsFiles(classLoader));
        
        addKotlinSourceRoot(config, file.getAbsolutePath());
        
        return config;
    }

    @Override
    public void report(CompilerMessageSeverity severity, String message, CompilerMessageLocation location) {
        switch (severity) {
            case ERROR:
            case EXCEPTION:
                this.compileErrors.incrementAndGet();
                log.error(message + " " + location);
                break;
            case INFO:
                log.info(message + " " + location);
                break;
            case WARNING:
                this.compileWarnings.incrementAndGet();
                log.warn(message + " " + location);
                break;
            default:
                log.debug(message + " " + location);
                break;
        }
    }

    @Override
    public void dispose() {
        // nothing to do
    }
}
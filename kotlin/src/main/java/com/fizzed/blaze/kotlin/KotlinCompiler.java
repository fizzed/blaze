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
import java.io.File;
import java.util.LinkedList;
import java.util.List;

import org.jetbrains.kotlin.builtins.KotlinBuiltIns;
import org.jetbrains.kotlin.cli.common.CLIConfigurationKeys;
import org.jetbrains.kotlin.cli.common.arguments.K2JVMCompilerArguments;
import org.jetbrains.kotlin.cli.common.messages.CompilerMessageLocation;
import org.jetbrains.kotlin.cli.common.messages.CompilerMessageSeverity;
import org.jetbrains.kotlin.cli.common.messages.MessageCollector;
import org.jetbrains.kotlin.cli.jvm.K2JVMCompiler;
import org.jetbrains.kotlin.cli.jvm.compiler.CommandLineScriptUtils;
import org.jetbrains.kotlin.cli.jvm.compiler.EnvironmentConfigFiles;
import org.jetbrains.kotlin.cli.jvm.compiler.KotlinCoreEnvironment;
import org.jetbrains.kotlin.cli.jvm.compiler.KotlinToJVMBytecodeCompiler;
import org.jetbrains.kotlin.cli.jvm.config.JVMConfigurationKeys;
import org.jetbrains.kotlin.config.CompilerConfiguration;
import org.jetbrains.kotlin.name.Name;
import org.jetbrains.kotlin.resolve.AnalyzerScriptParameter;
//import org.jetbrains.kotlin.types.JetType;
import org.jetbrains.kotlin.utils.KotlinPaths;
import org.jetbrains.kotlin.utils.PathUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.intellij.openapi.Disposable;
import static org.jetbrains.kotlin.cli.jvm.config.JvmContentRootsKt.addJvmClasspathRoots;
import static org.jetbrains.kotlin.config.ContentRootsKt.addKotlinSourceRoot;
//import com.xafero.dynkt.util.ReflUtils;

public class KotlinCompiler implements MessageCollector, Disposable {

    private static final Logger log = LoggerFactory.getLogger("kc");

    private final KotlinPaths paths;
    private final List<String> configPaths;

    public KotlinCompiler() {
        this.paths = PathUtil.getKotlinPathsForCompiler();
        this.configPaths = EnvironmentConfigFiles.JVM_CONFIG_FILES;
    }

    public Class<?> compileScript(File file) {
        log.info("Compiling '{}'...", file);
        CompilerConfiguration config = createCompilerConfig(file);
        config = addCurrentClassPath(config);
        KotlinCoreEnvironment env = KotlinCoreEnvironment.createForProduction(this, config, configPaths);
        return KotlinToJVMBytecodeCompiler.compileScript(config, paths, env);
    }

    @SuppressWarnings("unchecked")
    private CompilerConfiguration addCurrentClassPath(CompilerConfiguration config) {
        K2JVMCompilerArguments args = new K2JVMCompilerArguments();
        args.classpath = System.getProperty("java.class.path");
        args.noStdlib = true;
        args.module = "Hello";
        args.moduleName = "Hello";
        
        //addJvmClasspathRoots(config,
         //       (List<File>) ReflUtils.invoke(K2JVMCompiler.class, null, "getClasspath", paths, cmpArgs));
        return config;
    }

    private CompilerConfiguration createCompilerConfig(File file) {
        CompilerConfiguration config = new CompilerConfiguration();
        config.put(CLIConfigurationKeys.MESSAGE_COLLECTOR_KEY, this);
        // Put arguments as field
        List<AnalyzerScriptParameter> scriptParams = new LinkedList<AnalyzerScriptParameter>();
        scriptParams.addAll(CommandLineScriptUtils.scriptParameters());
        // Bundle injection
        //JetType type = KotlinBuiltIns.getInstance().getMutableMap().getDefaultType();
        //Name ctxName = Name.identifier("ctx");
        //scriptParams.add(new AnalyzerScriptParameter(ctxName, type));
        // Finish configuration
        config.put(JVMConfigurationKeys.SCRIPT_PARAMETERS, scriptParams);
        addJvmClasspathRoots(config, PathUtil.getJdkClassesRoots());
        addKotlinSourceRoot(config, file.getAbsolutePath());
        return config;
    }

    @Override
    public void report(CompilerMessageSeverity severity, String message, CompilerMessageLocation location) {
        switch (severity) {
            case ERROR:
            case EXCEPTION:
                log.error(message + " " + location);
                break;
            case INFO:
                log.info(message + " " + location);
                break;
            case WARNING:
                log.warn(message + " " + location);
                break;
            default:
                log.debug(message + " " + location);
                break;
        }
    }

    @Override
    public void dispose() {
        log.info("Disposed.");
    }
}
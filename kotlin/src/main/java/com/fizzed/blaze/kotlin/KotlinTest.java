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

import java.io.File;
import org.jetbrains.kotlin.cli.common.messages.MessageCollector;
import org.jetbrains.kotlin.cli.common.messages.MessageCollectorUtil;
import org.jetbrains.kotlin.cli.jvm.compiler.KotlinCoreEnvironment;
import org.jetbrains.kotlin.cli.jvm.compiler.KotlinToJVMBytecodeCompiler;
import org.jetbrains.kotlin.cli.jvm.config.JVMConfigurationKeys;
import org.jetbrains.kotlin.codegen.CompilationException;
import org.jetbrains.kotlin.config.CommonConfigurationKeys;
import org.jetbrains.kotlin.config.CompilerConfiguration;
import org.jetbrains.kotlin.utils.KotlinPaths;
import org.jetbrains.kotlin.utils.PathUtil;

/**
 *
 * @author Joe Lauer
 */
public class KotlinTest {
    
    static public void main(String[] args) {
        KotlinCompiler compiler = new KotlinCompiler();
        
        compiler.compileScript(new File("hello.kt"));
        
        /**
        //CompilerConfiguration config = new CompilerConfiguration();
        
        //KotlinPaths paths = new KotlinPaths();
        
        //KotlinToJVMBytecodeCompiler.compileScript(config, , kce)
        
        KotlinPaths paths = PathUtil.getKotlinPathsForCompiler();
        //MessageCollector messageCollector = MessageCollectorPlainTextToStream.PLAIN_TEXT_TO_SYSTEM_ERR;

        //Disposable rootDisposable = Disposer.newDisposable();
        try {
            //KotlinCoreEnvironment kce = new KotlinCoreEnvironment();
            
            CompilerConfiguration configuration = new CompilerConfiguration();
            
                    //JetTestUtils.compilerConfigurationForTests(ConfigurationKind.JDK_AND_ANNOTATIONS, TestJdkKind.MOCK_JDK);
            //configuration.put(CLIConfigurationKeys.MESSAGE_COLLECTOR_KEY, messageCollector);
            //configuration.add(CommonConfigurationKeys.SOURCE_ROOTS_KEY, "compiler/testData/script/" + scriptPath);
            //configuration.addAll(CommonConfigurationKeys.SCRIPT_DEFINITIONS_KEY, scriptDefinitions);
            //configuration.put(JVMConfigurationKeys.SCRIPT_PARAMETERS, scriptParameters);

            //JetCoreEnvironment environment =
            //        JetCoreEnvironment.createForProduction(rootDisposable, configuration, EnvironmentConfigFiles.JVM_CONFIG_FILES);

            try {
                //JetScriptDefinitionProvider.getInstance(environment.getProject()).markFileAsScript(environment.getSourceFiles().get(0));
                KotlinToJVMBytecodeCompiler.compileScript(configuration, );
            }
            catch (CompilationException e) {
                //messageCollector.report(CompilerMessageSeverity.EXCEPTION, OutputMessageUtil.renderException(e),
                //                        MessageUtil.psiElementToMessageLocation(e.getElement()));
                //return null;
            }
            catch (Throwable t) {
                //MessageCollectorUtil.reportException(messageCollector, t);
                //return null;
            }
        }
        finally {
            //Disposer.dispose(rootDisposable);
        }
        */
    }
    
}

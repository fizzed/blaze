/*
 * Copyright 2014 Fizzed Inc.
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
package co.fizzed.blaze.action;

import co.fizzed.blaze.core.Context;
import co.fizzed.stork.launcher.Configuration;
import co.fizzed.stork.launcher.FileUtil;
import co.fizzed.stork.launcher.Generator;
import java.io.File;
import java.util.List;

/**
 *
 * @author joelauer
 */
public class StorkGenerateAction extends Action<Integer> {

    private File outputDir;
    private File inputFile;
    
    public StorkGenerateAction(Context context) {
        super(context);
    }

    public File getOutputDir() {
        return outputDir;
    }

    public StorkGenerateAction outputDir(File outputDir) {
        this.outputDir = outputDir;
        return this;
    }

    public File getInputFile() {
        return inputFile;
    }

    public StorkGenerateAction inputFile(File inputFile) {
        this.inputFile = inputFile;
        return this;
    }
    
    @Override
    protected Result<Integer> execute() throws Exception {
        Generator generator = new Generator();
        List<File> inputFiles = FileUtil.findFiles(this.inputFile.getPath(), true);
        List<Configuration> configs = generator.readConfigurationFiles(inputFiles);
        int generated = generator.generateAll(configs, outputDir);
        return new Result(generated);
    }
    
}

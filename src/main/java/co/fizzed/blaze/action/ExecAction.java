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
import java.io.File;
import java.io.FileNotFoundException;
import org.zeroturnaround.exec.ProcessExecutor;
import org.zeroturnaround.exec.ProcessResult;

/**
 *
 * @author joelauer
 */
public class ExecAction extends Action<ProcessResult> {

    private final ProcessExecutor pe;
    
    public ExecAction(Context context) {
        super(context, "exec");
        this.pe = new ProcessExecutor()
            .redirectInput(System.in)
            .redirectOutput(System.out)
            .redirectErrorStream(true)
            .exitValueNormal();
    }
    
    public ExecAction command(String... command) throws Exception {
        // first argument is a command we need to search for
        String exeName = command[0];
        
        File exeFile = WhichAction.findExecutable(context, exeName);
        if (exeFile == null) {
            throw new FileNotFoundException("Unable to find executable [" + exeName + "]");
        }
        
        command[0] = exeFile.getAbsolutePath();
        this.pe.command(command);
        
        return this;
    }
    
    @Override
    protected Result<ProcessResult> execute() throws Exception {
        ProcessResult pr = this.pe.execute();
        return new Result(pr);
    }
}

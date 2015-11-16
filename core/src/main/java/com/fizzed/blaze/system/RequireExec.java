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
package com.fizzed.blaze.system;

import com.fizzed.blaze.Context;
import com.fizzed.blaze.core.Action;
import com.fizzed.blaze.core.BlazeException;
import com.fizzed.blaze.core.MessageOnlyException;
import java.nio.file.Path;
import java.util.List;
import com.fizzed.blaze.core.PathsMixin;
import java.io.File;

/**
 * 
 * @author joelauer
 */
public class RequireExec extends Action<Path> implements PathsMixin<RequireExec> {
    
    private final Which which;
    private String message;
    
    public RequireExec(Context context) {
        super(context);
        // which will be used to locate the executable
        this.which = new Which(context);
    }
    
    @Override
    public List<Path> getPaths() {
        return this.which.getPaths();
    }
    
    public RequireExec command(Path command) {
        this.which.command(command);
        return this;
    }
    
    public RequireExec command(File command) {
        this.which.command(command);
        return this;
    }
    
    public RequireExec command(String command) {
        this.which.command(command);
        return this;
    }
    
    public RequireExec message(String message) {
        this.message = message;
        return this;
    }

    @Override
    protected Path doRun() throws BlazeException {
        Path exeFile = this.which.run();
        
        if (exeFile == null) {
            throw new MessageOnlyException("Unable to find the required executable '" + this.which.getCommand() + "'."
                + (message != null ? " " + message : ""));
        }
        
        return exeFile;
    }
    
}

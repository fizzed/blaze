/*
 * Copyright 2020 Fizzed, Inc.
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
package com.fizzed.blaze.docker;

import com.fizzed.blaze.Context;
import com.fizzed.blaze.core.BlazeException;
import com.fizzed.blaze.local.LocalExec;
import com.fizzed.blaze.system.Exec;

public class DockerExec extends LocalExec {

    private final DockerSession session;

    public DockerExec(Context context, DockerSession session) {
        super(context);
        this.session = session;
    }

    @Override
    protected Exec.Result doRun() throws BlazeException {
        
        // we need to run a docker command locally, which is what we'll do!
        final LocalExec localExec = new LocalExec(this.context);
        
        localExec.command("docker");
        
        localExec.arg("exec");
        localExec.args("-i");

        localExec.arg(this.session.uri().getHost());
        
        if (this.which.getCommand() != null) {
            localExec.arg(this.which.getCommand());
        }
        
        // now include all arguments really submitted...
        this.arguments.forEach(v -> localExec.arg(v));
        
        
        localExec.pipeInput(this.pipeInput);
        localExec.pipeOutput(this.pipeOutput);
        localExec.pipeError(this.pipeError);
        localExec.pipeErrorToOutput(this.pipeErrorToOutput);
        
        
        
        return localExec.runResult();
    }

}
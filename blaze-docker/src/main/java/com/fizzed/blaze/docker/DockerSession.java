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
import com.fizzed.blaze.system.Exec;
import com.fizzed.blaze.system.ExecSession;
import com.fizzed.blaze.util.ImmutableUri;

public class DockerSession implements ExecSession {

    private final Context context;
    private final ImmutableUri uri;

    public DockerSession(Context context, ImmutableUri uri) {
        this.context = context;
        this.uri = uri;
        
        if (!"docker".equalsIgnoreCase(this.uri.getScheme())) {
            throw new BlazeException("Only docker scheme supported");
        }
    }

    public Context context() {
        return this.context;
    }
    
    public ImmutableUri uri() {
        return this.uri;
    }
    
    @Override
    public Exec newExec() {
        return new DockerExec(this.context, this);
    }
    
}
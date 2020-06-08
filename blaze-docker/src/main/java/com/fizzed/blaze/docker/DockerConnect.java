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
package com.fizzed.blaze.docker;

import com.fizzed.blaze.Context;
import com.fizzed.blaze.core.Action;
import com.fizzed.blaze.core.BlazeException;
import com.fizzed.blaze.util.MutableUri;
import com.fizzed.blaze.util.ObjectHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DockerConnect extends Action<DockerConnect.Result,DockerSession> {
    static private final Logger log = LoggerFactory.getLogger(DockerConnect.class);

    private MutableUri uri;
    
    public DockerConnect(Context context) {
        this(context, new MutableUri("docker:/"));
    }
    
    public DockerConnect(Context context, MutableUri uri) {
        super(context);
        this.uri = uri;
    }

    static public class Result extends com.fizzed.blaze.core.Result<DockerConnect,DockerSession,Result> {
        
        Result(DockerConnect action, DockerSession value) {
            super(action, value);
        }
        
    }
    
    @Override
    protected Result doRun() throws BlazeException {
        ObjectHelper.requireNonNull(uri, "uri cannot be null");
        ObjectHelper.requireNonNull(uri.getScheme(), "uri scheme is required for docker (e.g. docker://container)");
        ObjectHelper.requireNonNull(uri.getHost(), "uri host is required for docker");
  
        // TODO: verify this docker container is running???
        
        log.info("Connected docker://{}", uri.getHost());

        return new Result(this, new DockerSession(this.context, this.uri.toImmutableUri()));
    }
    
}

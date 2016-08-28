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
package com.fizzed.blaze.ssh;

import com.fizzed.blaze.Context;
import com.fizzed.blaze.core.BlazeException;
import com.fizzed.blaze.util.MutableUri;
import java.nio.file.Path;
import java.util.List;

public class SshChainedConnect extends SshConnect {

    protected final List<SshConnect> chain;
    
    public SshChainedConnect(Context context, List<SshConnect> chain) {
        super(context);
        this.chain = chain;
    }

    public List<SshConnect> getChain() {
        return this.chain;
    }
    
    @Override
    public SshConnect configFile(Path configFile) {
        this.chain.stream().forEach((connect) -> {
            connect.configFile(configFile);
        });
        return this;
    }

    @Override
    public SshConnect identityFile(Path identityFile) {
        this.chain.stream().forEach((connect) -> {
            connect.identityFile(identityFile);
        });
        return this;
    }

    @Override
    public SshConnect keepAliveInterval(long keepAliveInterval) {
        this.chain.stream().forEach((connect) -> {
            connect.keepAliveInterval(keepAliveInterval);
        });
        return this;
    }

    @Override
    public SshConnect knownHostsFile(Path knownHostsFile) {
        this.chain.stream().forEach((connect) -> {
            connect.knownHostsFile(knownHostsFile);
        });
        return this;
    }
    
    @Override
    public SshConnect disableHostChecking() {
        this.chain.stream().forEach((connect) -> {
            connect.disableHostChecking();
        });
        return this;
    }
    
    @Override
    public SshConnect newConnect(MutableUri uri) {
        throw new UnsupportedOperationException("A chain of ssh connects cannot create new connects!");
    }
    
    @Override
    public SshConnect proxy(SshSession session, boolean autoclose) {
        throw new UnsupportedOperationException("A chain of ssh connects cannot have their proxy set!");
    }
    
    @Override
    public MutableUri getUri() {
        // return the last uri for the group?
        return this.chain.get(this.chain.size()-1).getUri();
    }

    @Override
    protected Result doRun() throws BlazeException {
        // connect via jumps
        SshSession session = null;
        
        for (SshConnect connect : chain) {
            if (session != null) {
                // we autoclose sessions on a chain
                connect.proxy(session, true);
            }
            session = connect.run();
        }
        
        // we return the last session in the list
        return super.createResult(session);
    }
    
}

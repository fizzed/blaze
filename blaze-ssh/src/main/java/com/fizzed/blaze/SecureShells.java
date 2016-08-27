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
package com.fizzed.blaze;

import com.fizzed.blaze.ssh.SshChainedConnect;
import com.fizzed.blaze.ssh.SshConnect;
import com.fizzed.blaze.util.MutableUri;
import com.fizzed.blaze.ssh.SshExec;
import com.fizzed.blaze.ssh.SshSession;
import com.fizzed.blaze.ssh.SshSftp;
import com.fizzed.blaze.ssh.impl.JschConnect;
import com.fizzed.blaze.ssh.impl.JschExec;
import com.fizzed.blaze.ssh.impl.JschSftp;
import java.util.ArrayList;
import java.util.List;

public class SecureShells {
    
    static public List<MutableUri> sshUris(String uri) {
        String[] split = uri.split(",");
        List<MutableUri> uris = new ArrayList<>();
        
        for (String u : split) {
            if (!u.startsWith("ssh://")) {
                u = "ssh://" + u;
            }
            uris.add(new MutableUri(u));
        }
        
        return uris;
    }
    
    static public SshConnect sshConnect(String uri) {
        // always try to split it
        List<MutableUri> uris = sshUris(uri);
        
        if (uris.size() == 1) {
            return sshConnect(uris.get(0));
        } else {
            return sshConnect(uris);
        }
    }
    
    static public SshChainedConnect sshConnect(String... chainedUris) {
        List<SshConnect> chain = new ArrayList<>();
        for (String uri : chainedUris) {
            chain.add(sshConnect(uri));
        }
        return new SshChainedConnect(Contexts.currentContext(), chain);
    }
    
    static public SshConnect sshConnect(MutableUri uri) {
        return new JschConnect(Contexts.currentContext(), uri);
    }
    
    static public SshConnect sshConnect(MutableUri... chainedUris) {
        List<SshConnect> chain = new ArrayList<>();
        for (MutableUri uri : chainedUris) {
            chain.add(sshConnect(uri));
        }
        return new SshChainedConnect(Contexts.currentContext(), chain);
    }
    
    static public SshConnect sshConnect(List<MutableUri> chainedUris) {
        List<SshConnect> chain = new ArrayList<>();
        for (MutableUri uri : chainedUris) {
            chain.add(sshConnect(uri));
        }
        return new SshChainedConnect(Contexts.currentContext(), chain);
    }
    
    static public SshExec sshExec(SshSession session) {
        return new JschExec(Contexts.currentContext(), session);
    }
    
    static public SshExec sshExec(SshSession session, String command, Object ... arguments) {
        return new JschExec(Contexts.currentContext(), session)
            .command(command)
            .args(arguments);
    }
    
    static public SshSftp sshSftp(SshSession session) {
        return new JschSftp(Contexts.currentContext(), session);
    }
    
}

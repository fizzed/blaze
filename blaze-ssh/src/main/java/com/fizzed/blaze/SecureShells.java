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

import com.fizzed.blaze.ssh.SshConnect;
import com.fizzed.blaze.util.MutableUri;
import com.fizzed.blaze.ssh.SshExec;
import com.fizzed.blaze.ssh.SshProvider;
import com.fizzed.blaze.ssh.SshSession;
import com.fizzed.blaze.ssh.SshSftp;
import com.fizzed.blaze.ssh.impl.JschExec;
import com.fizzed.blaze.ssh.impl.JschSftp;
import com.fizzed.blaze.util.SchemeProvider;
import java.net.URI;

public class SecureShells {
    
    static public SshConnect sshConnect(String uri) {
        return sshConnect(MutableUri.of(uri));
    }
    
    static public SshConnect sshConnect(URI uri) {
        return sshConnect(new MutableUri(uri));
    }
    
    static public SshConnect sshConnect(MutableUri uri) {
        System.out.println("THIS IS A SNAPSHOT! DUDE!");
        // load provider
        SshProvider provider = SchemeProvider.load(uri.getScheme(), SshProvider.class);
        return provider.connect(Contexts.currentContext(), uri);
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

/*
 * Copyright 2016 Fizzed, Inc.
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
package com.fizzed.blaze.ssh.util;

/**
 * Helps working with an OpenSSH ProxyCommand.  Examples:
 * 
 *   ProxyCommand ssh -q jump1.example.com nc %h %p
 *   ProxyCommand ssh vivek@Jumphost nc FooServer 22
 *   ProxyCommand ssh root@v.backup2 nc %h %p %r
 *   ProxyCommand ssh gateway -W %h:%p
 *   ProxyCommand ssh -i /home/fred/.ssh/rsa_key jumphost.example.org -W %h:%p
 *   ProxyCommand ssh -l fred2 -i /home/fred/.ssh/rsa_key jumphost.example.org -W %h:%p
 * 
 * @author jjlauer
 */
public class ProxyCommand {
   
    private final SshCommand sshCommand;
    
    public ProxyCommand(SshCommand command) {
        this.sshCommand = command;
    }

    public SshCommand getSshCommand() {
        return sshCommand;
    }
    
    static public ProxyCommand parse(String value) {
        if (value.startsWith("ssh")) {
            SshCommand sshCommand = SshCommand.parse(value);
            return new ProxyCommand(sshCommand);
        }
        
        // otherwise we don't support it!
        throw new IllegalArgumentException("Unsupported ProxyCommand '" + value + "'");
    }
    
}

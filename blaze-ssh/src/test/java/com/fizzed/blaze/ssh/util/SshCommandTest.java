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

import java.util.Arrays;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;
import org.junit.Test;

public class SshCommandTest {
    
    @Test(expected=IllegalArgumentException.class)
    public void invalidExecutable() {
        SshCommand.parse("ssh2 -q jump1.example.com nc %h %p");
    }
    
    @Test(expected=IllegalArgumentException.class)
    public void unsupportedOption() {
        SshCommand.parse("ssh -X jump1.example.com nc %h %p");
    }
    
    @Test
    public void parse() {
        /*
         *   ssh vivek@Jumphost nc FooServer 22
         *   ssh root@v.backup2 nc %h %p %r
         *   ssh gateway -W %h:%p
         *   ssh -i /home/fred/.ssh/rsa_key jumphost.example.org -W %h:%p
         *   ssh -l fred2 -i /home/fred/.ssh/rsa_key jumphost.example.org -W %h:%p
         */
        SshCommand cmd = null;
        
        // simple
        cmd = SshCommand.parse("ssh -q jump1.example.com nc %h %p");
        assertThat(cmd.getQuiet(), is(true));
        assertThat(cmd.getTarget(), is("jump1.example.com"));
        assertThat(cmd.getCommands(), is(Arrays.asList("nc", "%h", "%p")));
        
        // simple2
        cmd = SshCommand.parse("ssh vivek@Jumphost nc FooServer 22");
        assertThat(cmd.getTarget(), is("vivek@Jumphost"));
        assertThat(cmd.getCommands(), is(Arrays.asList("nc", "FooServer", "22")));
        
        // lots of whitespace
        cmd = SshCommand.parse("ssh   -q    jump1.example.com    nc %h %p");
        assertThat(cmd.getQuiet(), is(true));
        assertThat(cmd.getTarget(), is("jump1.example.com"));
        assertThat(cmd.getCommands(), is(Arrays.asList("nc", "%h", "%p")));
        
        // normalized switch handling
        cmd = SshCommand.parse("ssh -q46 jump1.example.com");
        assertThat(cmd.getQuiet(), is(true));
        assertThat(cmd.getIpAddrMode(), is(6));
        assertThat(cmd.getTarget(), is("jump1.example.com"));
        assertThat(cmd.getCommands(), is(nullValue()));
        
        cmd = SshCommand.parse("ssh -i /home/fred/.ssh/rsa_key jumphost.example.org -W %h:%p");
        assertThat(cmd.getQuiet(), is(nullValue()));
        assertThat(cmd.getIdentityFile(), is("/home/fred/.ssh/rsa_key"));
        assertThat(cmd.getTarget(), is("jumphost.example.org"));
        assertThat(cmd.getFwdHostPort(), is("%h:%p"));
        assertThat(cmd.getCommands(), is(nullValue()));
        
        cmd = SshCommand.parse("ssh -l fred2 -i /home/fred/.ssh/rsa_key jumphost.example.org -W %h:%p");
        assertThat(cmd.getQuiet(), is(nullValue()));
        assertThat(cmd.getLoginName(), is("fred2"));
        assertThat(cmd.getIdentityFile(), is("/home/fred/.ssh/rsa_key"));
        assertThat(cmd.getTarget(), is("jumphost.example.org"));
        assertThat(cmd.getFwdHostPort(), is("%h:%p"));
        assertThat(cmd.getCommands(), is(nullValue()));
        
        // normalized switch handling
        cmd = SshCommand.parse("ssh -A jump1.example.com");
        assertThat(cmd.getForwardAgent(), is(true));
        assertThat(cmd.getTarget(), is("jump1.example.com"));
        assertThat(cmd.getCommands(), is(nullValue()));
    }
    
}

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

import com.fizzed.blaze.Context;
import com.fizzed.blaze.ssh.SshConnect;
import com.fizzed.blaze.util.MutableUri;
import java.util.Deque;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Parses arguments for ssh.  Primarily to help with ProxyCommand, but potentially
 * useful for other uses.
 * 
 * ssh [-1246AaCfgKkMNnqsTtVvXxYy] [-b bind_address] [-c cipher_spec] [-D [bind_address:]port] [-E log_file] [-e escape_char] [-F configfile]
         [-I pkcs11] [-i identity_file] [-L [bind_address:]port:host:hostport] [-l login_name] [-m mac_spec] [-O ctl_cmd] [-o option] [-p port]
         [-Q cipher | cipher-auth | mac | kex | key] [-R [bind_address:]port:host:hostport] [-S ctl_path] [-W host:port]
         [-w local_tun[:remote_tun]] [user@]hostname [command] 
 * 
 *   ssh -q jump1.example.com nc %h %p
 *   ssh vivek@Jumphost nc FooServer 22
 *   ssh root@v.backup2 nc %h %p %r
 *   ssh gateway -W %h:%p
 *   ssh -i /home/fred/.ssh/rsa_key jumphost.example.org -W %h:%p
 *   ssh -l fred2 -i /home/fred/.ssh/rsa_key jumphost.example.org -W %h:%p
 * 
 * @author jjlauer
 */
public class SshCommand {
    private static final Logger log = LoggerFactory.getLogger(SshCommand.class);
   
    private Boolean forwardAgent;        // -A, -a
    private Integer ipAddrMode;          // -4, -6
    private Boolean quiet;               // -q
    private String configFile;           // [-F configfile]
    private String identityFile;         // [-i identity_file]
    private Integer port;                // [-p port]
    private String loginName;            // [-l login_name]
    private String fwdHostPort;          // [-W host:port]
    private String target;               // [user@]hostname
    private List<String> commands;       // [command]
    
    public SshCommand() {
        this.commands = null;
    }

    public Boolean getForwardAgent() {
        return forwardAgent;
    }

    public void setForwardAgent(Boolean forwardAgent) {
        this.forwardAgent = forwardAgent;
    }

    public Integer getIpAddrMode() {
        return ipAddrMode;
    }

    public void setIpAddrMode(Integer ipAddrMode) {
        this.ipAddrMode = ipAddrMode;
    }

    public Boolean getQuiet() {
        return quiet;
    }

    public void setQuiet(Boolean quiet) {
        this.quiet = quiet;
    }

    public String getConfigFile() {
        return configFile;
    }

    public void setConfigFile(String configFile) {
        this.configFile = configFile;
    }

    public String getIdentityFile() {
        return identityFile;
    }

    public void setIdentityFile(String identityFile) {
        this.identityFile = identityFile;
    }

    public Integer getPort() {
        return port;
    }

    public void setPort(Integer port) {
        this.port = port;
    }

    public String getLoginName() {
        return loginName;
    }

    public void setLoginName(String loginName) {
        this.loginName = loginName;
    }

    public String getFwdHostPort() {
        return fwdHostPort;
    }

    public void setFwdHostPort(String fwdHostPort) {
        this.fwdHostPort = fwdHostPort;
    }

    public String getTarget() {
        return target;
    }

    public void setTarget(String target) {
        this.target = target;
    }

    public List<String> getCommands() {
        return commands;
    }

    public void setCommands(List<String> commands) {
        this.commands = commands;
    }
    
    public MutableUri toUri() {
        // build ssh uri
        MutableUri uri = MutableUri.of("ssh://" + this.target);
        
        // overlay port
        if (this.port != null) {
            uri.port(port);
        }
        
        // overlay login name
        if (this.loginName != null) {
            uri.username(loginName);
        }
        
        return uri;
    }
    
    public String toCommand() {
        if (this.commands == null || this.commands.isEmpty()) {
            if (this.fwdHostPort != null) {
                // NOTE: sorta a hack where we simply create a netcat command for it
                log.debug("ssh -W arg not fully supported; hacking it with netcat!");
                return "nc " + this.fwdHostPort.replace(":", " ");
            } else {
                return null;
            }
        }
        return String.join(" ", this.commands);
    }
    
    public void apply(SshConnect connect) {
        // identity file (how to handle ~ in path?)
        
        // config file (how to handle ~ in path?)
    }

    static public SshCommand parse(String value) {
        // split on whitespace
        // e.g. ssh -q jump1.example.com nc %h %p
        Deque<String> tokens = new ArrayDeque<>(Arrays.asList(value.split("\\s+")));
        
        String executable = tokens.pop();
        if (!Objects.equals(executable, "ssh")) {
            throw new IllegalArgumentException("Unable to parse command for executable '" + executable + "' (only support ssh)");
        }
        
        SshCommand cmd = new SshCommand();
        
        // keep parsing until no more tokens
        while (!tokens.isEmpty()) {
            String maybeArg = tokens.pop();
            for (String arg : normalizeSwitch(maybeArg)) {
                switch (arg) {
                    case "-F":
                        cmd.configFile = tokens.pop();
                        break;
                    case "-i":
                        cmd.identityFile = tokens.pop();
                        break;
                    case "-p":
                        cmd.port = Integer.valueOf(tokens.pop());
                        break;
                    case "-l":
                        cmd.loginName = tokens.pop();
                        break;
                    case "-W":
                        cmd.fwdHostPort = tokens.pop();
                        break;
                    case "-q":
                        cmd.quiet = Boolean.TRUE; 
                        break;
                    case "-A":
                        cmd.forwardAgent = Boolean.TRUE; 
                        break;
                    case "-a":
                        cmd.forwardAgent = Boolean.FALSE; 
                        break;
                    case "-4":
                        cmd.ipAddrMode = 4; 
                        break;
                    case "-6":
                        cmd.ipAddrMode = 6; 
                        break;
                    default:
                        if (arg.startsWith("-")) {
                            throw new IllegalArgumentException("Unsupported ssh switch [" + arg + "]");
                        } else {
                            if (cmd.target == null) {
                                cmd.target = arg;
                            } else {
                                if (cmd.commands == null) {
                                    cmd.commands = new ArrayList<>();
                                }
                                cmd.commands.add(arg);
                            }
                        }
                        break;
                }
            }
        }
        
        // only required parameter is target
        if (cmd.target == null) {
            throw new IllegalArgumentException("ssh command requires at least [user@]hostname");
        }
        
        return cmd;
    }
    
    static private List<String> normalizeSwitch(String arg) {
        List<String> normalizedSwitches = new ArrayList<>();
        if (arg.startsWith("-") && arg.length() > 2) {
            char[] chars = arg.toCharArray();
            for (int i = 1; i < chars.length; i++) {
                normalizedSwitches.add("-" + chars[i]);
            }
        } else {
            normalizedSwitches.add(arg);
        }
        return normalizedSwitches;
    }
    
}

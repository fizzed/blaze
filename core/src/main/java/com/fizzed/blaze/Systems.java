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

import com.fizzed.blaze.system.Exec;
import com.fizzed.blaze.system.RequireExec;
import com.fizzed.blaze.system.Which;
import java.io.File;

/**
 *
 * @author joelauer
 */
public class Systems {
    
    static public Which which(String command) {
        return new Which(Context.currentContext())
            .command(command);
    }
    
    static public RequireExec requireExec(String command) {
        return new RequireExec(Context.currentContext())
            .command(command);
    }
    
    static public RequireExec requireExec(String command, String message) {
        return new RequireExec(Context.currentContext())
            .command(command)
            .message(message);
    }
    
    static public Exec exec(String command, Object ... arguments) {
        return new Exec(Context.currentContext())
            .command(command, arguments);
    }
    
    static public Exec exec(File command, Object ... arguments) {
        return exec(command.toString(), arguments);
    }
    
    
    
    /**
    static public Scp scp(String user, String host) {
        return new Scp(Context.currentContext())
            .user(user)
            .host(host);
    }
    */
    
}

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

import com.fizzed.blaze.core.ContextImpl;
import com.fizzed.blaze.system.Exec;
import com.fizzed.blaze.system.Remove;
import com.fizzed.blaze.system.RequireExec;
import com.fizzed.blaze.system.Which;
import java.io.File;
import java.nio.file.Path;

/**
 *
 * @author joelauer
 */
public class Systems {
    
    static public Which which(String command) {
        return new Which(Contexts.current())
            .command(command);
    }
    
    static public RequireExec requireExec(String command) {
        return new RequireExec(Contexts.current())
            .command(command);
    }
    
    static public RequireExec requireExec(String command, String message) {
        return new RequireExec(Contexts.current())
            .command(command)
            .message(message);
    }
    
    static public Exec exec(String command, Object ... arguments) {
        return new Exec(Contexts.current())
            .command(command, arguments);
    }
    
    static public Exec exec(File command, Object ... arguments) {
        return exec(command.toString(), arguments);
    }
    
    static public Remove remove(Path... paths) {
        return new Remove(Contexts.current())
            .paths(paths);
    }
    
    static public Remove remove(File... files) {
        return new Remove(Contexts.current())
            .paths(files);
    }
    
    /**
    static public Scp scp(String user, String host) {
        return new Scp(Context.currentContext())
            .user(user)
            .host(host);
    }
    */
    
}

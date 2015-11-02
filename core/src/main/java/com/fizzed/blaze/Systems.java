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
    
    /**
     * Prepares an action to find `which` executable to use for a command.  Similiar to a Unix `which`
     * command.  Platform-specific executable extensions do not need to included
     * (e.g. `.bat` on windows).  Will search for an executable base do on 
     * the environment PATH variable as well as any user-defined paths.
     * 
     * <pre>
     * import static com.fizzed.blaze.Systems.which;
     * 
     * // ...
     * 
     * Path p = which("javac").run();
     * </pre>
     * 
     * @param command The command to search for such as "javac" or "ls"
     * @return A new Which action bound to current context
     */
    static public Which which(String command) {
        return new Which(Contexts.currentContext())
            .command(command);
    }
    
    /**
     * Prepares an action to require an executable to be present or an exception will be thrown and
     * the script will stop executing. Will use which() under-the-hood.
     * 
     * <pre>
     * import static com.fizzed.blaze.Systems.requireExec;
     * 
     * // ...
     * 
     * requireExec("javac").run();
     * </pre>
     * 
     * @param command The command to require for such as "javac" or "ls"
     * @return A new RequireExec action bound to current context
     */
    static public RequireExec requireExec(String command) {
        return new RequireExec(Contexts.currentContext())
            .command(command);
    }
    
    /**
     * Prepares an action to require an executable to be present or an exception will be thrown and
     * the script will stop executing. Will use which() under-the-hood.
     * 
     * <pre>
     * import static com.fizzed.blaze.Systems.requireExec;
     * 
     * // ...
     * 
     * requireExec("javac", "Java is used for compiling stuff.").run();
     * </pre>
     * 
     * @param command The command to require for such as "javac" or "ls"
     * @param message The custom message to display
     * @return A new RequireExec action bound to current context
     */
    static public RequireExec requireExec(String command, String message) {
        return new RequireExec(Contexts.currentContext())
            .command(command)
            .message(message);
    }
    
    /**
     * Prepares an executable to be spawned off in an external process with
     * the supplied arguments.
     * 
     * <pre>
     * import static com.fizzed.blaze.Systems.exec;
     * import static com.fizzed.blaze.system.ExecResult;
     * 
     * // ...
     * 
     * ExecResult result = exec("javac", "-version").run();
     * </pre>
     * 
     * @param command The executable to spawn. Uses Which under-the-hood so
     *      same searching rules will apply.
     * @param arguments The optional list of arguments. The toString() method
     *      will be called on each and passed to the process.
     * @return A new Exec action bound to current context
     */
    static public Exec exec(String command, Object ... arguments) {
        return new Exec(Contexts.currentContext())
            .command(command)
            .args(arguments);
    }
    
    /**
     * Prepares an executable to be spawned off in an external process with
     * the supplied arguments.
     * 
     * <pre>
     * import static com.fizzed.blaze.Systems.exec;
     * import static com.fizzed.blaze.system.ExecResult;
     * 
     * // ...
     * 
     * ExecResult result = exec("javac", "-version").run();
     * </pre>
     * 
     * @param command The executable to spawn. Uses Which under-the-hood so
     *      same searching rules will apply.
     * @param arguments The optional list of arguments. The toString() method
     *      will be called on each and passed to the process.
     * @return A new Exec action bound to current context
     */
    static public Exec exec(Path command, Object ... arguments) {
        return exec(command.toString(), arguments);
    }
    
    /**
     * Prepares an executable to be spawned off in an external process with
     * the supplied arguments.
     * 
     * <pre>
     * import static com.fizzed.blaze.Systems.exec;
     * import static com.fizzed.blaze.system.ExecResult;
     * 
     * // ...
     * 
     * ExecResult result = exec("javac", "-version").run();
     * </pre>
     * 
     * @param command The executable to spawn. Uses Which under-the-hood so
     *      same searching rules will apply.
     * @param arguments The optional list of arguments. The toString() method
     *      will be called on each and passed to the process.
     * @return A new Exec action bound to current context
     */
    static public Exec exec(File command, Object ... arguments) {
        return exec(command.toString(), arguments);
    }
    
    /**
     * Prepares an action to delete one or more files and directories.
     * 
     * <pre>
     * import static com.fizzed.blaze.Systems.remove;
     * 
     * // ...
     * 
     * remove(Paths.get("images"))
     *      .recursive()
     *      .force()
     *      .run();
     * </pre>
     * 
     * @param paths The paths to delete
     * @return A new Remove action bound to current context
     */
    static public Remove remove(Path... paths) {
        return new Remove(Contexts.currentContext())
            .paths(paths);
    }
    
    /**
     * Prepares an action to delete one or more files and directories.
     * 
     * <pre>
     * import static com.fizzed.blaze.Systems.remove;
     * 
     * // ...
     * 
     * remove(Paths.get("images"))
     *      .recursive()
     *      .force()
     *      .run();
     * </pre>
     * 
     * @param files The files to delete
     * @return A new Remove action bound to current context
     */
    static public Remove remove(File... files) {
        return new Remove(Contexts.currentContext())
            .paths(files);
    }
    
}

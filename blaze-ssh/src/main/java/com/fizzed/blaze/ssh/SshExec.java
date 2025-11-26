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
import com.fizzed.blaze.system.Exec;

abstract public class SshExec extends Exec {

    final protected SshSession session;
    protected boolean pty;
    protected boolean disableArgumentSmartEscaping;
    
    public SshExec(Context context, SshSession session) {
        super(context);
        this.session = session;
        this.pty = false;
    }

    /**
     * Allocates a pseudo-terminal (PTY) for the SSH session.
     * This is useful for executing interactive or terminal-based commands within the SSH session.
     *
     * @return This {@code SshExec} instance for method chaining.
     */
    public SshExec pty() {
        return this.pty(true);
    }

    /**
     * Configures whether to allocate a pseudo-terminal (PTY) for the SSH session.
     * A PTY is often required for certain interactive or terminal-emulating commands.
     *
     * @param pty A boolean flag indicating whether to allocate a PTY.
     *            If true, a PTY will be allocated for the SSH session.
     * @return This {@code SshExec} instance for method chaining.
     */
    public SshExec pty(boolean pty) {
        this.pty = pty;
        return this;
    }

    /**
     * Disables smart escaping for arguments when executing commands on the SSH session.
     * This simplifies command execution by preventing automatic escaping of special characters. Your arguments must
     * be properly escaped for the target platform, as "ssh exec" sends a command line String, which is interpreted
     * (usually by a shell like SH or BASH or CMD.EXE) and parsed.
     *
     * @return This {@code SshExec} instance for method chaining.
     */
    public SshExec disableArgumentSmartEscaping() {
        return this.disableArgumentSmartEscaping(true);
    }

    /**
     * Enables or disables smart escaping for arguments when executing commands on the SSH session.
     * When disabled, arguments are not automatically escaped, and it is the caller's responsibility
     * to ensure arguments are correctly escaped for the target platform.
     *
     * @param disableArgumentSmartEscaping A boolean flag indicating whether to disable smart escaping.
     *                                     If true, smart escaping for arguments is disabled.
     * @return This {@code SshExec} instance for method chaining.
     */
    public SshExec disableArgumentSmartEscaping(boolean disableArgumentSmartEscaping) {
        this.disableArgumentSmartEscaping = disableArgumentSmartEscaping;
        return this;
    }

}
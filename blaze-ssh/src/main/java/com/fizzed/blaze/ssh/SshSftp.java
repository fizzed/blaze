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
import com.fizzed.blaze.core.Action;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

abstract public class SshSftp extends Action<SshSftp.Result, SshSftpSession> {
    static private final Logger log = LoggerFactory.getLogger(SshSftp.class);

    final protected SshSession session;
    
    public SshSftp(Context context, SshSession session) {
        super(context);
        this.session = session;
    }
    
    static public class Result extends com.fizzed.blaze.core.Result<SshSftp,SshSftpSession,Result> {
        
        public Result(SshSftp action, SshSftpSession value) {
            super(action, value);
        }
        
    }
    
}

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
package com.fizzed.blaze.haproxy;

import com.fizzed.blaze.Context;
import com.fizzed.blaze.core.Action;
import com.fizzed.blaze.core.BlazeException;
import com.fizzed.blaze.haproxy.impl.HaproxySessionImpl;
import com.fizzed.blaze.system.ExecSession;
import com.fizzed.blaze.util.ObjectHelper;
import com.fizzed.blaze.util.Timer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HaproxyConnect extends Action<HaproxyConnect.Result,HaproxySession> {
    static private final Logger log = LoggerFactory.getLogger(HaproxyConnect.class);

    protected final ExecSession execSession;
    protected boolean sudo;
    protected String adminSocket;
    
    public HaproxyConnect(Context context, ExecSession execSession) {
        super(context);
        this.execSession = execSession;
        this.adminSocket = "/run/haproxy/admin.sock";
        this.sudo = false;
    }

    public HaproxyConnect sudo(boolean sudo) {
        this.sudo = sudo;
        return this;
    }
    
    public HaproxyConnect adminSocket(String path) {
        this.adminSocket = path;
        return this;
    }
    
    static public class Result extends com.fizzed.blaze.core.Result<HaproxyConnect,HaproxySession,Result> {
        
        Result(HaproxyConnect action, HaproxySession value) {
            super(action, value);
        }
        
    }
    
    @Override
    protected Result doRun() throws BlazeException {
        ObjectHelper.requireNonNull(execSession, "execSession cannot be null");
        ObjectHelper.requireNonNull(adminSocket, "adminSocket cannot be null");
        
        log.info("Connecting to haproxy {}...", execSession.uri());
        
        final Timer timer = new Timer();
        
        final HaproxySession session = new HaproxySessionImpl(this.execSession, this.sudo, this.adminSocket);
        
        final HaproxyInfo info = session.getInfo();
        
        log.info("Connected to haproxy {} ({} v{}) (in {})", execSession.uri(), info.getName(), info.getVersion(), timer);
        
        return new Result(this, session);
    }
    
}

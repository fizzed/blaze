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
package com.fizzed.blaze.ssh.impl;

import com.fizzed.blaze.ssh.*;
import com.fizzed.blaze.Context;
import com.fizzed.blaze.core.BlazeException;
import com.fizzed.blaze.util.ObjectHelper;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JschSftp extends SshSftp {
    static private final Logger log = LoggerFactory.getLogger(JschSftp.class);
    
    public JschSftp(Context context, SshSession session) {
        super(context, session);
    }
    
    @Override
    protected Result doRun() throws BlazeException {
        Session jschSession = ((JschSession)session).getJschSession();
        ObjectHelper.requireNonNull(jschSession, "ssh session must be established first");
        
        try {
            ChannelSftp channel = (ChannelSftp)jschSession.openChannel("sftp");
            
            channel.connect();
            
            return new Result(this, new JschSftpSession(session, channel));
        } catch (JSchException e) {
            throw new SshException(e.getMessage(), e);
        }
    }
    
}

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
import com.fizzed.blaze.core.BlazeException;
import com.fizzed.blaze.ssh.impl.JschSession;
import com.fizzed.blaze.ssh.impl.JschSftpSession;
import com.fizzed.blaze.util.ObjectHelper;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author joelauer
 */
public class SshSftp extends Action<SshSftpSession> {
    static private final Logger log = LoggerFactory.getLogger(SshSftp.class);

    final private SshSession session;
    
    public SshSftp(Context context, SshSession session) {
        super(context);
        this.session = session;
    }
    
    @Override
    protected SshSftpSession doRun() throws BlazeException {
        Session jschSession = ((JschSession)session).getJschSession();
        ObjectHelper.requireNonNull(jschSession, "ssh session must be established first");
        
        ChannelSftp channel = null;
        try {
            channel = (ChannelSftp)jschSession.openChannel("sftp");
            
            channel.connect();
            
            return new JschSftpSession(session, channel);
        } catch (JSchException e) {
            throw new SshException(e.getMessage(), e);
        }
    }
    
}

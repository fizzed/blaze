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
package com.fizzed.blaze.shell;

import com.fizzed.blaze.Action;
import com.fizzed.blaze.BlazeException;
import com.fizzed.blaze.Context;
import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.OpenSSHConfig;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.UserInfo;

/**
 *
 * @author joelauer
 */
public class Scp extends Action<Void> {

    private String user;
    private String host;
    private String password;
    
    public Scp(Context context) {
        super(context);
    }
    
    public Scp user(String user) {
        this.user = user;
        return this;
    }
    
    public Scp host(String host) {
        this.host = host;
        return this;
    }
    
    public Scp password(String password) {
        this.password = password;
        return this;
    }
    
    @Override
    public Void run() throws BlazeException {
        JSch jsch = new JSch();
        Session session = null;
        ChannelExec channel = null;
        try {
            session = jsch.getSession(user, host, 22);
            
            session.setDaemonThread(true);
            
            session.setUserInfo(new MyUserInfo());
            
            session.setConfig("StrictHostKeyChecking", "no");
            
            //session.setPassword(password);
            
            //session.setConfig();
            
            session.connect();
            
            System.out.println("Connected!");
            
            channel = (ChannelExec)session.openChannel("exec");
            
            channel.setCommand("ls");
            
            channel.setInputStream(System.in);
            channel.setOutputStream(System.out);
            
            channel.connect();
            
        } catch (JSchException e) {
            throw new BlazeException(e.getMessage(), e);
        } finally {
            if (channel != null) {
                channel.disconnect();
            }
            
            if (session != null) {
                session.disconnect();
            }
        }
        
        return null;
    }
    
    public class MyUserInfo implements UserInfo {

        @Override
        public String getPassphrase() {
            return Scp.this.password;
        }

        @Override
        public String getPassword() {
            return Scp.this.password;
        }

        @Override
        public boolean promptPassword(String string) {
            System.out.println("Password prompt: " + string);
            return true;
        }

        @Override
        public boolean promptPassphrase(String string) {
            System.out.println("Phassphrase prompt: " + string);
            return true;
        }

        @Override
        public boolean promptYesNo(String string) {
            System.out.println("promptYesNo: " + string);
            return true;
        }

        @Override
        public void showMessage(String string) {
            System.out.println("message: " + string);
        }
        
    }
}

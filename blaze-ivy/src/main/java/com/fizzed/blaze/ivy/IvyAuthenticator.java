/*
 * Copyright 2020 Fizzed, Inc.
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
package com.fizzed.blaze.ivy;

import java.net.Authenticator;
import java.net.PasswordAuthentication;
import java.util.concurrent.ConcurrentHashMap;
import org.apache.ivy.util.Credentials;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class IvyAuthenticator extends Authenticator {
    static private final Logger log = LoggerFactory.getLogger(IvyAuthenticator.class);
    
    private final ConcurrentHashMap<String,Credentials> credentials;
    
    public IvyAuthenticator() {
        this.credentials = new ConcurrentHashMap<>();
    }
    
    public void addCredentials(String host, String username, String password) {
        Credentials creds = new Credentials(null, host, username, password);
        this.credentials.put(host.toLowerCase(), creds);
    }

    @Override
    protected PasswordAuthentication getPasswordAuthentication() {

        final String host = this.getRequestingHost();
        final RequestorType type = this.getRequestorType();
        
        log.trace("Authentication requested for {}, {}", type, host);
        
        if (host != null) {
            Credentials creds = this.credentials.get(host.toLowerCase());
            if (creds != null) {   
                return new PasswordAuthentication(creds.getUserName(), creds.getPasswd().toCharArray());
            }
        }
        
        throw new AuthenticationException("Unable to authenticate for " + host + " (please add credentials to ~/.m2/settings.xml)");
    }
    
}
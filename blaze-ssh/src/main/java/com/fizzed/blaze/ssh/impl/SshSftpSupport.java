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

import com.fizzed.blaze.ssh.SshSftpException;
import com.fizzed.blaze.util.PathTranslator;
import com.fizzed.blaze.util.Streamable;
import com.fizzed.blaze.util.VerboseLogger;

import java.io.InputStream;
import java.io.OutputStream;

public interface SshSftpSupport {

    PathTranslator getPathTranslator();

    void get(VerboseLogger log, boolean progress, String source, Streamable<OutputStream> target) throws SshSftpException;
    
    void put(VerboseLogger log, boolean progress, Streamable<InputStream> source, String target) throws SshSftpException;
    
}

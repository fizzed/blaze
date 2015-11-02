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

import com.fizzed.blaze.ssh.SshException;
import com.fizzed.blaze.util.NamedStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Path;

/**
 *
 * @author joelauer
 */
public interface SshSftpSupport {
    
    void get(Path source, NamedStream<OutputStream> target) throws SshException;
    
    void put(NamedStream<InputStream> source, Path target) throws SshException;
    
}

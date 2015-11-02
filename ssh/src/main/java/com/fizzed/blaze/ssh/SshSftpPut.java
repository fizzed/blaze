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

import com.fizzed.blaze.core.Action;
import com.fizzed.blaze.core.BlazeException;
import java.io.File;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;
import com.fizzed.blaze.ssh.impl.SshSftpSupport;
import com.fizzed.blaze.util.NamedStream;
import com.fizzed.blaze.util.ObjectHelper;

/**
 *
 * @author joelauer
 */
public class SshSftpPut extends Action<Void> {

    private final SshSftpSupport sftp;
    private NamedStream<InputStream> source;
    private Path target;
    
    public SshSftpPut(SshSftpSession sftp) {
        super(sftp.session().context());
        this.sftp = (SshSftpSupport)sftp;
        this.target = null;
    }
    
    public SshSftpPut source(String sourceFile) {
        return source(Paths.get(sourceFile));
    }
    
    public SshSftpPut source(Path sourceFile) {
        return source(NamedStream.input(sourceFile));
    }
    
    public SshSftpPut source(File sourceFile) {
        return source(NamedStream.input(sourceFile));
    }
    
    public SshSftpPut source(InputStream source) {
        return source(NamedStream.of(source));
    }
    
    public SshSftpPut source(NamedStream<InputStream> source) {
        this.source = source;
        return this;
    }
    
    public SshSftpPut target(String targetFile) {
        return target(Paths.get(targetFile));
    }
    
    public SshSftpPut target(Path targetFile) {
        this.target = targetFile;
        return this;
    }
    
    public SshSftpPut target(File targetFile) {
        this.target = targetFile.toPath();
        return this;
    }

    @Override
    protected Void doRun() throws BlazeException {
        ObjectHelper.requireNonNull(source, "source cannot be null");
        ObjectHelper.requireNonNull(target, "target cannot be null");
        sftp.put(source, target);
        return null;
    }
    
}

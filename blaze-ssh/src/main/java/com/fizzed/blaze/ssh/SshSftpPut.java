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
import com.fizzed.blaze.core.ProgressMixin;
import com.fizzed.blaze.core.VerbosityMixin;
import java.io.File;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import com.fizzed.blaze.ssh.impl.SshSftpSupport;
import com.fizzed.blaze.util.*;

public class SshSftpPut extends Action<SshSftpPut.Result,Void> implements VerbosityMixin<SshSftpGet>, ProgressMixin<SshSftpGet> {

    private final VerboseLogger log;
    private final ValueHolder<Boolean> progress;
    private final SshSftpSupport sftp;
    private StreamableInput source;
    private String target;
    
    public SshSftpPut(SshSftpSession sftp) {
        super(sftp.session().context());
        this.log = new VerboseLogger(this);
        this.progress = new ValueHolder<>(false);
        this.sftp = (SshSftpSupport)sftp;
        this.target = null;
    }
    
    public SshSftpPut source(String sourceFile) {
        return source(Paths.get(sourceFile));
    }
    
    public SshSftpPut source(Path sourceFile) {
        return source(Streamables.input(sourceFile));
    }
    
    public SshSftpPut source(File sourceFile) {
        return source(Streamables.input(sourceFile));
    }
    
    public SshSftpPut source(InputStream source) {
        return source(Streamables.input(source));
    }
    
    public SshSftpPut source(StreamableInput source) {
        this.source = source;
        return this;
    }
    
    public SshSftpPut target(String targetFile) {
        this.target = targetFile;
        return this;
    }
    
    public SshSftpPut target(Path targetFile) {
        return this.target(this.sftp.getPathTranslator().toRemotePath(targetFile));
    }
    
    public SshSftpPut target(File targetFile) {
        return target(targetFile.toPath());
    }

    @Override
    public VerboseLogger getVerboseLogger() {
        return this.log;
    }

    @Override
    public ValueHolder<Boolean> getProgressHolder() {
        return this.progress;
    }

    @Override
    protected Result doRun() throws BlazeException {
        ObjectHelper.requireNonNull(source, "source cannot be null");
        ObjectHelper.requireNonNull(target, "target cannot be null");
        sftp.put(this.log, this.progress.get(), this.source, this.target);
        return new Result(this, null);
    }
    
    static public class Result extends com.fizzed.blaze.core.Result<SshSftpPut,Void,Result> {
        
        Result(SshSftpPut action, Void value) {
            super(action, value);
        }
        
    }
    
}

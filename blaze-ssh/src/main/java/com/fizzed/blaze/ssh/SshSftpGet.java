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

import com.fizzed.blaze.core.*;

import java.nio.file.Path;
import java.nio.file.Paths;

import com.fizzed.blaze.ssh.impl.SshSftpSupport;
import com.fizzed.blaze.util.*;

/**
 * A class for performing SFTP (SSH File Transfer Protocol) file download operations.
 * This class allows the user to configure the source file to be downloaded, the target
 * location for the downloaded file, and whether to show progress information during the
 * SFTP operation. It facilitates a fluent API for method chaining.
 */
public class SshSftpGet extends Action<SshSftpGet.Result,Void> implements VerbosityMixin<SshSftpGet>, ProgressMixin<SshSftpGet>, TargetOutputMixin<SshSftpGet> {

    private final VerboseLogger log;
    private final ValueHolder<Boolean> progress;
    private final SshSftpSupport sftp;
    private String source;
    private StreamableOutput target;
    
    public SshSftpGet(SshSftpSession sftp) {
        super(sftp.session().context());
        this.log = new VerboseLogger(this);
        this.progress = new ValueHolder<>(false);
        this.sftp = (SshSftpSupport)sftp;
        this.target = null;
    }

    @Override
    public VerboseLogger getVerboseLogger() {
        return this.log;
    }

    @Override
    public ValueHolder<Boolean> getProgressHolder() {
        return this.progress;
    }

    /**
     * Specifies the source file to be downloaded via SFTP using its path as a string.
     *
     * @param sourceFile the path to the source file as a string. This file will be retrieved during the SFTP operation.
     * @return the current instance of {@code SshSftpGet}, allowing for method chaining.
     */
    public SshSftpGet source(String sourceFile) {
        this.source = sourceFile;
        return this;
    }

    /**
     * Sets the source file to be downloaded via SFTP.
     *
     * @param sourceFile the {@code Path} representing the location of the source file
     *                   that will be downloaded during the SFTP operation.
     * @return the current instance of {@code SshSftpGet}, allowing for method chaining.
     */
    public SshSftpGet source(Path sourceFile) {
        return this.source(this.sftp.getPathTranslator().toRemotePath(sourceFile));
    }

    @Override
    public SshSftpGet target(StreamableOutput target) {
        this.target = target;
        return this;
    }

    @Override
    protected Result doRun() throws BlazeException {
        ObjectHelper.requireNonNull(this.source, "source cannot be null");
        ObjectHelper.requireNonNull(this.target, "target cannot be null");
        sftp.get(this.log, this.progress.get(), this.source, this.target);
        return new Result(this, null);
    }
    
    static public class Result extends com.fizzed.blaze.core.Result<SshSftpGet,Void,Result> {
        
        Result(SshSftpGet action, Void value) {
            super(action, value);
        }
        
    }
    
}

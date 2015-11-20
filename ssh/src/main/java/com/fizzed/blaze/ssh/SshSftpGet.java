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
import java.nio.file.Path;
import java.nio.file.Paths;
import com.fizzed.blaze.ssh.impl.SshSftpSupport;
import com.fizzed.blaze.util.ObjectHelper;
import com.fizzed.blaze.util.StreamableOutput;
import com.fizzed.blaze.util.Streamables;
import java.io.OutputStream;

public class SshSftpGet extends Action<SshSftpGet.Result,Void> {

    private final SshSftpSupport sftp;
    private StreamableOutput target;
    private Path source;
    
    public SshSftpGet(SshSftpSession sftp) {
        super(sftp.session().context());
        this.sftp = (SshSftpSupport)sftp;
        this.target = null;
    }
    
    public SshSftpGet source(String sourceFile) {
        return source(Paths.get(sourceFile));
    }
    
    public SshSftpGet source(Path sourceFile) {
        this.source = sourceFile;
        return this;
    }
    
    public SshSftpGet target(String targetFile) {
        return target(Paths.get(targetFile));
    }
    
    public SshSftpGet target(Path targetFile) {
        return target(Streamables.output(targetFile));
    }
    
    public SshSftpGet target(File targetFile) {
        return target(Streamables.output(targetFile));
    }
    
    public SshSftpGet target(OutputStream target) {
        return target(Streamables.output(target));
    }
    
    public SshSftpGet target(StreamableOutput target) {
        this.target = target;
        return this;
    }

    @Override
    protected Result doRun() throws BlazeException {
        ObjectHelper.requireNonNull(source, "source cannot be null");
        ObjectHelper.requireNonNull(target, "target cannot be null");
        sftp.get(source, target);
        return new Result(this, null);
    }
    
    static public class Result extends com.fizzed.blaze.core.Result<SshSftpGet,Void,Result> {
        
        Result(SshSftpGet action, Void value) {
            super(action, value);
        }
        
    }
    
}

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

import com.fizzed.blaze.ssh.impl.SshSupport;
import java.nio.file.Path;
import java.util.List;

public abstract class SshSftpSession implements SshSupport {

    abstract public SshSession session();
    
    abstract public Path pwd() throws SshException;
    
    abstract public void cd(String path) throws SshException;
    
    abstract public void cd(Path path) throws SshException;
    
    // like Files.readAttributes(file, BasicFileAttributes.class);
    abstract public SshFileAttributes lstat(String path) throws SshSftpException;
    
    abstract public SshFileAttributes lstat(Path path) throws SshSftpException;
    
    public SshFileAttributes lstatSafely(String path) throws SshSftpException {
        try {
            return lstat(path);
        } catch (SshSftpNoSuchFileException e) {
            return null;
        }
    }
    
    public SshFileAttributes lstatSafely(Path path) throws SshSftpException {
        try {
            return lstat(path);
        } catch (SshSftpNoSuchFileException e) {
            return null;
        }
    }

    // like Files.listFiles
    abstract public List<SshFile> ls(String path) throws SshException;
    
    abstract public List<SshFile> ls(Path path) throws SshException;

    // builder-syntax for puts is better
    abstract public SshSftpGet get() throws SshException;
    
    // builder-syntax for puts is better
    abstract public SshSftpPut put() throws SshException;
    
    abstract public void chgrp(String path, int gid);
    
    abstract public void chgrp(Path path, int gid);
    
    abstract public void chown(String path, int uid);
    
    abstract public void chown(Path path, int uid);
    
    abstract public void chown(String path, int uid, int gid);
    
    abstract public void chown(Path path, int uid, int gid);
    
    abstract public void mkdir(String path);
    
    abstract public void mkdir(Path path);
    
    abstract public void rm(String path);
    
    abstract public void rm(Path path);
    
    abstract public void rmdir(String path);
    
    abstract public void rmdir(Path path);
    
    abstract public void mv(String source, String target);
    
    abstract public void mv(Path source, Path target);
    
    abstract public void symlink(String target, String link);
    
    abstract public void symlink(Path target, Path link);
    
}

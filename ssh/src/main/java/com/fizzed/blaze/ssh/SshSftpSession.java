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
import com.jcraft.jsch.SftpException;
import java.nio.file.Path;
import java.util.List;

/**
 *
 * @author joelauer
 */
public interface SshSftpSession extends SshSupport {

    SshSession session();
    
    Path pwd() throws SshException;
    
    void cd(String path) throws SshException;
    
    void cd(Path path) throws SshException;
    
    // like Files.readAttributes(file, BasicFileAttributes.class);
    SshFileAttributes lstat(String path) throws SshException;
    
    SshFileAttributes lstat(Path path) throws SshException;

    // like Files.listFiles
    List<SshFile> ls(String path) throws SshException;
    
    List<SshFile> ls(Path path) throws SshException;

    // builder-syntax for puts is better
    SshSftpGet get() throws SshException;
    
    // builder-syntax for puts is better
    SshSftpPut put() throws SshException;
    
    void chgrp(String path, int gid);
    
    void chgrp(Path path, int gid);
    
    void chown(String path, int uid);
    
    void chown(Path path, int uid);
    
    void chown(String path, int uid, int gid);
    
    void chown(Path path, int uid, int gid);
    
    void mkdir(String path);
    
    void mkdir(Path path);
    
    void rm(String path);
    
    void rm(Path path);
    
    void rmdir(String path);
    
    void rmdir(Path path);
    
    void mv(String source, String target);
    
    void mv(Path source, Path target);
    
    void symlink(String target, String link);
    
    void symlink(Path target, Path link);
    
}

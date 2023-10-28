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
package com.fizzed.blaze.system;

import com.fizzed.blaze.Context;
import com.fizzed.blaze.core.*;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;

import com.fizzed.blaze.util.VerboseLogger;

import static com.fizzed.blaze.internal.FileHelper.isNotEmptyDir;

/**
 * rm - remove files or directories
 * 
 * @author joelauer
 */
public class Remove extends Action<Remove.Result,Void> implements PathsMixin<Remove>, VerbosityMixin<Remove> {

    private final VerboseLogger log;
    final private List<Path> paths;
    private boolean force;
    private boolean recursive;
    
    public Remove(Context context) {
        super(context);
        this.log = new VerboseLogger(this);
        this.paths = new ArrayList<>();
        this.force = false;
        this.recursive = false;
    }

    @Override
    public VerboseLogger getVerboseLogger() {
        return this.log;
    }

    @Override
    public List<Path> getPaths() {
        return this.paths;
    }
    
    public Remove force() {
        this.force = true;
        return this;
    }
    
    public Remove force(boolean force) {
        this.force = force;
        return this;
    }
    
    public Remove recursive() {
        this.recursive = true;
        return this;
    }

    public Remove recursive(boolean recursive) {
        this.recursive = recursive;
        return this;
    }

    @Override
    protected Result doRun() throws BlazeException {
        try {
            for (Path path : paths) {
                log.verbose("Deleting {}", path);

                if (!Files.exists(path)) {
                    if (!force) {
                        throw new FileNotFoundException("File " + path + " not found (and force is disabled)");
                    }
                    continue;
                }

                if (!recursive) {
                    if (Files.isDirectory(path) && isNotEmptyDir(path)) {
                        throw new DirectoryNotEmptyException("Directory " + path + " is not empty (and recursive is disabled)");
                    }

                    Files.delete(path);
                } else {
                    Files.walkFileTree(path, new SimpleFileVisitor<Path>() {
                        @Override
                        public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                            log.debug(" rm {}", file);
                            Files.delete(file);
                            return FileVisitResult.CONTINUE;
                        }

                        @Override
                        public FileVisitResult postVisitDirectory(Path dir, IOException e) throws IOException {
                            if (e == null) {
                                log.debug(" rmdir {}", dir);
                                Files.delete(dir);
                                return FileVisitResult.CONTINUE;
                            } else {
                                // directory iteration failed
                                throw e;
                            }
                        }
                    });
                }
            }
        } catch (IOException e) {
            throw new BlazeException("Unable to remove", e);
        }
        
        return new Result(this, null);
    }

    static public class Result extends com.fizzed.blaze.core.Result<Remove,Void,Result> {
        
        Result(Remove action, Void value) {
            super(action, value);
        }
        
    }
    
}

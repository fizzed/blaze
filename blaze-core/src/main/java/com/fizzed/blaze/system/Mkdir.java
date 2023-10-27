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
import com.fizzed.blaze.core.Action;
import com.fizzed.blaze.core.BlazeException;
import com.fizzed.blaze.util.ObjectHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Mkdir extends Action<Mkdir.Result,Void> {
    protected final Logger log = LoggerFactory.getLogger(this.getClass());

    static public class Result extends com.fizzed.blaze.core.Result<Mkdir,Void,Result> {

        Result(Mkdir action, Void value) {
            super(action, value);
        }

    }

    private Path target;
    private boolean parents;

    public Mkdir(Context context) {
        super(context);
        this.parents = false;
    }

    public Mkdir target(String path) {
        ObjectHelper.requireNonNull(path, "path cannot be null");
        return this.target(Paths.get(path));
    }

    public Mkdir target(File path) {
        ObjectHelper.requireNonNull(path, "path cannot be null");
        return this.target(path.toPath());
    }

    public Mkdir target(Path path) {
        ObjectHelper.requireNonNull(path, "path cannot be null");
        this.target = path;
        return this;
    }

    public Mkdir parents() {
        this.parents = true;
        return this;
    }

    public Mkdir parents(boolean recursive) {
        this.parents = recursive;
        return this;
    }

    @Override
    protected Mkdir.Result doRun() throws BlazeException {
        if (this.target == null) {
            throw new BlazeException("Mkdir requires a target");
        }

        try {
            if (this.parents) {
                Files.createDirectories(this.target);
            } else {
                Files.createDirectory(this.target);
            }
        } catch (IOException e) {
            throw new BlazeException("Unable to mkdir", e);
        }

        return new Mkdir.Result(this, null);
    }

    /*private void copyDirectory(Path sourceDir, Path targetDir) throws IOException {
        Files.walkFileTree(sourceDir, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                Path resolve = targetDir.resolve(sourceDir.relativize(dir));
                if (Files.notExists(resolve)) {
                    log.info("Creating directory {}", resolve);
                    Files.createDirectories(resolve);
                }
                return FileVisitResult.CONTINUE;

            }

            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                Path resolve = targetDir.resolve(sourceDir.relativize(file));
                log.info("Copying {} -> {}", file, resolve);
                Files.copy(file, resolve, StandardCopyOption.REPLACE_EXISTING);
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFileFailed(Path file, IOException exc) {
                System.err.format("Unable to copy: %s: %s%n", file, exc);
                return FileVisitResult.TERMINATE;
            }
        });
    }*/

}
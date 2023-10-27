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
import com.fizzed.blaze.core.ExecMixin;
import com.fizzed.blaze.core.PathsMixin;
import com.fizzed.blaze.util.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;
import java.util.concurrent.TimeUnit;

abstract public class Copy extends Action<Copy.Result,Void> {
    protected final Logger log = LoggerFactory.getLogger(this.getClass());

    static public class Result extends com.fizzed.blaze.core.Result<Copy,Void,Result> {

        Result(Copy action, Void value) {
            super(action, value);
        }

    }

    private List<Path> sources;
    private Path destination;
    private boolean force;
    private boolean recursive;

    public Copy(Context context) {
        super(context);
        this.sources = new ArrayList<>();
        this.force = false;
        this.recursive = false;
    }

    public Copy source(String path) {
        ObjectHelper.requireNonNull(path, "path cannot be null");
        return source(Paths.get(path));
    }

    public Copy source(File path) {
        ObjectHelper.requireNonNull(path, "path cannot be null");
        return source(path.toPath());
    }

    public Copy source(Path path) {
        ObjectHelper.requireNonNull(path, "path cannot be null");
        this.sources.add(path);
        return this;
    }

    public Copy destination(String path) {
        ObjectHelper.requireNonNull(path, "path cannot be null");
        return destination(Paths.get(path));
    }

    public Copy destination(File path) {
        ObjectHelper.requireNonNull(path, "path cannot be null");
        return destination(path.toPath());
    }

    public Copy destination(Path path) {
        ObjectHelper.requireNonNull(path, "path cannot be null");
        this.destination = path;
        return this;
    }

    public Copy force() {
        this.force = true;
        return this;
    }

    public Copy force(boolean force) {
        this.force = force;
        return this;
    }

    public Copy recursive() {
        this.recursive = true;
        return this;
    }

    public Copy recursive(boolean recursive) {
        this.recursive = recursive;
        return this;
    }

    @Override
    protected Copy.Result doRun() throws BlazeException {
        if (this.sources.isEmpty()) {
            throw new BlazeException("Copy requires at least 1 source path");
        }

        if (this.destination == null) {
            throw new BlazeException("Copy requires a destination");
        }

        // the sources must all exist
        for (Path source : this.sources) {
            if (!Files.exists(source)) {
                throw new BlazeException("Copy source " + source + " does not exist");
            }
        }

        try {
            // does the destination exist?
            if (Files.exists(this.destination)) {
                // if its a file, we could have issues
                if (!Files.isDirectory(this.destination) && !this.force) {
                    throw new BlazeException("Copy destination " + this.destination + " already exists");
                }
            } else {
                // destination does not exist, what to do?
                if (this.sources.size() > 1) {
                    // the destination MUST be a directory
                    Files.createDirectories(this.destination);
                } else {
                    // single to single, if the source is a directory, the target must be too
                    if (Files.isDirectory(this.sources.get(0))) {
                        Files.createDirectories(this.destination);
                    }
                }
            }

            // single -> single
            if (this.sources.size() == 1) {
                final Path source = this.sources.get(0);

                if (!Files.isDirectory(source)) {
                    // source is a file
                    if (Files.isDirectory(this.destination)) {
                        Path target = this.destination.resolve(source.getFileName());
                        log.info("Copying {} -> {}", source, target);
                        Files.copy(source, target, StandardCopyOption.REPLACE_EXISTING);
                    } else {
                        log.info("Copying {} -> {}", source, this.destination);
                        Files.copy(source, this.destination, StandardCopyOption.REPLACE_EXISTING);
                    }
                } else {
                    // source is a directory
                    if (Files.isDirectory(this.destination)) {
                        copyDirectory(source, this.destination);
                    } else {
                        throw new BlazeException("Cannot copy source directory " + source + " a file " + this.destination);
                    }
                }
            }


        } catch (IOException e) {
            throw new BlazeException("Unable to copy", e);
        }

        return new Copy.Result(this, null);
    }

    /*private void copyDirectory(Path sourceDir, Path destinationDir) throws IOException {
        Files.walk(sourceDir)
            .forEach(sourcePath -> {
                try {
                    Path targetPath = destinationDir.resolve(sourceDir.relativize(sourcePath));
                    log.info("Copying {} -> {}", sourcePath, targetPath);
                    Files.copy(sourcePath, targetPath, StandardCopyOption.REPLACE_EXISTING);
                } catch (IOException e) {
                    throw new UncheckedIOException("Unable to copy", e);
                }
            });
    }*/

    private void copyDirectory(Path sourceDir, Path destinationDir) throws IOException {
        CopyFileVisitor fileVisitor = new CopyFileVisitor(sourceDir, destinationDir);
        Files.walkFileTree(sourceDir, fileVisitor);

    }

    private class CopyFileVisitor extends SimpleFileVisitor<Path> {

        private final Path source;
        private final Path target;

        public CopyFileVisitor(Path source, Path target) {
            this.source = source;
            this.target = target;
        }

        @Override
        public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
            Path resolve = target.resolve(source.relativize(dir));
            if (Files.notExists(resolve)) {
                log.info("Creating directory {}", resolve);
                Files.createDirectories(resolve);
            }
            return FileVisitResult.CONTINUE;

        }

        @Override
        public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
            Path resolve = target.resolve(source.relativize(file));
            log.info("Copying {} -> {}", file, resolve);
            Files.copy(file, resolve, StandardCopyOption.REPLACE_EXISTING);
            return FileVisitResult.CONTINUE;
        }

        @Override
        public FileVisitResult visitFileFailed(Path file, IOException exc) {
            System.err.format("Unable to copy: %s: %s%n", file, exc);
            return FileVisitResult.TERMINATE;
        }

    }

}
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
import com.fizzed.blaze.core.DirectoryNotEmptyException;
import com.fizzed.blaze.core.FileNotFoundException;
import com.fizzed.blaze.core.Verbosity;
import com.fizzed.blaze.core.VerbosityMixin;
import com.fizzed.blaze.util.*;
import com.fizzed.blaze.util.Timer;

import java.io.*;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;
import java.util.stream.Stream;

import static com.fizzed.blaze.internal.FileHelper.isNotEmptyDir;

public class Copy extends Action<Copy.Result,Void> implements VerbosityMixin<Copy> {

    static public class Result extends com.fizzed.blaze.core.Result<Copy,Void,Result> {

        private int dirsCreated;
        private int filesCopied;
        private int filesOverwritten;

        Result(Copy action, Void value) {
            super(action, value);
        }

        public int getDirsCreated() {
            return dirsCreated;
        }

        public int getFilesCopied() {
            return filesCopied;
        }

        public int getFilesOverwritten() {
            return filesOverwritten;
        }

    }

    private final VerboseLogger log;
    private final List<Path> sources;
    private Path target;
    private boolean force;
    private boolean recursive;
    private Verbosity verbosity;

    public Copy(Context context) {
        super(context);
        this.log = new VerboseLogger(this);
        this.sources = new ArrayList<>();
        this.force = false;
        this.recursive = false;
    }

    public VerboseLogger getVerboseLogger() {
        return this.log;
    }

    public Copy source(String path) {
        ObjectHelper.requireNonNull(path, "path cannot be null");
        return this.source(Paths.get(path));
    }

    public Copy source(File path) {
        ObjectHelper.requireNonNull(path, "path cannot be null");
        return this.source(path.toPath());
    }

    public Copy source(Path path) {
        ObjectHelper.requireNonNull(path, "path cannot be null");
        this.sources.clear();
        this.sources.add(path);
        return this;
    }

    public Copy sources(Path... paths) {
        ObjectHelper.requireNonNull(paths, "paths cannot be null");
        this.sources.clear();
        for (Path p : paths) {
            this.sources.add(p);
        }
        return this;
    }

    public Copy sources(Globber globber) {
        ObjectHelper.requireNonNull(globber, "globber cannot be null");
        try {
            List<Path> paths = globber.scan();
            this.sources.clear();
            this.sources.addAll(paths);
            return this;
        } catch (IOException e) {
            throw new BlazeException(e.getMessage(), e);
        }
    }

    public Copy sources(File... files) {
        ObjectHelper.requireNonNull(files, "files cannot be null");
        this.sources.clear();
        for (File f : files) {
            this.sources.add(f.toPath());
        }
        return this;
    }

    public Copy sources(String... paths) {
        ObjectHelper.requireNonNull(paths, "paths cannot be null");
        this.sources.clear();
        for (String p : paths) {
            this.sources.add(Paths.get(p));
        }
        return this;
    }

    public Copy target(String path) {
        ObjectHelper.requireNonNull(path, "path cannot be null");
        return this.target(Paths.get(path));
    }

    public Copy target(File path) {
        ObjectHelper.requireNonNull(path, "path cannot be null");
        return this.target(path.toPath());
    }

    public Copy target(Path path) {
        ObjectHelper.requireNonNull(path, "path cannot be null");
        this.target = path;
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
        if (this.sources.isEmpty() && !this.force) {
            throw new BlazeException("Copy requires at least 1 source path (and force is disabled)");
        }

        if (this.target == null) {
            throw new BlazeException("Copy requires a target");
        }

        // the sources must all exist (we should check this first before we do anything)
        for (Path source : this.sources) {
            if (!Files.exists(source)) {
                throw new FileNotFoundException("Copy source file " + source + " not found");
            }
        }

        final Result result = new Result(this, null);
        final Timer timer = new Timer();

        try {
            for (Path source : this.sources) {
                log.verbose("Copying {} -> {}", source, this.target);

                if (Files.isDirectory(source)) {
                    // if the source is a directory, does it have stuff in it?
                    if (isNotEmptyDir(source) && !this.recursive) {
                        throw new DirectoryNotEmptyException("Copy source directory " + source + " is not empty (and recursive is disabled)");
                    }

                    // source is a directory
                    if (Files.exists(this.target)) {
                        // is it the same dir?
                        if (Files.isSameFile(source, this.target)) {
                            throw new BlazeException("Copy source " + source + " and target " + this.target + " are the same directory");
                        }
                        // target exists, which may or may not be okay
                        if (Files.isDirectory(this.target)) {
                            // target exists, but is a directory, we can simply copy the source dir to it
                            Path relativeTarget = this.target.resolve(source.getFileName());
                            copyDirectory(source, relativeTarget, result);
                        } else {
                            // target exists, but is a file!
                            throw new BlazeException("Cannot copy source directory " + source + " to an existing file " + this.target);
                        }
                    } else {
                        // build a new relative target we will perform the copy to
                        log.debug(" mkdir {}", this.target);
                        copyDirectory(source, this.target, result);
                    }
                } else {
                    // source is a file
                    if (Files.isDirectory(this.target)) {
                        // target is a directory
                        Path t = this.target.resolve(source.getFileName());

                        if (Files.exists(t)) {
                            // is it the same file?
                            if (Files.isSameFile(source, t)) {
                                throw new BlazeException("Copy source " + source + " and target " + t + " are the same file");
                            }
                            if (!force) {
                                throw new BlazeException("Copy target " + t + " already exists (and force is disabled)");
                            }
                            log.debug(" overwrite {} -> {}", source, t);
                            Files.copy(source, t, StandardCopyOption.REPLACE_EXISTING);
                            result.filesOverwritten++;
                        } else {
                            log.debug(" copy {} -> {}", source, t);
                            Files.copy(source, t, StandardCopyOption.REPLACE_EXISTING);
                            result.filesCopied++;
                        }
                    } else {
                        // target is a file
                        if (Files.exists(this.target)) {
                            // is it the same file?
                            if (source.toAbsolutePath().equals(this.target.toAbsolutePath())) {
                                throw new BlazeException("Copy source " + source + " and target " + this.target + " are the same file");
                            }
                            // we know its not the same file, but is it forced?
                            if (!this.force) {
                                throw new BlazeException("Copy target " + this.target + " already exists (and force is disabled)");
                            }
                            log.debug(" overwrite {} -> {}", source, this.target);
                            Files.copy(source, this.target, StandardCopyOption.REPLACE_EXISTING);
                            result.filesOverwritten++;
                        } else {
                            // already logged this at top of loop
                            //log.debug("Copying {} -> {}", source, this.target);
                            Files.copy(source, this.target, StandardCopyOption.REPLACE_EXISTING);
                            result.filesCopied++;
                        }
                    }
                }
            }
        } catch (IOException e) {
            throw new BlazeException("Unable to copy", e);
        }

        log.debug("Copied {} files, overwrote {} files, created {} dirs (in {})", result.filesCopied, result.filesOverwritten, result.dirsCreated, timer);

        return new Copy.Result(this, null);
    }

    private void copyDirectory(Path sourceDir, Path targetDir, Result result) throws IOException {
        Files.walkFileTree(sourceDir, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
//                log.trace("preVisitDirectory: dir={}", dir);
                Path relativeDir = sourceDir.relativize(dir);
//                log.trace("preVisitDirectory: relativeDir={}", relativeDir);
//                Path resolved = targetDir.resolve(relativeDir).resolve(dir.getFileName());
                Path resolved = targetDir.resolve(relativeDir);
//                log.trace("preVisitDirectory: resolved={}", resolved);
                if (Files.exists(resolved)) {
                    if (!Copy.this.force) {
                        throw new BlazeException("Copy target " + resolved + " already exists (and force is disabled)");
                    }
                } else {
                    log.debug(" mkdir {}", resolved);
                    Files.createDirectories(resolved);
                    result.dirsCreated++;
                }

                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
//                log.trace("visitFile: file={}", file);
                Path relativeFile = sourceDir.relativize(file);
//                log.trace("visitFile: relativeFile={}", relativeFile);
                Path resolved = targetDir.resolve(relativeFile);
//                log.trace("visitFile: resolved={}", resolved);
                if (Files.exists(resolved)) {
                    if (!Copy.this.force) {
                        throw new BlazeException("Copy target " + resolved + " already exists (and force is disabled)");
                    }
                    log.debug(" overwrite {} -> {}", file, resolved);
                    Files.copy(file, resolved, StandardCopyOption.REPLACE_EXISTING);
                    result.filesOverwritten++;
                } else {
                    log.debug(" copy {} -> {}", file, resolved);
                    Files.copy(file, resolved, StandardCopyOption.REPLACE_EXISTING);
                    result.filesCopied++;
                }

                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFileFailed(Path file, IOException e) {
                log.error("Failed while copying directory", e);
                return FileVisitResult.TERMINATE;
            }
        });
    }
}
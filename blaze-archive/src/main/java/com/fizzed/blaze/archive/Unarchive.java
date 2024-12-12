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
package com.fizzed.blaze.archive;

import com.fizzed.blaze.Context;
import com.fizzed.blaze.core.*;
import com.fizzed.blaze.util.ObjectHelper;
import com.fizzed.blaze.util.Timer;
import com.fizzed.blaze.util.VerboseLogger;
import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.ArchiveException;
import org.apache.commons.compress.archivers.ArchiveInputStream;
import org.apache.commons.compress.archivers.ArchiveStreamFactory;
import org.apache.commons.compress.compressors.CompressorException;
import org.apache.commons.compress.compressors.CompressorStreamFactory;
import org.apache.commons.compress.compressors.FileNameUtil;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.*;

public class Unarchive extends Action<Unarchive.Result,Void> implements VerbosityMixin<Unarchive> {

    static public class Result extends com.fizzed.blaze.core.Result<Unarchive,Void,Result> {

        private int dirsCreated;
        private int filesCopied;
        private int filesOverwritten;

        Result(Unarchive action, Void value) {
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
    private final Path source;
    private Path target;
    //private boolean force;
    private Verbosity verbosity;

    public Unarchive(Context context, Path source) {
        super(context);
        this.log = new VerboseLogger(this);
        this.source = source;
        //this.force = false;
    }

    public VerboseLogger getVerboseLogger() {
        return this.log;
    }

    public Unarchive target(String path) {
        ObjectHelper.requireNonNull(path, "path cannot be null");
        return this.target(Paths.get(path));
    }

    public Unarchive target(File path) {
        ObjectHelper.requireNonNull(path, "path cannot be null");
        return this.target(path.toPath());
    }

    public Unarchive target(Path path) {
        ObjectHelper.requireNonNull(path, "path cannot be null");
        this.target = path;
        return this;
    }

    /*public Unarchive force() {
        this.force = true;
        return this;
    }

    public Unarchive force(boolean force) {
        this.force = force;
        return this;
    }*/

    @Override
    protected Unarchive.Result doRun() throws BlazeException {
        /*if (this.sources.isEmpty() && !this.force) {
            throw new BlazeException("Copy requires at least 1 source path (and force is disabled)");
        }*/

        // the source must all exist (we should check this first before we do anything)
        if (!Files.exists(this.source)) {
            throw new FileNotFoundException("Source file " + source + " not found");
        }

        // target is current directory by default, or the provided target
        final Path destDir = this.target != null ? this.target : Paths.get(".");

        if (!Files.exists(destDir)) {
            log.debug("Creating target directory: {}", destDir);
            try {
                Files.createDirectories(destDir);
            } catch (IOException e) {
                throw new BlazeException("Unable to create target directory", e);
            }
        }


        final Result result = new Result(this, null);
        final Timer timer = new Timer();

        final ArchiveFormat archiveFormat = ArchiveFormats.detectByFileName(this.source.getFileName().toString());

        if (archiveFormat == null) {
            throw new BlazeException("Unable to detect archive format (or its unsupported)");
        }

        log.debug("Unarchiving {} -> {}", this.source, this.target);
        log.debug("Detected archive format: archiveMethod={}, compressMethod={}", archiveFormat.getArchiveMethod(), archiveFormat.getCompressMethod());

        try (InputStream fin = Files.newInputStream(this.source)) {
            // we need it buffered so we can auto-detect the format with mark/reset
            try (InputStream bin = new BufferedInputStream(fin)) {
                // is this file compressed?
                InputStream uncompressedIn = bin;
                if (archiveFormat.getCompressMethod() != null) {
                    try {
                        uncompressedIn = CompressorStreamFactory.getSingleton().createCompressorInputStream(archiveFormat.getCompressMethod(), bin);
                    } catch (CompressorException e) {
                        throw new BlazeException("Unable to uncompress source", e);
                    }
                }

                try {
                    // is this file archived?
                    if (archiveFormat.getArchiveMethod() != null) {
                        try {
                            ArchiveInputStream<? extends ArchiveEntry> ais = ArchiveStreamFactory.DEFAULT.createArchiveInputStream(archiveFormat.getArchiveMethod(), uncompressedIn);

                            ArchiveEntry entry = ais.getNextEntry();
                            while (entry != null) {
                                final Path file = entry.resolveIn(destDir);

                                if (entry.isDirectory()) {
                                    Files.createDirectories(file);
                                } else {
                                    log.debug(entry.getName());
                                    Files.createDirectories(file.getParent());
                                    Files.copy(ais, file, StandardCopyOption.REPLACE_EXISTING);
                                }

                                entry = ais.getNextEntry();
                            }
                        } catch (ArchiveException e) {
                            throw new BlazeException("Unable to unarchive source", e);
                        }
                    }
                } finally {
                    if (uncompressedIn != null) {
                        uncompressedIn.close();
                    }
                }
            }



            /*ArchiveInputStream ain = new ArchiveStreamFactory().createArchiveInputStream(ArchiveStreamFactory.ZIP, is);
            ZipArchiveEntry entry = (ZipArchiveEntry) in.getNextEntry();
            OutputStream out = Files.newOutputStream(dir.toPath().resolve(entry.getName()));
            IOUtils.copy(in, out);
            out.close();
            in.close();final InputStream is = Files.newInputStream(input.toPath());
            ArchiveInputStream in = new ArchiveStreamFactory().createArchiveInputStream(ArchiveStreamFactory.ZIP, is);
            ZipArchiveEntry entry = (ZipArchiveEntry) in.getNextEntry();
            OutputStream out = Files.newOutputStream(dir.toPath().resolve(entry.getName()));
            IOUtils.copy(in, out);
            out.close();
            in.close();*/
        } catch (IOException e) {
            throw new BlazeException("Unable to copy", e);
        }

        //log.debug("Copied {} files, overwrote {} files, created {} dirs (in {})", result.filesCopied, result.filesOverwritten, result.dirsCreated, timer);

        return new Unarchive.Result(this, null);
    }

}
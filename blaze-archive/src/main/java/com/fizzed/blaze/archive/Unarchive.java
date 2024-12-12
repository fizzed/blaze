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
import org.apache.commons.compress.archivers.sevenz.SevenZArchiveEntry;
import org.apache.commons.compress.archivers.sevenz.SevenZFile;
import org.apache.commons.compress.compressors.CompressorException;
import org.apache.commons.compress.compressors.CompressorStreamFactory;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.*;
import java.util.function.Predicate;

public class Unarchive extends Action<Unarchive.Result,Void> implements VerbosityMixin<Unarchive> {

    static public class Result extends com.fizzed.blaze.core.Result<Unarchive,Void,Result> {

        private int fileCount;

        Result(Unarchive action, Void value) {
            super(action, value);
        }

        public int getFileCount() {
            return fileCount;
        }

    }

    private final VerboseLogger log;
    private final Path source;
    private Path target;
    private Boolean stripLeadingPath;
    //private Predicate<String> filter;
    //private boolean force;
    private Verbosity verbosity;

    public Unarchive(Context context, Path source) {
        super(context);
        this.log = new VerboseLogger(this);
        this.source = source;
        this.stripLeadingPath = false;
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

    public Unarchive stripLeadingPath() {
        this.stripLeadingPath = true;
        return this;
    }

    public Unarchive stripLeadingPath(Boolean stripLeadingPath) {
        this.stripLeadingPath = stripLeadingPath;
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

        // special handling for 7z
        if ("7z".equals(archiveFormat.getArchiveMethod())) {
            this.unarchive7z(this.source, destDir);
        } else {
            // unarchiving via streaming
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
                            this.unarchiveStreaming(archiveFormat.getArchiveMethod(), uncompressedIn, destDir);
                        }
                    } finally {
                        if (uncompressedIn != null) {
                            uncompressedIn.close();
                        }
                    }
                }
            } catch (IOException e) {
                throw new BlazeException("Unable to copy", e);
            }
        }

        //log.debug("Copied {} files, overwrote {} files, created {} dirs (in {})", result.filesCopied, result.filesOverwritten, result.dirsCreated, timer);

        return new Unarchive.Result(this, null);
    }

    private void unarchiveStreaming(String archiveMethod, InputStream in, Path destDir) throws BlazeException {
        try {
            ArchiveInputStream<? extends ArchiveEntry> ais = ArchiveStreamFactory.DEFAULT.createArchiveInputStream(archiveMethod, in);

            ArchiveEntry entry = ais.getNextEntry();
            while (entry != null) {
                this.extractEntry(entry, ais, destDir);
                entry = ais.getNextEntry();
            }
        } catch (IOException | ArchiveException e) {
            throw new BlazeException("Failed to unarchive: " + e.getMessage(), e);
        }
    }

    private void unarchive7z(Path source, Path destDir) throws BlazeException {
        try (SevenZFile sevenZFile = new SevenZFile(source.toFile())) {
            SevenZArchiveEntry entry = sevenZFile.getNextEntry();
            while (entry != null) {
                try (InputStream in = sevenZFile.getInputStream(entry)) {
                    this.extractEntry(entry, in, destDir);
                }

                entry = sevenZFile.getNextEntry();
            }
        } catch (IOException e) {
            throw new BlazeException("Failed to unarchive: " + e.getMessage(), e);
        }
    }

    private void extractEntry(ArchiveEntry entry, InputStream in, Path destDir) throws BlazeException {
        try {
            if (!entry.isDirectory()) {
                String name = entry.getName();

                if (this.stripLeadingPath != null && this.stripLeadingPath) {
                    int slashPos = name.indexOf('/');
                    if (slashPos > 0) {
                        name = name.substring(slashPos+1);
                    }
                    log.debug("{} (stripped leading)", name);
                } else {
                    log.debug("{}", name);
                }

                Path file = destDir.resolve(name).normalize();
                Files.createDirectories(file.getParent());
                Files.copy(in, file, StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (IOException e) {
            throw new BlazeException("Failed to unarchive: " + e.getMessage(), e);
        }
    }

}
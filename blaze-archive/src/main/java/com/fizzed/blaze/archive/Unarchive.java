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
import java.util.function.Function;
import java.util.function.Predicate;

import static java.util.Optional.ofNullable;

public class Unarchive extends Action<Unarchive.Result,Void> implements VerbosityMixin<Unarchive> {

    static public class Result extends com.fizzed.blaze.core.Result<Unarchive,Void,Result> {

        private int filesCreated;
        private int filesOverwritten;

        Result(Unarchive action, Void value) {
            super(action, value);
        }

        public int getFilesCreated() {
            return filesCreated;
        }

        public Result setFilesCreated(int filesCreated) {
            this.filesCreated = filesCreated;
            return this;
        }

        public int getFilesOverwritten() {
            return filesOverwritten;
        }

        public Result setFilesOverwritten(int filesOverwritten) {
            this.filesOverwritten = filesOverwritten;
            return this;
        }

    }

    private final VerboseLogger log;
    private final Path source;
    private Path target;
    private int stripComponents;
    private boolean force;
    private Function<String,String> renamer;
    private Predicate<String> filter;

    public Unarchive(Context context, Path source) {
        super(context);
        this.log = new VerboseLogger(this);
        this.source = source;
        this.stripComponents = 0;
        this.force = false;
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
        this.stripComponents = 1;
        return this;
    }

    public Unarchive stripComponents(int stripComponents) {
        this.stripComponents = stripComponents;
        return this;
    }

    public Unarchive force() {
        this.force = true;
        return this;
    }

    public Unarchive force(boolean force) {
        this.force = force;
        return this;
    }

    public Unarchive renamer(Function<String, String> renamer) {
        this.renamer = renamer;
        return this;
    }

    public Unarchive filter(Predicate<String> filter) {
        this.filter = filter;
        return this;
    }

    @Override
    protected Unarchive.Result doRun() throws BlazeException {
        // the source must all exist (we should check this first before we do anything)
        if (!Files.exists(this.source)) {
            throw new FileNotFoundException("Source file " + source + " not found");
        }

        // target is current directory by default, or the provided target
        final Path destDir = this.target != null ? this.target : Paths.get(".");

        log.info("Unarchiving {} ", this.source);
        log.info(" -> {}", destDir);

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

        final ArchiveInfo archiveInfo = ArchiveHelper.archiveInfo(this.source);

        if (archiveInfo == null) {
            throw new BlazeException("Unable to detect archive format from file extension (e.g. .tar.gz) or the format is not yet unsupported");
        }

        log.verbose("Using archiver={}, compressor={}",
            ofNullable(archiveInfo.getArchiver()).map(v -> v.name()).orElse("none"),
            ofNullable(archiveInfo.getCompressor()).map(v -> v.name()).orElse("none"));

        // special handling for 7z
        if (archiveInfo.getArchiver() == Archiver.SEVENZ) {
            this.unarchive7z(this.source, destDir, result);
        } else {
            try (InputStream fin = Files.newInputStream(this.source)) {
                try (InputStream bin = new BufferedInputStream(fin)) {
                    // is this file compressed?
                    InputStream uncompressedIn = bin;
                    if (archiveInfo.getCompressor() != null) {
                        uncompressedIn = this.openUncompressedStream(archiveInfo.getCompressor(), bin);
                    }

                    try {
                        // is this file archived?
                        if (archiveInfo.getArchiver() != null) {
                            this.unarchiveCommonsArchiveStream(archiveInfo.getArchiver(), uncompressedIn, destDir, result);
                        } else {
                            // file is compressed only, we'll treat it as a single entry within an archive
                            this.extractEntry(archiveInfo.getUnarchivedName(), uncompressedIn, destDir, result);
                        }
                    } finally {
                        if (uncompressedIn != null) {
                            uncompressedIn.close();
                        }
                    }
                }
            } catch (IOException e) {
                throw new BlazeException("Failed to unarchive", e);
            }
        }

        log.info("Unarchived {} files, overwrote {} files (in {})", result.filesCreated, result.filesOverwritten, timer);

        return new Unarchive.Result(this, null);
    }

    private void unarchiveCommonsArchiveStream(Archiver archiver, InputStream in, Path destDir, Result result) throws BlazeException {
        try {
            final String archiverName = ArchiveHelper.getCommonsArchiverName(archiver);

            ArchiveInputStream<? extends ArchiveEntry> ais = ArchiveStreamFactory.DEFAULT.createArchiveInputStream(archiverName, in);

            ArchiveEntry entry = ais.getNextEntry();
            while (entry != null) {
                if (!entry.isDirectory()) {
                    this.extractEntry(entry.getName(), ais, destDir, result);
                }
                entry = ais.getNextEntry();
            }
        } catch (IOException | ArchiveException e) {
            throw new BlazeException("Failed to unarchive: " + e.getMessage(), e);
        }
    }

    private void unarchive7z(Path source, Path destDir, Result result) throws BlazeException {
        try (SevenZFile sevenZFile = new SevenZFile(source.toFile())) {
            SevenZArchiveEntry entry = sevenZFile.getNextEntry();
            while (entry != null) {
                if (!entry.isDirectory()) {
                    try (InputStream in = sevenZFile.getInputStream(entry)) {
                        this.extractEntry(entry.getName(), in, destDir, result);
                    }
                }

                entry = sevenZFile.getNextEntry();
            }
        } catch (IOException e) {
            throw new BlazeException("Failed to unarchive: " + e.getMessage(), e);
        }
    }

    private void extractEntry(String entryName, InputStream in, Path destDir, Result result) throws BlazeException {
        try {
            String name = entryName;
            String strippedPath = null;

            if (this.stripComponents > 0) {
                final String[] stripResult = ArchiveHelper.stripComponents(name, this.stripComponents);
                name = stripResult[0];
                strippedPath = stripResult[1];
            }

            // any filtering?
            if (this.filter != null) {
                if (!this.filter.test(name)) {
                    log.verbose("Filtered: {}   (skipped)", name);
                    return; // do not extract
                }
            }

            // any renaming?
            String origName = null;
            if (this.renamer != null) {
                String newName = this.renamer.apply(name);
                if (newName != null && !newName.equals(name)) {
                    origName = name;
                    name = newName;
                }
            }

            // build options string for logging
            StringBuilder optionsStr = new StringBuilder();
            if (origName != null) {
                optionsStr.append("renamed: ");
                optionsStr.append(origName);
            }
            if (strippedPath != null) {
                if (optionsStr.length() > 0) {
                    optionsStr.append(", ");
                }
                optionsStr.append("strippedPath: ");
                optionsStr.append(strippedPath);
            }

            // log the entry now
            if (optionsStr.length() > 0) {
                log.verbose("Extracting: {}   ({})", name, optionsStr);
            } else {
                log.verbose("Extracting: {}", name);
            }

            Path file = destDir.resolve(name).normalize();

            if (Files.exists(file)) {
                if (!this.force) {
                    throw new BlazeException("File already exists: " + file + " (you can call .force() to overwrite existing files)");
                } else {
                    result.filesOverwritten++;
                }
            } else {
                result.filesCreated++;
            }

            final Path dir = file.getParent();
            if (dir != null && !Files.exists(dir)) {
                Files.createDirectories(dir);
            }
            Files.copy(in, file, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new BlazeException("Failed to unarchive: " + e.getMessage(), e);
        }
    }

    private InputStream openUncompressedStream(Compressor compressor, InputStream compressedStream) throws BlazeException {
        try {
            String compressorName = ArchiveHelper.getCommonsCompressorName(compressor);

            return CompressorStreamFactory.getSingleton().createCompressorInputStream(compressorName, compressedStream);
        } catch (CompressorException e) {
            throw new BlazeException("Unable to uncompress source", e);
        }
    }

}
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
import com.fizzed.blaze.core.FileNotFoundException;
import com.fizzed.blaze.util.*;
import com.typesafe.config.ConfigException;
import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.ArchiveException;
import org.apache.commons.compress.archivers.ArchiveInputStream;
import org.apache.commons.compress.archivers.ArchiveStreamFactory;
import org.apache.commons.compress.archivers.sevenz.SevenZArchiveEntry;
import org.apache.commons.compress.archivers.sevenz.SevenZFile;
import org.apache.commons.compress.compressors.CompressorException;
import org.apache.commons.compress.compressors.CompressorInputStream;
import org.apache.commons.compress.compressors.CompressorStreamFactory;

import java.io.*;
import java.nio.file.*;
import java.util.function.Function;
import java.util.function.Predicate;
import static java.util.Optional.ofNullable;

public class Unarchive extends Action<Unarchive.Result,Void> implements VerbosityMixin<Unarchive>, ProgressMixin<Unarchive> {

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
    private final ValueHolder<Boolean> progress;
    private final Path source;
    private Path target;
    private boolean useTemporaryFiles;
    private int stripComponents;
    private boolean force;
    private Function<String,String> renamer;
    private Predicate<String> filter;

    public Unarchive(Context context, Path source) {
        super(context);
        this.log = new VerboseLogger(this);
        this.progress = new ValueHolder<>(false);
        this.source = source;
        this.useTemporaryFiles = false;
        this.stripComponents = 0;
        this.force = false;
    }

    public VerboseLogger getVerboseLogger() {
        return this.log;
    }

    @Override
    public ValueHolder<Boolean> getProgressHolder() {
        return this.progress;
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

    /**
     * Enables the use of temporary files during the unarchiving process. The file(s) will be unarchived to the target
     * directory, initially as the filename but with a .tmp file extension. Once fully unarchived, the .tmp file will
     * be renamed to the final filename.
     * By default, temporary files are not used unless explicitly configured.
     *
     * @return the current instance of {@code Unarchive} for method chaining
     */
    public Unarchive useTemporaryFiles() {
        this.useTemporaryFiles = true;
        return this;
    }

    /**
     * Sets whether temporary files will be used during the unarchiving process. If enabled,
     * files will initially be unarchived with a temporary `.tmp` extension before being
     * renamed to their final names once the unarchiving process completes.
     * This can help ensure that incomplete or partial files are not mistakenly processed.
     *
     * @param useTemporaryFiles a boolean indicating whether to use temporary files
     *                          (true to enable, false to disable)
     * @return the current instance of {@code Unarchive}, allowing for method chaining
     */
    public Unarchive useTemporaryFiles(boolean useTemporaryFiles) {
        this.useTemporaryFiles = useTemporaryFiles;
        return this;
    }

    /**
     * Configures the unarchiving process to strip the leading path component
     * from file entries in the archive. This is typically used to ignore
     * the root directory entry in an archive when extracting its contents.
     *
     * @return the current instance of {@code Unarchive}, allowing for method chaining
     */
    public Unarchive stripLeadingPath() {
        this.stripComponents = 1;
        return this;
    }

    /**
     * Configures the unarchiving process to strip a specified number of leading path
     * components from file entries in the archive. This can be used to reorganize the
     * extracted file structure by removing unnecessary directory levels.
     *
     * @param stripComponents the number of leading path components to strip from
     *                        each file entry in the archive (must be non-negative)
     * @return the current instance of {@code Unarchive}, allowing for method chaining
     */
    public Unarchive stripComponents(int stripComponents) {
        this.stripComponents = stripComponents;
        return this;
    }

    /**
     * Enables the "force" mode for the unarchiving process. This typically
     * ensures that any existing files or constraints that might prevent the
     * unarchiving are overridden or bypassed.
     *
     * @return the current instance of {@code Unarchive}, allowing for method chaining
     */
    public Unarchive force() {
        this.force = true;
        return this;
    }

    /**
     * Sets whether the "force" mode should be enabled during the unarchiving process.
     * When enabled, this typically ensures that any constraints or file conflicts
     * preventing the unarchiving process are overridden or bypassed.
     *
     * @param force a boolean indicating whether to enable or disable "force" mode
     *              (true to enable, false to disable)
     * @return the current instance of {@code Unarchive}, allowing for method chaining
     */
    public Unarchive force(boolean force) {
        this.force = force;
        return this;
    }

    /**
     * Configures a custom renaming function to be applied to the filenames
     * during the unarchiving process. The renaming function takes the original
     * filename as input and returns the desired renamed filename.
     *
     * @param renamer a {@code Function<String, String>} that defines the mapping
     *                from original filenames to renamed filenames
     * @return the current instance of {@code Unarchive}, allowing for method chaining
     */
    public Unarchive renamer(Function<String, String> renamer) {
        this.renamer = renamer;
        return this;
    }

    /**
     * Configures a filter to selectively include or exclude specific entries
     * from the unarchiving process, based on the provided {@code Predicate}.
     * The filter uses the entry name as input to determine whether the entry
     * should be processed.
     *
     * @param filter a {@code Predicate<String>} that evaluates the entry name
     *               and returns {@code true} if the entry should be included
     *               in the unarchiving process, or {@code false} otherwise
     * @return the current instance of {@code Unarchive}, allowing for method chaining
     */
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

        log.verbose("Unarchiving {} -> {}", this.source, destDir);

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

        log.debug("Using archiver={}, compressor={}",
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
                            // Single-file compression formats do not store the uncompressed size in an easily accessible header. For these formats, the only reliable way to get the size is to decompress the entire stream and count the resulting bytes.
                            this.extractEntry(archiveInfo.getUnarchivedName(), -1L, this.useTemporaryFiles, uncompressedIn, destDir, result);
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

        log.info("Unarchived {} files, overwrote {} files in {}", result.filesCreated, result.filesOverwritten, timer);

        return new Unarchive.Result(this, null);
    }

    private void unarchiveCommonsArchiveStream(Archiver archiver, InputStream in, Path destDir, Result result) throws BlazeException {
        try {
            final String archiverName = ArchiveHelper.getCommonsArchiverName(archiver);

            ArchiveInputStream<? extends ArchiveEntry> ais = ArchiveStreamFactory.DEFAULT.createArchiveInputStream(archiverName, in);

            ArchiveEntry entry = ais.getNextEntry();
            while (entry != null) {
                if (!entry.isDirectory()) {
                    this.extractEntry(entry.getName(), entry.getSize(), this.useTemporaryFiles, ais, destDir, result);
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
                        this.extractEntry(entry.getName(), entry.getSize(), this.useTemporaryFiles, in, destDir, result);
                    }
                }

                entry = sevenZFile.getNextEntry();
            }
        } catch (IOException e) {
            throw new BlazeException("Failed to unarchive: " + e.getMessage(), e);
        }
    }

    private void extractEntry(String entryName, long knownSize, boolean useTemporaryFile, InputStream in, Path destDir, Result result) throws BlazeException {
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
                    log.verbose("-x {} (filtered out)", name);
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
                log.verbose("-> {} ({})", name, optionsStr);
            } else {
                log.verbose("-> {}", name);
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

            final StreamableOutput output = Streamables.output(file, useTemporaryFile);
            try (OutputStream finalOutput = output.stream()) {
                final boolean showProgress = this.progress.get() && log.isVerbose();
                IoHelper.copy(in, finalOutput, showProgress, true, knownSize);
            }
        } catch (IOException e) {
            throw new BlazeException("Failed to unarchive: " + e.getMessage(), e);
        }
    }

    private InputStream openUncompressedStream(Compressor compressor, InputStream compressedStream) throws BlazeException {
        try {
            if (compressor == Compressor.ZSTD) {
                return new ZstdExternalInputStream(compressedStream);
            }
        } catch (IOException e) {
            throw new BlazeException("Unable to uncompress source", e);
        }

        try {
            String compressorName = ArchiveHelper.getCommonsCompressorName(compressor);

            CompressorInputStream cis = CompressorStreamFactory.getSingleton().createCompressorInputStream(compressorName, compressedStream);

            return cis;
        } catch (CompressorException e) {
            throw new BlazeException("Unable to uncompress source", e);
        }
    }

}
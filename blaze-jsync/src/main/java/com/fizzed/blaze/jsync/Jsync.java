package com.fizzed.blaze.jsync;

import com.fizzed.blaze.Context;
import com.fizzed.blaze.core.Action;
import com.fizzed.blaze.core.BlazeException;
import com.fizzed.blaze.core.ProgressMixin;
import com.fizzed.blaze.core.VerbosityMixin;
import com.fizzed.blaze.util.IoHelper;
import com.fizzed.blaze.util.ValueHolder;
import com.fizzed.blaze.util.VerboseLogger;
import com.fizzed.jsync.engine.*;
import com.fizzed.jsync.vfs.*;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class Jsync extends Action<Jsync.Result,JsyncResult> implements VerbosityMixin<Jsync>, ProgressMixin<Jsync> {

    static public class Result extends com.fizzed.blaze.core.Result<Jsync,JsyncResult,Result> {

        Result(Jsync action, JsyncResult value) {
            super(action, value);
        }

    }

    private final VerboseLogger log;
    private final ValueHolder<Boolean> progress;
    private final JsyncEngine engine;
    private VirtualVolume source;
    private VirtualVolume target;
    private JsyncMode mode;

    public Jsync(Context context) {
        super(context);
        this.log = new VerboseLogger(this);
        this.progress = new ValueHolder<>(false);
        this.engine = new JsyncEngine();
    }

    @Override
    public VerboseLogger getVerboseLogger() {
        return this.log;
    }

    @Override
    public ValueHolder<Boolean> getProgressHolder() {
        return this.progress;
    }

    /**
     * Enables the creation of parent directories for the target path
     * during the jsync operation.
     *
     * This method is a shorthand for {@code parents(true)}, enabling
     * parent directory creation by default.
     *
     * @return the current instance of {@code Jsync} for method chaining.
     */
    public Jsync parents() {
        return this.parents(true);
    }

    /**
     * Specifies whether parent directories should be created for the target
     * during the jsync operation.
     *
     * @param parents a boolean value indicating whether parent directories
     *                should be created (true) or not (false).
     * @return the current instance of {@code Jsync} for method chaining.
     */
    public Jsync parents(boolean parents) {
        this.engine.setParents(parents);
        return this;
    }

    /**
     * Enables deletion of extraneous files from the target directory
     * during the synchronization process.
     *
     * This method is a shorthand for {@code delete(true)}, which
     * enables the deletion of such files by default.
     *
     * @return the current instance of {@code Jsync} for method chaining
     *         after enabling deletion.
     */
    public Jsync delete() {
        return this.delete(true);
    }

    /**
     * Specifies whether extraneous files should be deleted from the target
     * directory during the synchronization process.
     *
     * @param delete a boolean value indicating whether deletion of extraneous
     *               files should be enabled (true) or disabled (false).
     * @return the current instance of {@code Jsync} for method chaining.
     */
    public Jsync delete(boolean delete) {
        this.engine.setDelete(delete);
        return this;
    }

    /**
     * Enables the "force" behavior for the jsync operation. This is a shorthand
     * for {@code force(true)} and allows for overwriting files or handling
     * specific operational conditions that require force to be enabled. One such example is if the target has
     * a path that doesn't match the same type as the source (e.g. source file -> target dir with same name)
     *
     * @return the current instance of {@code Jsync} for method chaining.
     */
    public Jsync force() {
        return this.force(true);
    }

    /**
     * Specifies whether the "force" behavior should be enabled or disabled
     * for the jsync operation. When enabled, it allows for overwriting files
     * or handling specific operational conditions that require force, such as
     * mismatched source and target types (e.g., a source file being copied to
     * a target directory with the same name).
     *
     * @param force a boolean value indicating whether the force behavior
     *              should be enabled (true) or disabled (false).
     * @return the current instance of {@code Jsync} for method chaining.
     */
    public Jsync force(boolean force) {
        this.engine.setForce(force);
        return this;
    }

    /**
     * Enables the option to ignore file timestamps during the synchronization process.
     * This method is a shorthand for {@code ignoreTimes(true)}, automatically enabling
     * the ignore timestamps functionality. This basicaly ensures that checksums are performed to check if a file
     * needs to be updated, and entirely ignores timestamps.
     *
     * @return the current instance of {@code Jsync} for method chaining.
     */
    public Jsync ignoreTimes() {
        return this.ignoreTimes(true);
    }

    /**
     * Configures whether file timestamps should be ignored during the synchronization process.
     * When enabled, the operation will rely on checksums to determine if a file needs to be updated,
     * disregarding file timestamps entirely.
     *
     * @param ignoreTimes a boolean value indicating whether to ignore file timestamps (true)
     *                    or not (false) during synchronization.
     * @return the current instance of {@code Jsync} for method chaining.
     */
    public Jsync ignoreTimes(boolean ignoreTimes) {
        this.engine.setIgnoreTimes(ignoreTimes);
        return this;
    }

    /**
     * Enables skipping of permission-related operations during the synchronization process.
     * This method is a shorthand for {@code skipPermissions(true)}, automatically enabling
     * the skip permissions functionality.
     *
     * @return the current instance of {@code Jsync} for method chaining.
     */
    public Jsync skipPermissions() {
        return this.skipPermissions(true);
    }

    /**
     * Specifies whether permission-related operations should be skipped during the synchronization process.
     *
     * @param skipPermissions a boolean value indicating whether to skip permission-related operations (true)
     *                        or perform them (false) during synchronization.
     * @return the current instance of {@code Jsync} for method chaining.
     */
    public Jsync skipPermissions(boolean skipPermissions) {
        this.engine.setSkipPermissions(skipPermissions);
        return this;
    }

    /**
     * Adds the specified file or directory to the list of excluded items
     * for the synchronization process. This can be used to specify files or
     * directories that should not be included in the synchronization. If you specify these plus set delete to true
     * then this will also cause any extraneous files to be deleted from the target directory.
     *
     * @param exclude the file or directory pattern to be excluded from synchronization.
     * @return the current instance of {@code Jsync} for method chaining.
     */
    public Jsync exclude(String exclude) {
        this.engine.addExclude(exclude);
        return this;
    }

    /**
     * Adds multiple files or directories to the list of excluded items
     * for the synchronization process. The specified list of files or directories
     * will not be included in the synchronization. If deletion is enabled,
     * these excluded items will also cause any extraneous files to be deleted
     * from the target directory.
     *
     * @param excludes a list of file or directory patterns to be excluded from synchronization.
     * @return the current instance of {@code Jsync} for method chaining.
     */
    public Jsync excludes(List<String> excludes) {
        for (String exclude : excludes) {
            this.exclude(exclude);
        }
        return this;
    }

    /**
     * Adds the specified path to the list of ignored paths or files in the synchronization process.
     *
     * @param ignore the pattern or path to be ignored during synchronization
     * @return the current Jsync instance for method chaining
     */
    public Jsync ignore(String ignore) {
        this.engine.addIgnore(ignore);
        return this;
    }

    /**
     * Specifies multiple paths to be ignored during the synchronization process.
     * Each pattern in the provided list will be added to the list of ignored items.
     *
     * @param ignores a list of file or directory patterns to be ignored during synchronization.
     * @return the current instance of {@code Jsync} for method chaining.
     */
    public Jsync ignores(List<String> ignores) {
        for (String ignore : ignores) {
            this.ignore(ignore);
        }
        return this;
    }

    /**
     * Specifies the preferred checksums to be used during the synchronization process.
     * Multiple checksum types can be provided, and they will be applied in the order
     * they are specified.
     *
     * @param checksum the preferred checksum types to use, such as CK, MD5, or SHA1.
     *                 Multiple values can be passed to prioritize different checksum modes.
     * @return the current instance of {@code Jsync} for method chaining.
     */
    public Jsync preferredChecksums(Checksum... checksum) {
        this.engine.setPreferredChecksums(checksum);
        return this;
    }

    /**
     * Sets the source virtual volume for the synchronization operation.
     *
     * @param source the VirtualVolume object representing the source location
     *               for the synchronization process.
     * @return the current instance of {@code Jsync} for method chaining.
     */
    public Jsync source(VirtualVolume source) {
        this.source = source;
        return this;
    }

    /**
     * Configures the target virtual volume and the synchronization mode
     * for the jsync operation. The target specifies the destination location
     * for the synchronization process, while the mode determines how
     * the source is structured within the target.
     *
     * @param target the VirtualVolume object representing the target location
     *               for the synchronization process.
     * @param mode the JsyncMode determining the synchronization behavior,
     *             such as NEST or MERGE.
     * @return the current instance of {@code Jsync} for method chaining.
     */
    public Jsync target(VirtualVolume target, JsyncMode mode) {
        this.target = target;
        this.mode = mode;
        return this;
    }

    @Override
    protected Result doRun() throws BlazeException {
        Objects.requireNonNull(this.source, "source volume not specified");
        Objects.requireNonNull(this.target, "target volume not specified");

        // enable logging & progress bars
        this.engine.setEventHandler(new ProgressEventHandler());

        try {
            final JsyncResult result = this.engine.sync(this.source, this.target, this.mode);

            return new Result(this, result);
        } catch (Exception e) {
            throw new BlazeException(e.getMessage(), e);
        }
    }

    private class ProgressEventHandler implements JsyncEventHandler {

        @Override
        public void willBegin(VirtualFileSystem sourceVfs, VirtualPath sourcePath, VirtualFileSystem targetVfs, VirtualPath targetPath) {
            log.verbose("Syncing {}:{} -> {}:{}", sourceVfs, sourcePath, targetVfs, targetPath);
        }

        @Override
        public void willEnd(VirtualFileSystem sourceVfs, VirtualPath sourcePath, VirtualFileSystem targetVfs, VirtualPath targetPath, JsyncResult result, long timeMillis) {
            log.verbose("Synced {} files (new={} updated={} deleted={}) {} dirs (new={} deleted={}) {} stats (in {} ms)",
                result.getFilesTotal(), result.getFilesCreated(), result.getFilesUpdated(), result.getFilesDeleted(),
                result.getDirsTotal(), result.getDirsCreated(), result.getDirsDeleted(), result.getStatsOnlyUpdated(), timeMillis);
        }

        @Override
        public void willExcludePath(VirtualPath sourcePath) {
            log.debug("Excluding {}", sourcePath);
        }

        @Override
        public void willIgnoreSourcePath(VirtualPath sourcePath) {
            log.debug("Ignoring source {}", sourcePath);
        }

        @Override
        public void willIgnoreTargetPath(VirtualPath targetPath) {
            log.debug("Ignoring target {}", targetPath);
        }

        @Override
        public void willCreateDirectory(VirtualPath targetPath, boolean recursively) {
            if (!recursively) {
                log.verbose("Creating directory {}", targetPath);
            } else {
                log.debug("Creating directory {}", targetPath);
            }
        }

        @Override
        public void willDeleteDirectory(VirtualPath targetPath, boolean recursively) {
            if (!recursively) {
                log.verbose("Deleting directory {}", targetPath);
            } else {
                log.debug("Deleting directory {}", targetPath);
            }
        }

        @Override
        public void willDeleteFile(VirtualPath targetPath, boolean recursively) {
            if (!recursively) {
                log.verbose("Deleting file {}", targetPath);
            } else {
                log.debug("Deleting file {}", targetPath);
            }
        }

        @Override
        public void willTransferFile(VirtualPath sourcePath, VirtualPath targetPath, JsyncPathChanges changes) {
            if (changes.isMissing()) {
                log.verbose("Creating file {} ({})", targetPath, changes);
            } else {
                log.verbose("Updating file {} ({})", targetPath, changes);
            }
        }

        @Override
        public void willUpdateStat(VirtualPath sourcePath, VirtualPath targetPath, JsyncPathChanges changes, Collection<StatUpdateOption> options, boolean associatedWithFileUpdateOrDirCreated) {
            // if the stat change is simply associate, we don't need to log it verbosely
            String optionsStr = options.stream().map(v -> v.name().toLowerCase()).collect(Collectors.joining(", "));
            log.debug("Updating stat {} ({})", targetPath, optionsStr);
        }

        @Override
        public void doCopy(InputStream input, OutputStream output, long knownContentLength) throws IOException {
            boolean progressEnabled = getVerboseLogger().isVerbose() && progress.get();
            IoHelper.copy(input, output, progressEnabled, true, knownContentLength);
        }
    }

}
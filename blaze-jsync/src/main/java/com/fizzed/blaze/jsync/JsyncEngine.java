package com.fizzed.blaze.jsync;

import com.fizzed.blaze.core.VerbosityMixin;
import com.fizzed.blaze.util.IoHelper;
import com.fizzed.blaze.util.VerboseLogger;
import com.fizzed.blaze.vfs.*;
import com.fizzed.blaze.vfs.util.VirtualPathPair;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Path;
import java.util.*;

import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toList;

public class JsyncEngine implements VerbosityMixin<JsyncEngine> {

    // options for syncing, try to mimic defaults for how rsync works
    final private VerboseLogger log;
    final private List<Checksum> preferredChecksums;
    private boolean delete;
    private boolean force;
    private boolean parents;
    private boolean progress;
    private boolean ignoreTimes;
    private int maxFilesMaybeModifiedLimit;
    private List<String> excludes;

    public JsyncEngine() {
        this.log = new VerboseLogger(this);
        this.delete = false;
        this.force = false;
        this.progress = false;
        this.parents = false;
        this.ignoreTimes = false;
        this.preferredChecksums = new ArrayList<>(asList(Checksum.CK, Checksum.MD5));
        this.maxFilesMaybeModifiedLimit = 256;
        this.excludes = null;
    }

    @Override
    public VerboseLogger getVerboseLogger() {
        return this.log;
    }

    public boolean isDelete() {
        return this.delete;
    }

    public JsyncEngine setDelete(boolean delete) {
        this.delete = delete;
        return this;
    }

    public boolean isForce() {
        return this.force;
    }

    public JsyncEngine setForce(boolean force) {
        this.force = force;
        return this;
    }

    public boolean isParents() {
        return this.parents;
    }

    public JsyncEngine setParents(boolean parents) {
        this.parents = parents;
        return this;
    }

    public boolean isIgnoreTimes() {
        return this.ignoreTimes;
    }

    public JsyncEngine setIgnoreTimes(boolean ignoreTimes) {
        this.ignoreTimes = ignoreTimes;
        return this;
    }

    public boolean isProgress() {
        return this.progress;
    }

    public JsyncEngine setProgress(boolean progress) {
        this.progress = progress;
        return this;
    }

    public List<Checksum> getPreferredChecksums() {
        return this.preferredChecksums;
    }

    public JsyncEngine setPreferredChecksums(Checksum... checksum) {
        this.preferredChecksums.clear();
        this.preferredChecksums.addAll(asList(checksum));
        return this;
    }

    public int getMaxFilesMaybeModifiedLimit() {
        return this.maxFilesMaybeModifiedLimit;
    }

    public JsyncEngine setMaxFilesMaybeModifiedLimit(int maxFilesMaybeModifiedLimit) {
        this.maxFilesMaybeModifiedLimit = maxFilesMaybeModifiedLimit;
        return this;
    }

    public List<String> getExcludes() {
        return excludes;
    }

    public JsyncEngine setExcludes(List<String> excludes) {
        this.excludes = excludes;
        return this;
    }

    public JsyncEngine addExclude(String exclude) {
        if (this.excludes == null) {
            this.excludes = new ArrayList<>();
        }
        this.excludes.add(exclude);
        return this;
    }

    public JsyncResult sync(Path sourcePath, Path targetPath, JsyncMode mode) throws IOException {
        // local -> local
        final LocalVirtualFileSystem localVfs = LocalVirtualFileSystem.open();

        return this.sync(localVfs, sourcePath.toString(), localVfs, targetPath.toString(), mode);
    }

    public JsyncResult sync(VirtualFileSystem sourceVfs, String sourcePath, VirtualFileSystem targetVfs, String targetPath, JsyncMode mode) throws IOException {
        final JsyncResult result = new JsyncResult();

        // source MUST exist
        final VirtualPath sourcePathRaw = VirtualPath.parse(sourcePath);
        final VirtualPath sourcePathAbsWithoutStat = sourceVfs.pwd().resolve(sourcePathRaw);
        // NOTE: this will throw an exception if the source dir/file does not exist
        final VirtualPath sourcePathAbs = sourceVfs.stat(sourcePathAbsWithoutStat);


        // its better to use absolute paths on source & target since the checksum methods on any host require full paths
        // the target will assume the "directory" value from the source, since in most cases it will be the exact same
        VirtualPath targetPathRaw = VirtualPath.parse(targetPath, sourcePathAbs.isDirectory());

        // if we're dealing with a NESTED target, the only difference is we need to build the target using the "name" of source
        if (mode == JsyncMode.NEST) {
            targetPathRaw = targetPathRaw.resolve(sourcePathAbs.getName(), sourcePathAbs.isDirectory());
        }

        final VirtualPath targetPathAbsWithoutStat = targetVfs.pwd().resolve(targetPathRaw);

        //
        // Create parent directories of target if necessary
        //

        // the target may or may not exist yet (which is not yet an error, so we use 'exists' for stats)
        VirtualPath targetPathAbs = targetVfs.exists(targetPathAbsWithoutStat);

        // if the target is missing, we will need to make sure it exists or its parent dirs exist
        if (targetPathAbs == null) {
            if (targetPathAbsWithoutStat.isDirectory()) {
                // we're dealing with directories, so we need to ensure this dir exists
                this.createDirectory(result, targetVfs, targetPathAbsWithoutStat, true, this.parents);

                // we need to make sure we have this directory, with stats
                targetPathAbs = targetVfs.stat(targetPathAbsWithoutStat);
            } else {
                // we're dealing with files, so we just need to ensure the parent dir exists
                final VirtualPath parentDir = targetPathAbsWithoutStat.resolveParent();

                // check if it exists first, if not then we will create it
                if (targetVfs.exists(parentDir) == null) {
                    this.createDirectory(result, targetVfs, parentDir, true, this.parents);
                }

                // we know the file doesn't exist yet, so we will use the path without stats
                targetPathAbs = targetPathAbsWithoutStat;
            }
        }

        //
        // Negotiate checksum methods between source and target filesystems if necessary
        //

        // find the best common checksum to use
        final Checksum checksum = this.negotiateChecksum(sourceVfs, targetVfs);


        log.info("Syncing {}:{} -> {}:{} (mode={}, checksum={}, delete={})",
            sourceVfs, sourcePathAbs, targetVfs, targetPathAbs, mode, checksum, this.delete);


        //
        // Ready to start sync, the only part that matters is if we're syncing a director or a file
        //

        final List<VirtualPathPair> deferredFiles = new ArrayList<>();

        if (sourcePathAbs.isDirectory()) {
            // any excludes, let's resolve them against pwd of the source to make it easier to exclude them
            final List<VirtualPath> excludePaths;
            if (this.excludes != null) {
                excludePaths = this.excludes.stream()
                    .map(VirtualPath::parse)
                    .map(sourcePathAbs::resolve)
                    .collect(toList());
            } else {
                excludePaths = Collections.emptyList();
            }

            // as we process files, only a subset may require more advanced methods of detecting whether they were modified
            // since that process could be "expensive", we keep a list of files on source/target that we will defer processing
            // until we have a chance to do some bulk processing of checksums, etc.
            this.syncDirectory(0, result, excludePaths, deferredFiles, sourceVfs, sourcePathAbs, targetVfs, targetPathAbs, checksum);
        } else {
            // we are only syncing a file, we may need to do some more expensive checks to determine if it needs to be updated
            this.syncFile(result, deferredFiles, sourceVfs, sourcePathAbs, targetVfs, targetPathAbs);
            this.syncDeferredFiles(result, deferredFiles, sourceVfs, targetVfs, checksum);
        }

        return result;
    }

    protected void syncFile(JsyncResult result, List<VirtualPathPair> deferredFiles, VirtualFileSystem sourceVfs, VirtualPath sourcePath, VirtualFileSystem targetVfs, VirtualPath targetPath) throws IOException {
        // source needs to be a file
        if (sourcePath.isDirectory()) {
            throw new IllegalArgumentException("Source path " + sourcePath + " must be a file");
        }

        // target needs to be a file
        if (targetPath.isDirectory()) {
            log.warn("Type mismatch: source {} is a file but target {} is a directory!", sourcePath, targetPath);

            if (!this.force) {
                throw new PathOverwriteException("Type mismatch: source " + sourcePath + " is a file but target " + targetPath + " is a directory. Either delete the target directory manually or use the 'force' option to have jsync do it for you.");
            }

            // delete the target dir
            this.deleteDirectory(0, result, targetVfs, targetPath);

            // create a new target path that's a file and will be "missing"
            targetPath = new VirtualPath(targetPath.getParentPath(), sourcePath.getName(), false, null);
        }

        // detect what changes exists between source & target paths
        final JsyncPathChanges changes = this.detectChanges(sourcePath, targetPath);

        log.debug("Itemized changes to {}: {}", targetPath, changes);

        // first, check if we should defer syncing the file till later on
        if (deferredFiles != null && changes.isDeferredProcessing(this.ignoreTimes)) {
            deferredFiles.add(new VirtualPathPair(sourcePath, targetPath));
            return;
        }

        // do we need to sync the file content now?
        if (changes.isContentModified(this.ignoreTimes)) {
            this.syncFileContent(result, sourceVfs, sourcePath, targetVfs, targetPath);
        }

        if (changes.isStatModified()) {
            // stat will need updated if the dir is new OR if the dir stats have changed
            this.syncPathStat(result, sourcePath, targetVfs, targetPath);
        }
    }

    protected void syncDeferredFiles(JsyncResult result, List<VirtualPathPair> deferredFiles, VirtualFileSystem sourceVfs, VirtualFileSystem targetVfs, Checksum checksum) throws IOException {
        // we need to calculate checksums for source and target files
        final List<VirtualPath> sourceFiles = deferredFiles.stream()
            .map(VirtualPathPair::getSource)
            .collect(toList());

        sourceVfs.checksums(checksum, sourceFiles);

        final List<VirtualPath> targetFiles = deferredFiles.stream()
            .map(VirtualPathPair::getTarget)
            .collect(toList());

        targetVfs.checksums(checksum, targetFiles);

        result.incrementChecksums(targetFiles.size());

        for (VirtualPathPair pair : deferredFiles) {
            // call sync file with deferred processing disabled
            this.syncFile(result, null, sourceVfs, pair.getSource(), targetVfs, pair.getTarget());
        }

        deferredFiles.clear();
    }

    protected void syncDirectory(int level, JsyncResult result, List<VirtualPath> excludePaths, List<VirtualPathPair> deferredFiles, VirtualFileSystem sourceVfs, VirtualPath sourcePath, VirtualFileSystem targetVfs, VirtualPath targetPath, Checksum checksum) throws IOException {

        // source needs to be a directory
        if (!sourcePath.isDirectory()) {
            throw new IllegalArgumentException("Source path " + sourcePath + " must be a directory");
        }

        // target needs to be a directory
        if (!targetPath.isDirectory()) {
            log.warn("Type mismatch: source {} is a directory but target '{}' is a file!", sourcePath, targetPath);

            if (!this.force) {
                throw new PathOverwriteException("Type mismatch: source " + sourcePath + " is a directory but target " + targetPath + " is a file. Either delete the target file manually or use the 'force' option to have jsync do it for you.");
            }

            // delete the target file
            log.verbose("Deleting file {}", targetPath);
            targetVfs.rm(targetPath);
            result.incrementFilesDeleted();

            // create a new target path that's a directory and will be "missing"
            targetPath = new VirtualPath(targetPath.getParentPath(), sourcePath.getName(), true, null);
        }

        // detect what changes exists between source & target paths
        final JsyncPathChanges changes = this.detectChanges(sourcePath, targetPath);

        log.debug("Itemized changes to {}: {}", targetPath, changes);

        if (changes.isMissing()) {
            this.createDirectory(result, targetVfs, targetPath, false, false);
        }


        // NOTE: stat must be changed last - AFTER all files within the directory are guaranteed to not be touched
        // further, otherwise the operating system will update the modified timestamp when files are changed in the dir


        // we need a list of files in both directories, so we can see what to add/delete
        List<VirtualPath> sourceChildPaths = sourceVfs.ls(sourcePath).stream()
            // apply filter to source files if they are on the exclude list
            .filter(v -> {
                //log.info("Checking for exlcude of path {} with excludes {}", v, excludePaths);
                for (VirtualPath excludePath : excludePaths) {
                    if (v.startsWith(excludePath)) {
                        log.verbose("Excluding path {}", v);
                        return false;
                    }
                }
                return true;
            })
            // apply filter to excluding non-regular files (such as symlinks)
            .filter(v -> {
                switch (v.getStat().getType()) {
                    case SYMLINK:
                        log.warn("Excluding symlink {} (not supported at this time)", v);
                        return false;
                    case OTHER:
                        log.warn("Excluding non-regular file {} (not supported at this time)", v);
                        return false;
                    default:
                        return true;
                }
            })
            .collect(toList());

        final List<VirtualPath> targetChildPaths = targetVfs.ls(targetPath);

        // its better to work with all dirs first, then files, so we sort the files before we process them
        this.sortPaths(sourceChildPaths);
        this.sortPaths(targetChildPaths);

        // calculate paths new / changed / same
        CHILD_LOOP:
        for (VirtualPath sourceChildPath : sourceChildPaths) {
            // find a matching target path entirely by name
            VirtualPath targetChildPath = targetChildPaths.stream()
                .filter(p -> targetVfs.areFileNamesEqual(p.getName(), sourceChildPath.getName()))
                .findFirst()
                .orElse(null);

            // if the child path is missing, create it and have it take the type of the source
            if (targetChildPath == null) {
                targetChildPath = targetPath.resolve(sourceChildPath.getName(), sourceChildPath.isDirectory(), null);
            }

            if (sourceChildPath.isDirectory()) {
                this.syncDirectory(level+1, result, excludePaths, deferredFiles, sourceVfs, sourceChildPath, targetVfs, targetChildPath, checksum);
            } else {
                // NOTE: it's possible syncFile will "defer" processing if a checksum is required
                this.syncFile(result, deferredFiles, sourceVfs, sourceChildPath, targetVfs, targetChildPath);
            }
        }

        // handle any deferred files that need to be processed
        if (level == 0 || deferredFiles.size() >= this.maxFilesMaybeModifiedLimit) {
            this.syncDeferredFiles(result, deferredFiles, sourceVfs, targetVfs, checksum);
        }

        // handle any paths that need to be deleted
        if (this.delete) {
            for (VirtualPath targetChildPath : targetChildPaths) {
                // find a matching source path entirely by name
                final VirtualPath sourceChildPath = sourceChildPaths.stream()
                    .filter(p -> sourceVfs.areFileNamesEqual(p.getName(), targetChildPath.getName()))
                    .findFirst()
                    .orElse(null);

                if (sourceChildPath == null) {
                    if (targetChildPath.isDirectory()) {
                        // NOTE: this method handles recursion
                        deleteDirectory(0, result, targetVfs, targetChildPath);
                    } else {
                        log.verbose("Deleting file {}", targetChildPath);
                        targetVfs.rm(targetChildPath);
                        result.incrementFilesDeleted();
                    }
                }
            }
        }

        // last step is to update the stat of the target dir
        // To successfully preserve directory timestamps, you must set the directory attributes after you have finished touching every single file inside that directory.
        if (changes.isStatModified()) {
            // stat will need updated if the dir is new OR if the dir stats have changed
            this.syncPathStat(result, sourcePath, targetVfs, targetPath);
        }
    }

    protected JsyncPathChanges detectChanges(VirtualPath sourcePath, VirtualPath targetPath) throws IOException {
        // source "stats" MUST exist
        Objects.requireNonNull(sourcePath, "sourceFile cannot be null");

        if (sourcePath.getStat() == null) {
            log.error("Source file {} missing 'stat' (it must not exist yet on source)", sourcePath);
            throw new IllegalArgumentException("sourceFile must have a 'stat' object");
        }

        // if the targetFile "stat" are null then we know it must not even exist yet
        if (targetPath == null || targetPath.getStat() == null) {
            log.trace("Target path {} missing (new dir/file)", targetPath);
            // we can immediately return the changes since the stat object doesn't exist
            return new JsyncPathChanges(true, false, false, false, false, null);
        }

        // the remaining properties can all now be calculate
        boolean size = false;
        boolean timestamps = false;
        boolean ownership = false;
        boolean permissions = false;
        Boolean checksums = null;           // unknown

        // are file sizes different? if they are then this is a cheap way of figuring out a sync is needed
        // only try to compare sizes if they are not dirs
        if (!sourcePath.isDirectory()) {
            if (sourcePath.getStat().getSize() != targetPath.getStat().getSize()) {
                log.trace("Source path {} size {} != target size {} (modified file)", sourcePath, sourcePath.getStat().getSize(), targetPath.getStat().getSize());
                size = true;
            }
        }

        // if we can take modified timestamps into account, this is a cheap way of figuring out a file changed
        // due to lack of millis precision in filesystems, we need to allow a larger delta of difference
        if (Math.abs(sourcePath.getStat().getModifiedTime() - targetPath.getStat().getModifiedTime()) > 2000L) {
            log.trace("Source path {} modified time {} != target modified time {} (maybe modified file)", sourcePath, sourcePath.getStat().getModifiedTime(), targetPath.getStat().getModifiedTime());
            timestamps = true;
        }

        // if we have "cksum" values on both sides, we can compare those
        if (sourcePath.getStat().getCksum() != null && targetPath.getStat().getCksum() != null) {
            if (!sourcePath.getStat().getCksum().equals(targetPath.getStat().getCksum())) {
                log.trace("Source path {} cksum {} != target chksum {} (modified file)", sourcePath, sourcePath.getStat().getCksum(), targetPath.getStat().getCksum());
                checksums = true;
            } else {
                checksums = false;
            }
        }

        // if we have "md5" values on both sides, we can compare those
        if (sourcePath.getStat().getMd5() != null && targetPath.getStat().getMd5() != null) {
            if (!sourcePath.getStat().getMd5().equalsIgnoreCase(targetPath.getStat().getMd5())) {
                log.trace("Source path {} md5 {} != target md5 {} (modified file)", sourcePath, sourcePath.getStat().getMd5(), targetPath.getStat().getMd5());
                checksums = true;
            } else {
                checksums = false;
            }
        }

        // if we have "sha1" values on both sides, we can compare those
        if (sourcePath.getStat().getSha1() != null && targetPath.getStat().getSha1() != null) {
            if (!sourcePath.getStat().getSha1().equalsIgnoreCase(targetPath.getStat().getSha1())) {
                log.trace("Source path {} sha1 {} != target sha1 {} (modified file)", sourcePath, sourcePath.getStat().getSha1(), targetPath.getStat().getSha1());
                checksums = true;
            } else {
                checksums = false;
            }
        }

        return new JsyncPathChanges(false, size, timestamps, ownership, permissions, checksums);
    }

    protected void syncFileContent(JsyncResult result, VirtualFileSystem sourceVfs, VirtualPath sourceFile, VirtualFileSystem targetVfs, VirtualPath targetFile) throws IOException {
        // if the target file has no "stats", then we have no info on it yet, and know we're going to create it fresh
        if (targetFile.getStat() == null) {
            log.verbose("Creating file {}", targetFile);
        } else {
            log.verbose("Updating file {}", targetFile);
        }

        // progress is only enabled if verbose is too
        final boolean progress = this.getVerboseLogger().isVerbose() && this.progress;

        // transfer the file
        try (InputStream input = sourceVfs.readFile(sourceFile)) {
            try (OutputStream output = targetVfs.writeStream(targetFile)) {
                IoHelper.copy(input, output, progress, true, sourceFile.getStat().getSize());
            }
        }

        // update results after we know the operation was successful
        if (targetFile.getStat() == null) {
            result.incrementFilesCreated();
        } else {
            result.incrementFilesUpdated();
        }
    }

    protected void syncPathStat(JsyncResult result, VirtualPath sourcePath, VirtualFileSystem targetVfs, VirtualPath targetPath) throws IOException {
        log.debug("Updating stat {}", targetPath);

        targetVfs.updateStat(targetPath, sourcePath.getStat());

        result.incrementStatsUpdated();
    }

    protected void createDirectory(JsyncResult result, VirtualFileSystem vfs, VirtualPath path, boolean verifyParentExists, boolean parents) throws IOException {
        // if parents is enabled, we want to make any parent dirs that are also missing
        if (parents) {
            List<VirtualPath> parentDirsMissing = new ArrayList<>();

            VirtualPath parentPath = path.resolveParent();
            while (parentPath != null) {
                VirtualPath parentPathStats = vfs.exists(parentPath);
                if (parentPathStats != null) {
                    // we have the parent dir, we can stop checking
                    break;
                }
                // otherwise, we need to create the parent dir
                parentDirsMissing.add(parentPath);
                parentPath = parentPath.resolveParent();
            }

            // any parent dirs missing? we need to process them in reverse order
            if (!parentDirsMissing.isEmpty()) {
                for (int i = parentDirsMissing.size()-1; i >= 0; i--) {
                    VirtualPath parentPathMissing = parentDirsMissing.get(i);
                    log.debug("Creating parent dir: {}", parentPathMissing);
                    vfs.mkdir(parentPathMissing);
                    result.incrementDirsCreated();
                }
            }
        } else if (verifyParentExists) {
            // if parents is disabled, we want to make sure the parent dir exists, so we can throw a better exception
            VirtualPath parentPath = path.resolveParent();
            if (parentPath != null) {
                final VirtualPath parentPathStats = vfs.exists(parentPath);
                if (parentPathStats == null) {
                    throw new ParentDirectoryMissingException("Unable to create directory '" + path + "' since its parent directory does not exist (did you forget to use 'parents' option?)");
                } else if (!parentPathStats.isDirectory()) {
                    throw new PathOverwriteException("Unable to create directory '" + path + "' since its parent directory exists, but is not a directory!");
                }
            }
        }

        // finally we can create the directory
        log.verbose("Creating dir {}", path);
        vfs.mkdir(path);
        result.incrementDirsCreated();
    }

    protected void deleteDirectory(int level, JsyncResult result, VirtualFileSystem vfs, VirtualPath path) throws IOException {
        // we need a list of files in both directories, since we'll need to recurse thru dirs
        final List<VirtualPath> childPaths = vfs.ls(path);
        sortPaths(childPaths);

        for (VirtualPath childPath : childPaths) {
            if (childPath.isDirectory()) {
                deleteDirectory(level+1, result, vfs, childPath);     // do not log this, that will happen in the below statement via recursion
            } else {
                log.debug("Deleting file {}", childPath);
                vfs.rm(childPath);
                result.incrementFilesDeleted();
            }
        }

        // finally we can delete the directory, if level 0, we log as info, but anything else is considered debugging
        if (level > 0) {
            log.debug("Deleting child dir {}", path);
        } else {
            log.verbose("Deleting dir {}", path);
        }
        vfs.rmdir(path);
        result.incrementDirsDeleted();
    }

    protected Checksum negotiateChecksum(VirtualFileSystem sourceVfs, VirtualFileSystem targetVfs) throws IOException {
        log.debug("Negotiating checksums supported on both source and target filesystems...");

        // negotiate the checksums to use
        Checksum checksum = null;
        List<Checksum> sourceChecksumsSupported = new ArrayList<>();
        List<Checksum> targetChecksumsSupported = new ArrayList<>();

        for (Checksum preferredChecksum : this.preferredChecksums) {
            log.debug("Detecting if {} checksum is supported on source/target", preferredChecksum);

            // check supported checksums, keep a tally of which are supported by both sides, so we can log them out
            boolean sourceSupported = sourceVfs.isSupported(preferredChecksum);

            if (sourceSupported) {
                sourceChecksumsSupported.add(preferredChecksum);
            }

            boolean targetSupported = targetVfs.isSupported(preferredChecksum);

            if (targetSupported) {
                targetChecksumsSupported.add(preferredChecksum);
            }

            log.verbose("Detected {} checksum supported on source={}, target={}", preferredChecksum, sourceSupported, targetSupported);

            if (sourceSupported && targetSupported) {
                checksum = preferredChecksum;
                break;
            }
        }

        if (checksum == null) {
            throw new IOException("Unable to find a checksum that is supported by both source and target filesystems. " +
                "Source filesystem " + sourceVfs.getName() + " supports checksums " + sourceChecksumsSupported
                + " and target filesystem " + targetVfs.getName() + " supports checksums " + targetChecksumsSupported);
        }

        return checksum;
    }

    protected void sortPaths(List<VirtualPath> paths) {
        // sort, where directories come before files
        paths.sort((p1, p2) -> {
            if (p1.isDirectory() && !p2.isDirectory()) {
                return -1;
            } else if (!p1.isDirectory() && p2.isDirectory()) {
                return 1;
            } else {
                return p1.getName().compareTo(p2.getName());
            }
        });
    }

}
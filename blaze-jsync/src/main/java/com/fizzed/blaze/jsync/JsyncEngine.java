package com.fizzed.blaze.jsync;

import com.fizzed.blaze.util.StreamableInput;
import com.fizzed.blaze.vfs.*;
import com.fizzed.blaze.vfs.util.VirtualPathPair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Path;
import java.util.*;

import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toList;

public class JsyncEngine {
    static private final Logger log = LoggerFactory.getLogger(JsyncEngine.class);

    // options for syncing, try to mimic defaults for how rsync works
    final private List<Checksum> preferredChecksums;
    private boolean delete;
    private boolean force;
    private boolean parents;
    private boolean progress;
    private boolean ignoreTimes;
    private int maxFilesMaybeModifiedLimit;

    public JsyncEngine() {
        this.delete = false;
        this.force = false;
        this.progress = false;
        this.parents = false;
        this.ignoreTimes = false;
        this.preferredChecksums = new ArrayList<>(asList(Checksum.CK, Checksum.MD5));
        this.maxFilesMaybeModifiedLimit = 256;
    }

    public boolean isDelete() {
        return delete;
    }

    public JsyncEngine setDelete(boolean delete) {
        this.delete = delete;
        return this;
    }

    public boolean isForce() {
        return force;
    }

    public JsyncEngine setForce(boolean force) {
        this.force = force;
        return this;
    }

    public boolean isParents() {
        return parents;
    }

    public JsyncEngine setParents(boolean parents) {
        this.parents = parents;
        return this;
    }

    public boolean isIgnoreTimes() {
        return ignoreTimes;
    }

    public JsyncEngine setIgnoreTimes(boolean ignoreTimes) {
        this.ignoreTimes = ignoreTimes;
        return this;
    }

    public boolean isProgress() {
        return progress;
    }

    public JsyncEngine setProgress(boolean progress) {
        this.progress = progress;
        return this;
    }

    public List<Checksum> getPreferredChecksums() {
        return preferredChecksums;
    }

    public JsyncEngine setPreferredChecksums(Checksum... checksum) {
        this.preferredChecksums.clear();
        this.preferredChecksums.addAll(asList(checksum));
        return this;
    }

    public int getMaxFilesMaybeModifiedLimit() {
        return maxFilesMaybeModifiedLimit;
    }

    public JsyncEngine setMaxFilesMaybeModifiedLimit(int maxFilesMaybeModifiedLimit) {
        this.maxFilesMaybeModifiedLimit = maxFilesMaybeModifiedLimit;
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
        final VirtualPath sourcePathAbsWithoutStats = sourceVfs.pwd().resolve(sourcePathRaw);
        // NOTE: this will throw an exception if the source dir/file does not exist
        final VirtualPath sourcePathAbs = sourceVfs.stat(sourcePathAbsWithoutStats);


        // its better to use absolute paths on source & target since the checksum methods on any host require full paths
        // the target will assume the "directory" value from the source, since in most cases it will be the exact same
        VirtualPath targetPathRaw = VirtualPath.parse(targetPath, sourcePathAbs.isDirectory());

        // if we're dealing with a NESTED target, the only difference is we need to build the target using the "name" of source
        if (mode == JsyncMode.NEST) {
            targetPathRaw = targetPathRaw.resolve(sourcePathAbs.getName(), sourcePathAbs.isDirectory());
        }

        final VirtualPath targetPathAbsWithoutStats = targetVfs.pwd().resolve(targetPathRaw);

        //
        // Create parent directories of target if necessary
        //

        // the target may or may not exist yet (which is not yet an error, so we use 'exists' for stats)
        VirtualPath targetPathAbs = targetVfs.exists(targetPathAbsWithoutStats);

        // if the target is missing, we will need to make sure it exists or its parent dirs exist
        if (targetPathAbs == null) {
            if (targetPathAbsWithoutStats.isDirectory()) {
                // we're dealing with directories, so we need to ensure this dir exists
                this.createDirectory(result, targetVfs, targetPathAbsWithoutStats, true, this.parents);
            } else {
                // we're dealing with files, so we just need to ensure the parent dir exists
                final VirtualPath parentDir = targetPathAbsWithoutStats.resolveParent();
                // check if it exists first, if not then we will create it
                if (targetVfs.exists(parentDir) == null) {
                    this.createDirectory(result, targetVfs, parentDir, true, this.parents);
                }
            }

            // we know the file doesn't exist yet, so we will use the path without stats
            targetPathAbs = targetPathAbsWithoutStats;
        } else {
            // we need to validate that we're not trying to sync a directory to a file or vice versa
            // this mimics rsync behavior where they will not automatically overwrite different file/dir types
            if (targetPathAbs.isDirectory() && !sourcePathAbs.isDirectory()) {
                throw new PathOverwriteException("Cannot overwrite target '" + targetPathAbs + "' since its a directory with source '" + sourcePathAbs + "' that is a file. If you intend to replace the directory with the file, you must manually delete the target directory first.");
            } else if (!targetPathAbs.isDirectory() && sourcePathAbs.isDirectory()) {
                throw new PathOverwriteException("Cannot overwrite target '" + targetPathAbs + "' since its a file with source '" + sourcePathAbs + "' that is a directory. If you intend to replace the file with the directory, you must manually delete the target file first.");
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
            // as we process files, only a subset may require more advanced methods of detecting whether they were modified
            // since that process could be "expensive", we keep a list of files on source/target that we will defer processing
            // until we have a chance to do some bulk processing of checksums, etc.
            this.syncDirectory(0, result, deferredFiles, sourceVfs, sourcePathAbs, targetVfs, targetPathAbs, checksum);
        } else {
            // we are only syncing a file, we may need to do some more expensive checks to determine if it needs to be updated
            this.syncFile(result, deferredFiles, sourceVfs, sourcePathAbs, targetVfs, targetPathAbs);
            this.syncDeferredFiles(result, deferredFiles, sourceVfs, targetVfs, checksum);
        }

        return result;
    }

    protected void syncFile(JsyncResult result, List<VirtualPathPair> deferredFiles, VirtualFileSystem sourceVfs, VirtualPath sourceFile, VirtualFileSystem targetVfs, VirtualPath targetFile) throws IOException {
        // we may know if the file definitely needs synced or just possibly needs synced
        final JsyncFileModified fileContentModified = this.isFileContentModified(sourceFile, targetFile);

        if (fileContentModified == JsyncFileModified.YES) {
            // we can immediately sync the file content and stats w/o deferring processing
            this.syncFileContent(result, sourceVfs, sourceFile, targetVfs, targetFile, true);
        } else if (fileContentModified == JsyncFileModified.MAYBE) {
            // defer the sync of this file until we have a chance to do more expensive checks in bulk
            // we will need to calculate checksums for both source and target files
            deferredFiles.add(new VirtualPathPair(sourceFile, targetFile));
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
            final JsyncFileModified fileContentModified = this.isFileContentModified(pair.getSource(), pair.getTarget());

            if (fileContentModified == JsyncFileModified.YES || fileContentModified == JsyncFileModified.MAYBE) {
                this.syncFileContent(result, sourceVfs, pair.getSource(), targetVfs, pair.getTarget(), true);
            } else {
                // the content was the same (awesome), but the stats may have changed and should be synced
                final JsyncStatsModified fileStatsModified = this.isFileStatsModified(pair.getSource(), pair.getTarget());
                if (fileStatsModified.isAny()) {
                    log.info("Updating file stats {}", pair.getTarget());
                    this.syncFileStats(result, pair.getSource(), targetVfs, pair.getTarget());
                }
            }
        }

        deferredFiles.clear();
    }

    protected void syncDirectory(int level, JsyncResult result, List<VirtualPathPair> deferredFiles, VirtualFileSystem sourceVfs, VirtualPath sourcePath, VirtualFileSystem targetVfs, VirtualPath targetPath, Checksum checksum) throws IOException {
        // we need a list of files in both directories, so we can see what to add/delete
        final List<VirtualPath> sourceChildPaths = sourceVfs.ls(sourcePath);
        final List<VirtualPath> targetChildPaths = targetVfs.ls(targetPath);

        // its better to work with all dirs first, then files, so we sort the files before we process them
        this.sortPaths(sourceChildPaths);
        this.sortPaths(targetChildPaths);

        // calculate paths new / changed / same
        for (VirtualPath sourceChildPath : sourceChildPaths) {
            // find a matching target path entirely by name
            VirtualPath targetChildPath = targetChildPaths.stream()
                .filter(p -> targetVfs.areFileNamesEqual(p.getName(), sourceChildPath.getName()))
                .findFirst()
                .orElse(null);

            if (targetChildPath == null) {
                // target path does not exist, we need to create it as a directory or just sync the file
                if (sourceChildPath.isDirectory()) {
                    targetChildPath = targetPath.resolve(sourceChildPath.getName(), true, null);
                    this.createDirectory(result, targetVfs, targetChildPath, false, false);
                } else {
                    targetChildPath = targetPath.resolve(sourceChildPath.getName(), false, null);
                    this.syncFile(result, deferredFiles, sourceVfs, sourceChildPath, targetVfs, targetChildPath);
                }
            } else {
                // if the target path exists, we need to check if it needs to be updated
                // is there a file/dir mismatch?
                if (sourceChildPath.isDirectory() && !targetChildPath.isDirectory()) {
                    log.warn("Source '{}' is a directory, target '{}' is a file!", sourceChildPath, targetChildPath);
                    if (!this.force) {
                        throw new PathOverwriteException("Cannot overwrite target '" + targetChildPath + "' since its a directory with source '" + sourceChildPath + "' that is a file. If you intend to replace the directory with the file (you can use the 'force' option to do this)");
                    }

                    // delete the target file
                    log.info("Deleting file {}", targetChildPath);
                    targetVfs.rm(targetChildPath);
                    result.incrementFilesDeleted();

                    // create the target dir new
                    targetChildPath = targetPath.resolve(sourceChildPath.getName(), true, null);
                    this.createDirectory(result, targetVfs, targetChildPath, false, false);

                } else if (!sourceChildPath.isDirectory() && targetChildPath.isDirectory()) {
                   log.warn("Source '{}' is a file, target '{}' is a directory!", sourceChildPath, targetChildPath);
                    if (!this.force) {
                        throw new PathOverwriteException("Cannot overwrite target '" + targetChildPath + "' since its a file with source '" + sourceChildPath + "' that is a directory. If you intend to replace the file with the directory (you can use the 'force' option to do this)");
                    }

                    // delete the target dir
                    this.deleteDirectory(0, result, targetVfs, targetChildPath);

                    // sync the target child path
                    targetChildPath = targetPath.resolve(sourceChildPath.getName(), false, null);
                    this.syncFile(result, deferredFiles, sourceVfs, sourceChildPath, targetVfs, targetChildPath);

                } else if (sourceChildPath.isDirectory() && targetChildPath.isDirectory()) {
                    // both are directories, nothing for us to do (will sync them later in this method)
                } else {
                    this.syncFile(result, deferredFiles, sourceVfs, sourceChildPath, targetVfs, targetChildPath);
                }
            }

            if (sourceChildPath.isDirectory()) {
                this.syncDirectory(level+1, result, deferredFiles, sourceVfs, sourceChildPath, targetVfs, targetChildPath, checksum);
            }
        }


        // handle existing files that may have been modified, but we want to batch as many as possible, but not wait too long, otherwise the array will get massive
        if (level == 0 || deferredFiles.size() >= this.maxFilesMaybeModifiedLimit) {
            this.syncDeferredFiles(result, deferredFiles, sourceVfs, targetVfs, checksum);
        }

        if (this.delete) {
            // calculate paths deleted
            for (VirtualPath targetChildPath : targetChildPaths) {
                // find a matching source path entirely by name
                final VirtualPath sourceChildPath = sourceChildPaths.stream()
                    .filter(p -> sourceVfs.areFileNamesEqual(p.getName(), targetChildPath.getName()))
                    .findFirst()
                    .orElse(null);

                if (sourceChildPath == null) {
                    if (targetChildPath.isDirectory()) {
                        deleteDirectory(0, result, targetVfs, targetChildPath);
                    } else {
                        log.info("Deleting file {}", targetChildPath);
                        targetVfs.rm(targetChildPath);
                        result.incrementFilesDeleted();
                    }
                }
            }
        }
    }

    protected JsyncFileModified isFileContentModified(VirtualPath sourceFile, VirtualPath targetFile) throws IOException {
        // source "stats" MUST exist
        Objects.requireNonNull(sourceFile, "sourceFile cannot be null");

        if (sourceFile.getStats() == null) {
            log.error("Source file {} missing 'stats' (it must not exist yet on source)", sourceFile);
            throw new IllegalArgumentException("sourceFile must have a 'stats' object");
        }

        // if the targetFile "stats" are null then we know it must not even exist yet
        if (targetFile.getStats() == null) {
            log.trace("Target file {} missing 'stats' (new file)", targetFile);
            return JsyncFileModified.YES;
        }

        // are file sizes different? if they are then this is a cheap way of figuring out a sync is needed
        if (sourceFile.getStats().getSize() != targetFile.getStats().getSize()) {
            log.trace("Source file {} size {} != target size {} (modified file)", sourceFile, sourceFile.getStats().getSize(), targetFile.getStats().getSize());
            return JsyncFileModified.YES;
        }

        // if we have "cksum" values on both sides, we can compare those
        if (sourceFile.getStats().getCksum() != null && targetFile.getStats().getCksum() != null) {
            if (!sourceFile.getStats().getCksum().equals(targetFile.getStats().getCksum())) {
                log.trace("Source file {} cksum {} != target chksum {} (modified file)", sourceFile, sourceFile.getStats().getCksum(), targetFile.getStats().getCksum());
                return JsyncFileModified.YES;
            } else {
                // we know for sure the file isn't modified
                return JsyncFileModified.NO;
            }
        }

        // if we have "md5" values on both sides, we can compare those
        if (sourceFile.getStats().getMd5() != null && targetFile.getStats().getMd5() != null) {
            if (!sourceFile.getStats().getMd5().equalsIgnoreCase(targetFile.getStats().getMd5())) {
                log.trace("Source file {} md5 {} != target md5 {} (modified file)", sourceFile, sourceFile.getStats().getMd5(), targetFile.getStats().getMd5());
                return JsyncFileModified.YES;
            } else {
                // we know for sure the file isn't modified
                return JsyncFileModified.NO;
            }
        }

        // if we have "sha1" values on both sides, we can compare those
        if (sourceFile.getStats().getSha1() != null && targetFile.getStats().getSha1() != null) {
            if (!sourceFile.getStats().getSha1().equalsIgnoreCase(targetFile.getStats().getSha1())) {
                log.trace("Source file {} sha1 {} != target sha1 {} (modified file)", sourceFile, sourceFile.getStats().getSha1(), targetFile.getStats().getSha1());
                return JsyncFileModified.YES;
            } else {
                // we know for sure the file isn't modified
                return JsyncFileModified.NO;
            }
        }

        // if we can take modified timestamps into account, this is a cheap way of figuring out a file changed
        if (!this.ignoreTimes) {
            // due to lack of millis precision in filesystems, we need to allow a larger delta of difference
            if (Math.abs(sourceFile.getStats().getModifiedTime() - targetFile.getStats().getModifiedTime()) > 2000L) {
                log.trace("Source file {} size {} != target size {} (maybe modified file)", sourceFile, sourceFile.getStats().getSize(), targetFile.getStats().getSize());
                return JsyncFileModified.MAYBE;
            } else {
                // The timestamps match and the file sizes match, so we know the file hasn't likely been modified
                // on either side.  However, if you're paranoid, the user could force the use of checksums by ignoring times
                return JsyncFileModified.NO;
            }
        }

        // file may be modified, no checksums were evaluated, or ignoreTimes was specified, and all we know then is
        // that the file sizes match each other, but nothing else
        return JsyncFileModified.MAYBE;
    }

    protected JsyncStatsModified isFileStatsModified(VirtualPath sourceFile, VirtualPath targetFile) throws IOException {
        // source "stats" MUST exist
        Objects.requireNonNull(sourceFile, "sourceFile cannot be null");

        if (sourceFile.getStats() == null) {
            throw new IllegalArgumentException("sourceFile must have a 'stats' object");
        }

        if (targetFile.getStats() == null) {
            // everything needs modified
            return new JsyncStatsModified(true, true, true);
        }

        boolean ownership = false;          // not supported yet
        boolean permissions = false;        // not supported yet
        boolean timestamps = false;

        // due to lack of millis precision in filesystems, we need to allow a larger delta of difference
        if (Math.abs(sourceFile.getStats().getModifiedTime() - targetFile.getStats().getModifiedTime()) > 2000L) {
            timestamps = true;
        }

        return new JsyncStatsModified(ownership, permissions, timestamps);
    }

    protected void syncFileContent(JsyncResult result, VirtualFileSystem sourceVfs, VirtualPath sourceFile, VirtualFileSystem targetVfs, VirtualPath targetFile, boolean includeStats) throws IOException {
        // if the target file has no "stats", then we have no info on it yet, and know we're going to create it fresh
        if (targetFile.getStats() == null) {
            log.info("Creating file: {}", targetFile);
        } else {
            log.info("Updating file: {}", targetFile);
        }

        // transfer the file
        try (StreamableInput input = sourceVfs.readFile(sourceFile, this.progress)) {
            targetVfs.writeFile(input, targetFile, this.progress);
        }

        // update results after we know the operation was successful
        if (targetFile.getStats() == null) {
            result.incrementFilesCreated();
        } else {
            result.incrementFilesUpdated();
        }

        if (includeStats) {
            this.syncFileStats(result, sourceFile, targetVfs, targetFile);
        }
    }

    protected void syncFileStats(JsyncResult result, VirtualPath sourceFile, VirtualFileSystem targetVfs, VirtualPath targetFile) throws IOException {
        // update the attributes with the source
        // TODO: we need to figure out what all we want to update in one fell swoop, which could include uid/gid, perms, and times
        // we'll want to build a "stats" object that possibly decides what all we want to update
        targetVfs.updateStat(targetFile, sourceFile.getStats());

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
        log.info("Creating dir {}", path);
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
            log.debug("Deleting dir {}", path);
        } else {
            log.info("Deleting dir {}", path);
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

            log.info("Detected {} checksum supported on source={}, target={}", preferredChecksum, sourceSupported, targetSupported);

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
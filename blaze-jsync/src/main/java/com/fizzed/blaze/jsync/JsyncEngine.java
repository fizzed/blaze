package com.fizzed.blaze.jsync;

import com.fizzed.blaze.util.StreamableInput;
import com.fizzed.blaze.vfs.LocalVirtualFileSystem;
import com.fizzed.blaze.vfs.ParentDirectoryMissingException;
import com.fizzed.blaze.vfs.VirtualFileSystem;
import com.fizzed.blaze.vfs.VirtualPath;
import com.fizzed.blaze.vfs.util.VirtualPathPair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.*;

import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toList;

public class JsyncEngine {
    static private final Logger log = LoggerFactory.getLogger(JsyncEngine.class);

    // options for syncing, try to mimic defaults for how rsync works
    final private List<Checksum> preferredChecksums;
    private boolean delete;
    private boolean parents;
    private boolean progress;

    public JsyncEngine() {
        this.delete = false;
        this.progress = false;
        this.parents = false;
        this.preferredChecksums = new ArrayList<>(asList(Checksum.CK, Checksum.MD5));
    }

    public boolean isDelete() {
        return delete;
    }

    public JsyncEngine setDelete(boolean delete) {
        this.delete = delete;
        return this;
    }

    public boolean isParents() {
        return parents;
    }

    public JsyncEngine setParents(boolean parents) {
        this.parents = parents;
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

    public void sync(Path sourcePath, Path targetPath, JsyncMode mode) throws IOException {
        // local -> local
        final LocalVirtualFileSystem localVfs = LocalVirtualFileSystem.open();
        this.sync(localVfs, sourcePath.toString(), localVfs, targetPath.toString(), mode);
    }

    public void sync(VirtualFileSystem sourceVfs, String sourcePath, VirtualFileSystem targetVfs, String targetPath, JsyncMode mode) throws IOException {
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
                this.createDirectory(targetVfs, targetPathAbsWithoutStats, true, this.parents);
            } else {
                // we're dealing with files, so we just need to ensure the parent dir exists
                final VirtualPath parentDir = targetPathAbsWithoutStats.resolveParent();
                // check if it exists first, if not then we will create it
                if (targetVfs.exists(parentDir) == null) {
                    this.createDirectory(targetVfs, parentDir, true, this.parents);
                }
            }

            // we know the file doesn't exist yet, so we will use the path without stats
            targetPathAbs = targetPathAbsWithoutStats;
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

        if (sourcePathAbs.isDirectory()) {
            // as we process files, only a subset may require more advanced methods of detecting whether they were modified
            // since that process could be "expensive", we keep a list of files on source/target that we will defer processing
            // until we have a chance to do some bulk processing of checksums, etc.
            final List<VirtualPathPair> filesMaybeModified = new ArrayList<>();

            this.syncDirectory(0, filesMaybeModified, sourceVfs, sourcePathAbs, targetVfs, targetPathAbs, checksum);
        } else {
            // we are only syncing a file, we may need to do some more expensive checks to determine if it needs to be updated
            this.syncFile(sourceVfs, sourcePathAbs, targetVfs, targetPathAbs);
        }
    }



    protected void syncDirectory(final int level, final List<VirtualPathPair> filesMaybeModified, VirtualFileSystem sourceVfs, VirtualPath sourcePath, VirtualFileSystem targetVfs, VirtualPath targetPath, Checksum checksum) throws IOException {
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
                    createDirectory(targetVfs, targetChildPath, false, false);
                } else {
                    targetChildPath = targetPath.resolve(sourceChildPath.getName(), false, null);
                    syncFile(sourceVfs, sourceChildPath, targetVfs, targetChildPath);
                }
            } else {
                // target path exists, we need to check if it needs to be updated
                // is there a file/dir mismatch?
                if (sourceChildPath.isDirectory() && !targetChildPath.isDirectory()) {
                    log.info("Source is a directory, target is a file! (target {})", targetChildPath);
                    // TODO: delete the target file and re-crate it as a directory
                    log.error("TODO: delete the target file, then create it as a directory");
                } else if (!sourceChildPath.isDirectory() && targetChildPath.isDirectory()) {
                    log.info("Source is a file, target is a directory! (target {})", targetChildPath);
                    // TODO: delete the target file and re-crate it as a directory
                    log.error("TODO: delete the target dir, and transfer the file");
                } else if (sourceChildPath.isDirectory() && targetChildPath.isDirectory()) {
                    // both are directories, nothing for us to do (will sync them later in this method)
                } else {
                    // the file may need synced, and its possible we don't need anything expensive here to check, if
                    // something as easy as the file size has changed
                    boolean wasSynced = this.syncFile(sourceVfs, sourceChildPath, targetVfs, targetChildPath);

                    if (!wasSynced) {
                        // we will need more "expensive" checks to determine if this file needs synced
                        filesMaybeModified.add(new VirtualPathPair(sourceChildPath, targetChildPath));
                    }
                }
            }

            if (sourceChildPath.isDirectory()) {
                syncDirectory(level+1, filesMaybeModified, sourceVfs, sourceChildPath, targetVfs, targetChildPath, checksum);
            }
        }

//        log.info("filesMaybeModified size now {}", filesMaybeModified.size());

        // handle existing files that may have been modified, but we want to batch as many as possible, but not wait
        // too long, otherwise the array will get massive
        if (filesMaybeModified.size() > 256) {
//            log.debug("Calculating checksums...");

            // we need calculate checksums for source and target files
            final List<VirtualPath> sourceFiles = filesMaybeModified.stream()
                .map(VirtualPathPair::getSource)
                .collect(toList());

//            log.debug("Calculating checksums for {} source files", sourceFiles.size());
            sourceVfs.checksums(checksum, sourceFiles);

            final List<VirtualPath> targetFiles = filesMaybeModified.stream()
                .map(VirtualPathPair::getTarget)
                .collect(toList());

//            log.debug("Calculating checksums for {} target files", targetFiles.size());
            targetVfs.checksums(checksum, targetFiles);

            for (VirtualPathPair pair : filesMaybeModified) {
                syncFile(sourceVfs, pair.getSource(), targetVfs, pair.getTarget());
            }

            filesMaybeModified.clear();
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
                        deleteDirectory(0, targetVfs, targetChildPath);
                    } else {
                        targetVfs.rm(targetChildPath);
                        log.info("Deleted file: {}", targetChildPath);
                    }
                }
            }
        }
    }

    protected boolean isFileContentModified(VirtualPath sourceFile, VirtualPath targetFile) throws IOException {
        // source "stats" MUST exist
        Objects.requireNonNull(sourceFile, "sourceFile cannot be null");

        if (sourceFile.getStats() == null) {
            log.error("Source file {} missing 'stats' (it must not exist yet on source)", sourceFile);
            throw new IllegalArgumentException("sourceFile must have a 'stats' object");
        }

        // if the targetFile "stats" are null then we know it must not even exist yet
        if (targetFile.getStats() == null) {
            log.trace("Target file {} missing 'stats' (it must not exist yet on target)", targetFile);
            return true;
        }

        // do the file sizes match?
        if (sourceFile.getStats().getSize() != targetFile.getStats().getSize()) {
            log.trace("Source file {} size {} != target size {} (so is modified)", sourceFile, sourceFile.getStats().getSize(), targetFile.getStats().getSize());
            return true;
        }

        // if we have "cksum" values on both sides, we can compare those
        if (sourceFile.getStats().getCksum() != null && targetFile.getStats().getCksum() != null
                && !sourceFile.getStats().getCksum().equals(targetFile.getStats().getCksum())) {
            log.trace("Source file {} cksum {} != target chksum {} (so is modified)", sourceFile, sourceFile.getStats().getCksum(), targetFile.getStats().getCksum());
            return true;
        }

        // if we have "md5" values on both sides, we can compare those
        if (sourceFile.getStats().getMd5() != null && targetFile.getStats().getMd5() != null
            && !sourceFile.getStats().getMd5().equalsIgnoreCase(targetFile.getStats().getMd5())) {
            log.trace("Source file {} md5 {} != target md5 {} (so is modified)", sourceFile, sourceFile.getStats().getMd5(), targetFile.getStats().getMd5());
            return true;
        }

        // if we have "sha1" values on both sides, we can compare those
        if (sourceFile.getStats().getSha1() != null && targetFile.getStats().getSha1() != null
            && !sourceFile.getStats().getSha1().equalsIgnoreCase(targetFile.getStats().getSha1())) {
            log.trace("Source file {} sha1 {} != target sha1 {} (so is modified)", sourceFile, sourceFile.getStats().getSha1(), targetFile.getStats().getSha1());
            return true;
        }

        // file must not be modified
        return false;
    }

    protected boolean syncFile(VirtualFileSystem sourceVfs, VirtualPath sourceFile, VirtualFileSystem targetVfs, VirtualPath targetFile) throws IOException {
        if (!this.isFileContentModified(sourceFile, targetFile)) {
            return false;
        }

        // if target file has no "stats", then we have no info on it yet, and know we're going to create it fresh
        if (targetFile.getStats() == null) {
            log.info("Create file: {}", targetFile);
        } else {
            log.info("Update file: {}", targetFile);
        }

        // transfer the file
        try (StreamableInput input = sourceVfs.readFile(sourceFile, this.progress)) {
            targetVfs.writeFile(input, targetFile, this.progress);
        }

        return true;
    }

    protected void createDirectory(VirtualFileSystem vfs, VirtualPath path, boolean verifyParentExists, boolean parents) throws IOException {
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
                }
            }
        } else if (verifyParentExists) {
            // if parents is disabled, we want to make sure the parent dir exists, so we can throw a better exception
            VirtualPath parentPath = path.resolveParent();
            if (parentPath != null && vfs.exists(parentPath) == null) {
                throw new ParentDirectoryMissingException("Unable to create directory '" + path + "' since its parent directory does not exist (did you forget to use 'parents' option?)");
            }
        }

        // finally we can create the directory
        log.info("Creating dir {}", path);
        vfs.mkdir(path);
    }

    protected void deleteDirectory(int level, VirtualFileSystem vfs, VirtualPath path) throws IOException {
        // we need a list of files in both directories, since we'll need to recurse thru dirs
        final List<VirtualPath> childPaths = vfs.ls(path);
        sortPaths(childPaths);

        for (VirtualPath childPath : childPaths) {
            if (childPath.isDirectory()) {
                deleteDirectory(level+1, vfs, childPath);     // do not log this, that will happen in the below statement via recursion
            } else {
                log.debug("Deleting file {}", childPath);
                vfs.rm(childPath);
            }
        }

        // finally we can delete the directory, if level 0, we log as info, but anything else is considered debugging
        if (level > 0) {
            log.debug("Deleting dir {}", path);
        } else {
            log.info("Deleting dir {}", path);
        }
        vfs.rmdir(path);
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
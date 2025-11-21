package com.fizzed.blaze.jsync;

import com.fizzed.blaze.util.StreamableInput;
import com.fizzed.blaze.vfs.VirtualFileSystem;
import com.fizzed.blaze.vfs.VirtualPath;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class JsyncEngine {
    static private final Logger log = LoggerFactory.getLogger(JsyncEngine.class);

    // options for syncing, try to mimic defaults for how rsync works
    private boolean delete;

    public JsyncEngine() {
        this.delete = false;
    }

    public boolean isDelete() {
        return delete;
    }

    public JsyncEngine delete(boolean delete) {
        this.delete = delete;
        return this;
    }

    public void sync(VirtualFileSystem sourceFS, String sourcePath, VirtualFileSystem targetFS, String targetPath) throws IOException {
        // source MUST exist
        final VirtualPath sourcePathWithoutStats = VirtualPath.parse(sourcePath, null);
//        final VirtualPath sourcePathWithStats = sourceFS.stat(sourcePathWithoutStats);
        final VirtualPath sourcePathWithStats = sourceFS.stat(sourceFS.pwd().resolve(sourcePathWithoutStats));              // use absolute version?

        // target MAY or MAY NOT exist
//        final VirtualPath targetPathWithoutStats = VirtualPath.parse(targetPath, null);
        final VirtualPath targetPathWithoutStats = targetFS.pwd().resolve(VirtualPath.parse(targetPath, null));     // use absolute version?
        VirtualPath targetPathWithStats = null;
        try {
            targetPathWithStats = targetFS.stat(targetPathWithoutStats);
        } catch (FileNotFoundException e) {
            log.info("Target dir not found, creating {}", targetPath);
            targetFS.mkdir(targetPathWithoutStats);
            targetPathWithStats = targetFS.stat(targetPathWithoutStats);
        }

        log.info("Syncing {} -> {} (delete={})", sourcePathWithStats, targetPathWithStats, this.delete);

        syncDirectory(sourceFS, sourcePathWithStats, targetFS, targetPathWithStats, this.delete);
    }

    protected void syncDirectory(VirtualFileSystem sourceFS, VirtualPath sourcePath, VirtualFileSystem targetFS, VirtualPath targetPath, boolean delete) throws IOException {
        // we need a list of files in both directories, so we can see what to add/delete
        final List<VirtualPath> sourceChildPaths = sourceFS.ls(sourcePath);
        final List<VirtualPath> targetChildPaths = targetFS.ls(targetPath);

        // its better to work with all dirs first, then files, so we sort the files before we process them
        this.sortPaths(sourceChildPaths);
        this.sortPaths(targetChildPaths);

        // as we process files, only a subset may require more advanced methods of detecting whether they were modified
        // since that process could be "expensive", we keep a list of files on source/target that we will defer processing
        // until we have a chance to do some bulk processing of checksums, etc.
        final List<VirtualPath> sourceFilesMaybeModified = new ArrayList<>();
        final List<VirtualPath> targetFilesMaybeModified = new ArrayList<>();

        // calculate paths new / changed / same
        for (VirtualPath sourceChildPath : sourceChildPaths) {
            // find a matching target path entirely by name
            VirtualPath targetChildPath = targetChildPaths.stream()
                .filter(p -> p.getName().equals(sourceChildPath.getName()))
                .findFirst()
                .orElse(null);

            if (targetChildPath == null) {
                // target path does not exist, we need to create it as a directory or just sync the file
                if (sourceChildPath.isDirectory()) {
                    targetChildPath = targetPath.resolve(sourceChildPath.getName(), true, null);
                    targetFS.mkdir(targetChildPath);
                    log.info("Created dir: {}", targetChildPath);
                } else {
                    targetChildPath = targetPath.resolve(sourceChildPath.getName(), false, null);
                    syncFile(sourceFS, sourceChildPath, targetFS, targetChildPath);
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
                    boolean wasSynced = this.syncFile(sourceFS, sourceChildPath, targetFS, targetChildPath);

                    if (!wasSynced) {
                        // we will need more "expensive" checks to determine if this file needs synced
                        sourceFilesMaybeModified.add(sourceChildPath);
                        targetFilesMaybeModified.add(targetChildPath);
                    }
                }
            }

            if (sourceChildPath.isDirectory()) {
                syncDirectory(sourceFS, sourceChildPath, targetFS, targetChildPath, delete);
            }
        }

        // now handle existing files that may have been modified
        if (!sourceFilesMaybeModified.isEmpty()) {
            // we need checksums now
            sourceFS.cksums(sourceFilesMaybeModified);
            targetFS.cksums(targetFilesMaybeModified);

            for (int i = 0; i < targetFilesMaybeModified.size(); i++) {
                final VirtualPath sourceFile = sourceFilesMaybeModified.get(i);
                final VirtualPath targetFile = targetFilesMaybeModified.get(i);
                syncFile(sourceFS, sourceFile, targetFS, targetFile);
            }
        }

        if (delete) {
            // calculate paths deleted
            for (VirtualPath targetChildPath : targetChildPaths) {
                // find a matching source path entirely by name
                final VirtualPath sourceChildPath = sourceChildPaths.stream()
                    .filter(p -> p.getName().equals(targetChildPath.getName()))
                    .findFirst()
                    .orElse(null);

                if (sourceChildPath == null) {
                    if (targetChildPath.isDirectory()) {
                        deleteDirectory(targetFS, targetChildPath);
                    } else {
                        targetFS.rm(targetChildPath);
                        log.info("Deleted file: {}", targetChildPath);
                    }
                }
            }
        }
    }

    protected boolean isFileContentModified(VirtualFileSystem sourceFS, VirtualPath sourceFile, VirtualFileSystem targetFS, VirtualPath targetFile) throws IOException {
        // source "stats" MUST exist
        Objects.requireNonNull(sourceFile, "sourceFile cannot be null");
        Objects.requireNonNull(sourceFile.getStats(), "sourceFile.getStats() cannot be null");

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

        // file must not be modified
        return false;
    }

    protected boolean syncFile(VirtualFileSystem sourceFS, VirtualPath sourceFile, VirtualFileSystem targetFS, VirtualPath targetFile) throws IOException {
        if (!this.isFileContentModified(sourceFS, sourceFile, targetFS, targetFile)) {
            return false;
        }

        // if target file has no "stats", then we have no info on it yet, and know we're going to create it fresh
        if (targetFile.getStats() == null) {
            log.info("Create file: {}", targetFile);
        } else {
            log.info("Update file: {}", targetFile);
        }

        // transfer the file
        try (StreamableInput input = sourceFS.readFile(sourceFile)) {
            targetFS.writeFile(input, targetFile);
        }

        return true;
    }

    protected void deleteDirectory(VirtualFileSystem fs, VirtualPath path) throws IOException {
        // we need a list of files in both directories, since we'll need to recurse thru dirs
        final List<VirtualPath> childPaths = fs.ls(path);
        sortPaths(childPaths);

        for (VirtualPath childPath : childPaths) {
            if (childPath.isDirectory()) {
                deleteDirectory(fs, childPath);     // do not log this, that will happen in the below statement via recursion
            } else {
                fs.rm(childPath);
                log.info("Deleted file: {}", childPath);
            }
        }

        // finally we can delete the directory
        fs.rmdir(path);
        log.info("Deleted dir: {}", path);
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
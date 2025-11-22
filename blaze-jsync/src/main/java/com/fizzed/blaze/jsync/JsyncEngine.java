package com.fizzed.blaze.jsync;

import com.fizzed.blaze.util.StreamableInput;
import com.fizzed.blaze.vfs.VirtualFileSystem;
import com.fizzed.blaze.vfs.VirtualPath;
import com.fizzed.blaze.vfs.util.VirtualPathPair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.*;

import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toList;

public class JsyncEngine {
    static private final Logger log = LoggerFactory.getLogger(JsyncEngine.class);

    // options for syncing, try to mimic defaults for how rsync works
    final private List<Checksum> preferredChecksums;
    private boolean delete;
    private boolean progress;

    public JsyncEngine() {
        this.delete = false;
        this.progress = false;
        this.preferredChecksums = new ArrayList<>(asList(Checksum.CK, Checksum.MD5));
    }

    public boolean isDelete() {
        return delete;
    }

    public JsyncEngine setDelete(boolean delete) {
        this.delete = delete;
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

        // negotiate the checksums to use
        Checksum checksum = null;
        List<Checksum> sourceChecksumsSupported = new ArrayList<>();
        List<Checksum> targetChecksumsSupported = new ArrayList<>();

        for (Checksum preferredChecksum : this.preferredChecksums) {
            log.info("Detecting if {} checksum is supported on both source & target", preferredChecksum);

            // check supported checksums, keep a tally of which are supported by both sides, so we can log them out
            boolean sourceSupported = sourceFS.isSupported(preferredChecksum);
            if (sourceSupported) {
                sourceChecksumsSupported.add(preferredChecksum);
            }

            boolean targetSupported = targetFS.isSupported(preferredChecksum);
            if (targetSupported) {
                targetChecksumsSupported.add(preferredChecksum);
            }

            log.info("Supported on source={}, target={}", sourceSupported, targetSupported);

            if (sourceSupported && targetSupported) {
                checksum = preferredChecksum;
                break;
            }
        }

        if (checksum == null) {
            throw new IOException("Unable to find a checksum that is supported by both source and target. " +
                "Source virtual filesystem " + sourceFS.getName() + " supports checksums " + sourceChecksumsSupported
                + " and target virtual filesystem " + targetFS.getName() + " supports checksums " + targetChecksumsSupported);
        }

        log.info("Syncing {} -> {} (checksum={}, delete={})", sourcePathWithStats, targetPathWithStats, checksum, this.delete);

        // as we process files, only a subset may require more advanced methods of detecting whether they were modified
        // since that process could be "expensive", we keep a list of files on source/target that we will defer processing
        // until we have a chance to do some bulk processing of checksums, etc.
        final List<VirtualPathPair> filesMaybeModified = new ArrayList<>();

        syncDirectory(0, filesMaybeModified, sourceFS, sourcePathWithStats, targetFS, targetPathWithStats, checksum);
    }

    protected void syncDirectory(final int level, final List<VirtualPathPair> filesMaybeModified, VirtualFileSystem sourceFS, VirtualPath sourcePath, VirtualFileSystem targetFS, VirtualPath targetPath, Checksum checksum) throws IOException {
        // we need a list of files in both directories, so we can see what to add/delete
        final List<VirtualPath> sourceChildPaths = sourceFS.ls(sourcePath);
        final List<VirtualPath> targetChildPaths = targetFS.ls(targetPath);

        // its better to work with all dirs first, then files, so we sort the files before we process them
        this.sortPaths(sourceChildPaths);
        this.sortPaths(targetChildPaths);

        // calculate paths new / changed / same
        for (VirtualPath sourceChildPath : sourceChildPaths) {
            // find a matching target path entirely by name
            VirtualPath targetChildPath = targetChildPaths.stream()
                .filter(p -> targetFS.areFileNamesEqual(p.getName(), sourceChildPath.getName()))
                .findFirst()
                .orElse(null);

            if (targetChildPath == null) {
                // target path does not exist, we need to create it as a directory or just sync the file
                if (sourceChildPath.isDirectory()) {
                    targetChildPath = targetPath.resolve(sourceChildPath.getName(), true, null);
                    log.info("Creating dir: {}", targetChildPath);
                    targetFS.mkdir(targetChildPath);
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
                        filesMaybeModified.add(new VirtualPathPair(sourceChildPath, targetChildPath));
                    }
                }
            }

            if (sourceChildPath.isDirectory()) {
                syncDirectory(level+1, filesMaybeModified, sourceFS, sourceChildPath, targetFS, targetChildPath, checksum);
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
            sourceFS.checksums(checksum, sourceFiles);

            final List<VirtualPath> targetFiles = filesMaybeModified.stream()
                .map(VirtualPathPair::getTarget)
                .collect(toList());

//            log.debug("Calculating checksums for {} target files", targetFiles.size());
            targetFS.checksums(checksum, targetFiles);

            for (VirtualPathPair pair : filesMaybeModified) {
                syncFile(sourceFS, pair.getSource(), targetFS, pair.getTarget());
            }

            filesMaybeModified.clear();
        }

        if (delete) {
            // calculate paths deleted
            for (VirtualPath targetChildPath : targetChildPaths) {
                // find a matching source path entirely by name
                final VirtualPath sourceChildPath = sourceChildPaths.stream()
                    .filter(p -> sourceFS.areFileNamesEqual(p.getName(), targetChildPath.getName()))
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

        if (sourceFile.getStats() == null) {
            log.error("Source file {} missing 'stats' (it must not exist yet on source)", sourceFile);
            throw new IllegalArgumentException("sourceFile must have a 'stats' object");
        }


        //Objects.requireNonNull(sourceFile.getStats(), "sourceFile.getStats() cannot be null");

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
        try (StreamableInput input = sourceFS.readFile(sourceFile, this.progress)) {
            targetFS.writeFile(input, targetFile, this.progress);
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
package com.fizzed.blaze.vfs;

import java.util.Objects;

public class VirtualPath {

    private final String parentPath;
    private final String name;
    private final Boolean directory;
    private final VirtualStats stats;

    public VirtualPath(String parentPath, String name, Boolean directory, VirtualStats stats) {
        Objects.requireNonNull(name, "name cannot be null");
        this.parentPath = parentPath;
        this.name = name;
        this.directory = directory;
        this.stats = stats;
    }

    static public VirtualPath parse(String path, Boolean directory) {
        return parse(path, directory, null);
    }

    static public VirtualPath parse(String path, Boolean directory, VirtualStats stats) {
        Objects.requireNonNull(path, "path cannot be null");

        int lastSlashPos = path.lastIndexOf('/');

        if (lastSlashPos < 0) {
            // fully relative, no parent path
            return new VirtualPath(null, path, directory, null);
        } else {
            // split the parent path and name
            String parentPath = path.substring(0, lastSlashPos);
            String name = path.substring(lastSlashPos + 1);
            return new VirtualPath(parentPath, name, directory, stats);
        }
    }

    public String toFullPath() {
        if (this.parentPath == null) {
            return this.name;
        }
        // otherwise, return the full name
        return this.parentPath + "/" + this.name;
    }

    public boolean isAbsolute() {
        return this.parentPath != null &&
            (this.parentPath.isEmpty() || this.parentPath.charAt(0) == '/');
    }

    public boolean isRelative() {
        return !this.isAbsolute();
    }

    public String getParentPath() {
        return this.parentPath;
    }

    public String getName() {
        return this.name;
    }

    /**
     * Determines whether this path represents a directory. It's possible its also null, which means its status of
     * being a directory is unknown.
     *
     * @return true if this path represents a directory, false otherwise. Null if its directory/file status is unknown.
     */
    public Boolean isDirectory() {
        return this.directory;
    }

    /**
     * Retrieves the VirtualStats object associated with this VirtualPath.
     *
     * @return the VirtualStats object containing information about size, modified time, and optionally checksum.
     *         Returns null if no stats are associated with this path.
     */
    public VirtualStats getStats() {
        return this.stats;
    }

    public VirtualPath resolve(String path, boolean directory) {
        return this.resolve(path, directory, null);
    }

    public VirtualPath resolve(String path, boolean directory, VirtualStats stats) {
        Objects.requireNonNull(path, "path cannot be null");

        VirtualPath otherPath = VirtualPath.parse(path, directory, stats);

        return this.resolve(otherPath);
    }

    public VirtualPath resolve(VirtualPath path) {
        Objects.requireNonNull(path, "path cannot be null");

        if (path.isAbsolute()) {
            return path;
        } else {
            // create a new full path
            String thisFullPath = this.toFullPath();
            String pathFullPath = path.toFullPath();
            String newFullPath = thisFullPath + "/" + pathFullPath;
            return VirtualPath.parse(newFullPath, path.isDirectory(), path.getStats());
        }
    }

    @Override
    public String toString() {
        return this.toFullPath();
    }

}

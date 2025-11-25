package com.fizzed.blaze.vfs;

import java.util.LinkedList;
import java.util.Objects;

public class VirtualPath {

    private final String parentPath;
    private final String name;
    private final String fullPath;
    private final Boolean directory;
    private final VirtualFileStat stat;

    public VirtualPath(String parentPath, String name, Boolean directory, VirtualFileStat stat) {
        Objects.requireNonNull(name, "name cannot be null");
        this.parentPath = parentPath;
        this.name = name;
        this.directory = directory;
        this.stat = stat;
        if (this.parentPath == null) {
            this.fullPath = name;
        } else {
            this.fullPath = parentPath + "/" + name;
        }
    }

    static public VirtualPath parse(String path) {
        return parse(path, null, null);
    }

    static public VirtualPath parse(String path, Boolean directory) {
        return parse(path, directory, null);
    }

    static public VirtualPath parse(String path, Boolean directory, VirtualFileStat stat) {
        Objects.requireNonNull(path, "path cannot be null");

        // normalize windows paths to use /'s to simplify logic
        path = path.replace('\\', '/');

        int lastSlashPos = path.lastIndexOf('/');

        if (lastSlashPos < 0) {
            // fully relative, no parent path
            return new VirtualPath(null, path, directory, null);
        } else {
            // split the parent path and name
            String parentPath = path.substring(0, lastSlashPos);
            String name = path.substring(lastSlashPos + 1);
            return new VirtualPath(parentPath, name, directory, stat);
        }
    }

    public VirtualPath resolveParent() {
        // is there a parent?
        if (this.parentPath == null || this.parentPath.isEmpty()) {
            return null;
        }

        // just parse the parent path, and it will always be a dir
        return parse(this.parentPath, true);
    }

    public String toFullPath() {
        return this.fullPath;
    }

    public boolean isAbsolute() {
        return this.parentPath != null &&
            (this.parentPath.isEmpty()                                                   // handles posix / case
                || this.parentPath.charAt(0) == '/'                                      // handles posix / case
                || (this.parentPath.length() > 1 && this.parentPath.charAt(1) == ':')   // handles windows
            );
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

    public boolean startsWith(VirtualPath path) {
        return this.fullPath.startsWith(path.toFullPath());
    }

    public boolean startsWith(String path) {
        return this.fullPath.startsWith(path);
    }

    /**
     * Retrieves the VirtualStats object associated with this VirtualPath.
     *
     * @return the VirtualStats object containing information about size, modified time, and optionally checksum.
     *         Returns null if no stats are associated with this path.
     */
    public VirtualFileStat getStat() {
        return this.stat;
    }

    public VirtualPath normalize() {
        final boolean isAbsolute = this.isAbsolute();

        String p = this.fullPath;
        String dl = null;
        // handle windows case, where we'll chop off drive letter and add it back
        if (this.fullPath.length() > 2 && this.fullPath.charAt(1) == ':') {
            dl = this.fullPath.substring(0, 2);
            p = this.fullPath.substring(2);
        }

        // 2. Split by slash
        String[] parts = p.split("/");

        // 3. Use a LinkedList as a stack to process parts
        LinkedList<String> stack = new LinkedList<>();

        for (String part : parts) {
            // Skip empty parts (caused by //) and current dir (.)
            if (part.isEmpty() || ".".equals(part)) {
                continue;
            }

            // Handle parent dir (..)
            if ("..".equals(part)) {
                if (!stack.isEmpty() && !stack.getLast().equals("..")) {
                    // If we have a path to go back from, pop it
                    stack.removeLast();
                } else if (!isAbsolute) {
                    // If it's relative (and stack is empty or has ..), we keep the ..
                    // Example: "../../file.txt" -> we must keep the dots
                    stack.add(part);
                }
                // If it IS absolute and stack is empty, we ignore the ..
                // (You can't go higher than root in Unix: /../ -> /)
            } else {
                // Regular filename/folder
                stack.add(part);
            }
        }

        // 4. Reassemble
        String normalized = String.join("/", stack);

        if (isAbsolute) {
            normalized =  "/" + normalized;
            if (dl != null) {
                normalized = dl + normalized;
            }
        } else {
            normalized = normalized.isEmpty() ? "." : normalized;
        }

        return VirtualPath.parse(normalized, this.directory, this.stat);
    }

    public VirtualPath resolve(String path, boolean directory) {
        return this.resolve(path, directory, null);
    }

    public VirtualPath resolve(String path, boolean directory, VirtualFileStat stats) {
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
            return VirtualPath.parse(newFullPath, path.isDirectory(), path.getStat());
        }
    }

    @Override
    public String toString() {
        return this.toFullPath();
    }

}

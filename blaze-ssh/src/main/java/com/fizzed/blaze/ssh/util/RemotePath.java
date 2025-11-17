package com.fizzed.blaze.ssh.util;

import com.fizzed.blaze.ssh.impl.PathHelper;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;

/**
 * Utility class for working with "remote" Paths, where they may differ from the local system, be it with file
 * separators (e.g. working on linux, remote system windows, or vice versa), or perhaps other interesting issues like
 * the "remote" system returning unix-style paths, but with invalid chars (e.g. "/C:/" as root on SFTP running on windows)
 */
public class RemotePath {

    final private String separator;
    final private String absolutePath;

    private RemotePath(String separator, String absolutePath) {
        this.separator = separator;
        this.absolutePath = absolutePath;
    }

    public Path toLocalPath(String remotePath) {
        // if it's a relative path, the conversion is pretty straightforward
        if (isRelative(remotePath)) {
            // if the separators are the same, no conversion is needed
            if (Objects.equals(this.separator, File.separator)) {
                return Paths.get(remotePath);
            } else {
                // we need to convert the separators, then return the path
                return Paths.get(remotePath.replace(this.separator, File.separator));
            }
        }


        return null;
        /*if (remotePath.startsWith("/")) {
            // e.g. /home/builder,
            // unix-style path
            separator =  "/";
            absolutePath = "/";
        } else if (path.length() > 3 && path.charAt(1) == ':' && path.charAt(2) == '\\') {
            separator =  "\\";
            absolutePath = path.substring(0, 3);
        }

*/
    }

    static public boolean isRelative(String path) {
        return !isAbsolute(path);
    }

    static public boolean isAbsolute(String path) {
        // covers both unix and/or windows style paths
        return path.startsWith("/")
            || path.length() > 3 && path.charAt(1) == ':' && path.charAt(2) == '\\';
    }


    static public RemotePath create(String path) {
        String separator = null;
        String absolutePath = null;

        if (path.startsWith("/")) {
            // an absolute, unix-style path?
            // e.g. /home/builder,
            // unix-style path
            separator =  "/";
            absolutePath = "/";
        } else if (path.length() > 3 && path.charAt(1) == ':' && path.charAt(2) == '\\') {
            // an absolute, windows-style path?
            separator =  "\\";
            absolutePath = path.substring(0, 3);
        } else if (path.contains("\\")) {
            // a relative, windows-style path
            separator = "\\";
            absolutePath = null;
        } else {
            // assume a relative, unix-style path
            separator = "/";
            absolutePath = null;
        }

        return new RemotePath(separator, absolutePath);
    }




    /*static public Path toPath(String path) {

    }

    static public String toString(Path path) {
        // TODO: figure out path of remote system?
        // for now assume its linux

        char pathSep = '/';

        StringBuilder s = new StringBuilder();

        // is absolute?  normal path.isAbsolute() doesn't work since local FS
        // may not match remote FS
        if (path.startsWith("\\") || path.startsWith("/")) {
            s.append(pathSep);
        }

        int count = path.getNameCount();
        for (int i = 0; i < count; i++) {
            Path name = path.getName(i);
            if (i != 0) {
                s.append(pathSep);
            }
            s.append(name.toString());
        }

        return s.toString();
    }*/


}
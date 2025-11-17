package com.fizzed.blaze.ssh.util;

import java.nio.file.Path;

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

    public Path toPath(String remotePath) {
        if (remotePath.startsWith("/")) {
            // e.g. /home/builder,
            // unix-style path
            separator =  "/";
            absolutePath = "/";
        } else if (path.length() > 3 && path.charAt(1) == ':' && path.charAt(2) == '\\') {
            separator =  "\\";
            absolutePath = path.substring(0, 3);
        }


    }

    static public RemotePath create(String path) {
        String separator = null;
        String absolutePath = null;

        if (path.startsWith("/")) {
            // e.g. /home/builder,
            // unix-style path
            separator =  "/";
            absolutePath = "/";
        } else if (path.length() > 3 && path.charAt(1) == ':' && path.charAt(2) == '\\') {
            separator =  "\\";
            absolutePath = path.substring(0, 3);
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
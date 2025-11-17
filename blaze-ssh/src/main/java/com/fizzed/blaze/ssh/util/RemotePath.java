package com.fizzed.blaze.ssh.util;

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

    public enum Spec {
        // "/" style paths
        POSIX("/"),
        // "<drive>:\\" style paths
        WINDOWS("\\"),
        // /<drive>:/ style paths
        WINDOWS_POSIX("/"),
        // /cygwin/<drive>/ style paths
        WINDOWS_CYGWIN("/");

        final private String separator;

        Spec(String separator) {
            this.separator = separator;
        }

        public String getSeparator() {
            return separator;
        }

        public String getWindowsDriveLetter(String path) {
            switch (this) {
                case WINDOWS:
                    if (path.length() < 3) {
                        throw new IllegalArgumentException("Path must be at least 3 characters long to get a windows drive letter [path was '" + path + "']");
                    }
                    // e.g. C:\path -> C
                    return path.substring(0, 1);
                case WINDOWS_CYGWIN:
                    if (path.length() < 10) {
                        throw new IllegalArgumentException("Path must be at least 10 characters long to get a windows drive letter [path was '" + path + "']");
                    }
                    // e.g. /cygwin/c/path -> c
                    return path.substring(8, 9);
                case WINDOWS_POSIX:
                    // could be either /C:/path/2 or /win-drive-C/path/2
                    if (path.length() < 3) {
                        throw new IllegalArgumentException("Path must be at least 3 characters long to get a windows drive letter [path was '" + path + "']");
                    }
                    // e.g. /C:/path -> C
                    return path.substring(1, 2);
                default:
                    return null;
            }
        }

        public String getWindowsPathWithoutDriveLetter(String path) {
            switch (this) {
                case WINDOWS:
                    // e.g. C:\path\2 -> path\2
                    return path.substring(3);
                case WINDOWS_CYGWIN:
                    // e.g. /cygwin/c/path/2 -> path/2
                    return path.substring(10);
                case WINDOWS_POSIX:
                    // could be either /C:/path/2 or /win-drive-C/path/2
                    // e.g. /C:/path/2 -> path/2
                    // e.g. /win-drive-C/path/2 -> path/2
                    return path.substring(4);
                default:
                    return null;
            }
        }

        public String toPathString(Spec fromSpec, String fromPath) {
            // if it's a relative path, the conversion is pretty straightforward
            if (isRelative(fromPath)) {
                // if the separators are the same, no conversion is needed
                if (Objects.equals(this.getSeparator(), fromSpec.getSeparator())) {
                    return fromPath;
                } else {
                    // we need to convert the separators, then return the path
                    return fromPath.replace(this.getSeparator(), fromSpec.getSeparator());
                }
            } else {
                // absolute path, we may need to convert the root path and/or separators
                if (this == fromSpec) {
                    // same specs, nothing to convert
                    return fromPath;
                } else if (this == Spec.WINDOWS) {
                    if (fromSpec == Spec.WINDOWS_POSIX || fromSpec == Spec.WINDOWS_CYGWIN) {
                        final String windowsDriveLetter = fromSpec.getWindowsDriveLetter(fromPath);
                        final String windowsPathWithoutDriveLetter = fromSpec.getWindowsPathWithoutDriveLetter(fromPath);
                        return windowsDriveLetter + ":\\" + windowsPathWithoutDriveLetter.replace(fromSpec.getSeparator(), this.getSeparator());
                    } else if (fromSpec == Spec.POSIX) {
                        // i guess we build a fake posix-esque path?
                        return "C:\\posix-root" + fromPath.replace(fromSpec.getSeparator(), this.getSeparator());
                    }
                } else if (this == Spec.POSIX) {
                    // we only need to convert one spec, the rest can be passed thru
                    if (fromSpec == Spec.WINDOWS_POSIX) {
                        final String windowsDriveLetter = fromSpec.getWindowsDriveLetter(fromPath);
                        final String windowsPathWithoutDriveLetter = fromSpec.getWindowsPathWithoutDriveLetter(fromPath);
                        return "/win-drive-" + windowsDriveLetter + windowsPathWithoutDriveLetter;
                    } else {
                        return fromPath;
                    }
                }
            }

            // should never happen
            throw new UnsupportedOperationException("Unable to convert from path '" + fromPath + "' to spec " + this);
        }

        static public Spec detect(String path) {
            if (path.startsWith("/")) {
                // we could also be dealing with a windows+unix style path such as /C:/ or /cygwin/c/
                if (path.length() > 3 && path.charAt(2) == ':' && path.charAt(3) == '/') {
                    return WINDOWS_POSIX;
                } else if (path.length() > 10 && path.startsWith("/cygwin/") && path.charAt(9) == '/') {
                    return WINDOWS_CYGWIN;
                } else {
                    return POSIX;
                }
            } else if (path.length() > 3 && path.charAt(1) == ':' && path.charAt(2) == '\\') {
                return WINDOWS;
            } else if (path.contains("\\")) {
                return WINDOWS;
            } else {
                return POSIX;
            }
        }

        static public Spec detectLocal() {
            // this should be enough to detect what we need to map locally to
            return detect(File.separator);
        }

    }

    final private Spec localSpec;
    final private Spec remoteSpec;

    private RemotePath(Spec localSpec, Spec remoteSpec) {
        Objects.requireNonNull(localSpec, "localSpec cannot be null");
        Objects.requireNonNull(remoteSpec, "remoteSpec cannot be null");
        this.localSpec = localSpec;
        this.remoteSpec = remoteSpec;
    }

    public Spec getLocalSpec() {
        return localSpec;
    }

    public Spec getRemoteSpec() {
        return remoteSpec;
    }

    public String toLocalPathString(String remotePath) {
        return this.remoteSpec.toPathString(this.localSpec, remotePath);
    }

    /*public Path toLocalPath(String remotePath) {
        // if it's a relative path, the conversion is pretty straightforward
        if (isRelative(remotePath)) {
            // if the separators are the same, no conversion is needed
            if (Objects.equals(this.localSpec.getSeparator(), this.remoteSpec.getSeparator())) {
                return Paths.get(remotePath);
            } else {
                // we need to convert the separators, then return the path
                return Paths.get(remotePath.replace(this.remoteSpec.getSeparator(), this.localSpec.getSeparator()));
            }
        } else {
            // absolute path, we may need to convert the root path and/or separators
            if (this.localSpec == this.remoteSpec) {
                // same specs, nothing to convert
                return Paths.get(remotePath);
            } else if (this.localSpec == Spec.WINDOWS) {
                if (this.remoteSpec == Spec.WINDOWS_POSIX || this.remoteSpec == Spec.WINDOWS_CYGWIN) {
                    final String windowsDriveLetter = this.remoteSpec.getWindowsDriveLetter(remotePath);
                    final String windowsPathWithoutDriveLetter = this.remoteSpec.getWindowsPathWithoutDriveLetter(remotePath);
                    final String newRemotePath = windowsDriveLetter + ":\\" + windowsPathWithoutDriveLetter.replace(this.remoteSpec.getSeparator(), this.localSpec.getSeparator());
                    return Paths.get(newRemotePath);
                } else if (this.remoteSpec == Spec.POSIX) {
                    final String newRemotePath = remotePath.replace(this.remoteSpec.getSeparator(), this.localSpec.getSeparator());
                    return Paths.get(newRemotePath);
                }


                throw new UnsupportedOperationException("Unable to convert remote path " + remotePath + " to local spec " + this.localSpec);
            } else if (this.localSpec == Spec.POSIX) {
                // we only need to convert one spec, the rest can be passed thru
                if (this.remoteSpec == Spec.WINDOWS_POSIX) {
                    final String windowsDriveLetter = this.remoteSpec.getWindowsDriveLetter(remotePath);
                    final String newRemotePath = "/win-drive-" + windowsDriveLetter + remotePath.substring(4);
                    return Paths.get(newRemotePath);
                } else {
                    return Paths.get(remotePath);
                }
            } else {
                // should never happen
                throw new UnsupportedOperationException("Unable to convert remote path " + remotePath + " to local spec " + this.localSpec);
            }
        }
    }*/

    static public boolean isRelative(String path) {
        return !isAbsolute(path);
    }

    static public boolean isAbsolute(String path) {
        // covers both unix and/or windows style paths
        return path.startsWith("/")
            || path.length() > 3 && path.charAt(1) == ':' && path.charAt(2) == '\\';
    }





    static public RemotePath create(String remotePath) {
        final Spec localSpec = Spec.detectLocal();
        final Spec remoteSpec = Spec.detect(remotePath);
        return new RemotePath(localSpec, remoteSpec);
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
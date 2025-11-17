package com.fizzed.blaze.util;

import java.io.File;
import java.nio.file.Path;
import java.util.Objects;

/**
 * Utility class for working with "remote" Paths, where they may differ from the local system, be it with file
 * separators (e.g. working on linux, remote system windows, or vice versa), or perhaps other interesting issues like
 * the "remote" system returning unix-style paths, but with invalid chars (e.g. "/C:/" as root on SFTP running on windows)
 */
public class PathTranslator {

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
            // special case of "/win-drive-C/blah"
            if (path.length() > 11 && path.startsWith("/win-drive-")) {
                return path.substring(11, 12);
            }

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
                    return path.substring(8, 9).toUpperCase();
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
            // special case of "/win-drive-C/blah"
            if (path.length() >= 12 && path.startsWith("/win-drive-")) {
                return path.substring(12);
            }

            switch (this) {
                case WINDOWS:
                    // e.g. C:\path\2 -> path\2
                    return path.substring(2);
                case WINDOWS_CYGWIN:
                    // e.g. /cygwin/c/path/2 -> path/2
                    return path.substring(9);
                case WINDOWS_POSIX:
                    // could be either /C:/path/2 or /win-drive-C/path/2
                    // e.g. /C:/path/2 -> path/2
                    // e.g. /win-drive-C/path/2 -> path/2
                    return path.substring(3);
                default:
                    return null;
            }
        }

        public String toPath(String fromPath, Spec toSpec) {
            final Spec fromSpec = this;

            // if it's a relative path, the conversion is pretty straightforward
            if (isRelative(fromPath)) {
                // if the separators are the same, no conversion is needed
                if (Objects.equals(toSpec.getSeparator(), fromSpec.getSeparator())) {
                    return fromPath;
                } else {
                    // we need to convert the separators, then return the path
                    return fromPath.replace(fromSpec.getSeparator(), toSpec.getSeparator());
                }
            } else {
                // absolute path, we may need to convert the root path and/or separators
                if (toSpec == fromSpec) {
                    // same specs, nothing to convert
                    return fromPath;
                } else if (toSpec == Spec.WINDOWS) {
                    if (fromSpec == Spec.WINDOWS_POSIX || fromSpec == Spec.WINDOWS_CYGWIN) {
                        final String windowsDriveLetter = fromSpec.getWindowsDriveLetter(fromPath);
                        final String windowsPathWithoutDriveLetter = fromSpec.getWindowsPathWithoutDriveLetter(fromPath);
                        return windowsDriveLetter + ":" + windowsPathWithoutDriveLetter.replace(fromSpec.getSeparator(), toSpec.getSeparator());
                    } else if (fromSpec == Spec.POSIX) {
                        // special case of "/win-drive-C/blah"
                        if (fromPath.startsWith("/win-drive-")) {
                            final String windowsDriveLetter = fromSpec.getWindowsDriveLetter(fromPath);
                            final String windowsPathWithoutDriveLetter = fromSpec.getWindowsPathWithoutDriveLetter(fromPath);
                            return windowsDriveLetter + ":" + windowsPathWithoutDriveLetter.replace(fromSpec.getSeparator(), toSpec.getSeparator());
                        }

                        // we can just convert this to a drive-letter-less path
                        return "" + fromPath.replace(fromSpec.getSeparator(), toSpec.getSeparator());
                    }
                } else if (toSpec == Spec.POSIX) {
                    // we only need to convert one spec, the rest can be passed thru
                    if (fromSpec == Spec.WINDOWS || fromSpec == Spec.WINDOWS_POSIX) {
                        // special case of "C:\\posix-root"
                        if (fromPath.startsWith("\\")) {
                        //if (fromPath.startsWith("C:\\posix-root")) {
                            // strip C:\posix-root off the front, then replace the separators
//                            return fromPath.substring(13).replace(fromSpec.getSeparator(), toSpec.getSeparator());
                            return fromPath.replace(fromSpec.getSeparator(), toSpec.getSeparator());
                        }

                        // otherwise, convert as needed
                        final String windowsDriveLetter = fromSpec.getWindowsDriveLetter(fromPath);
                        final String windowsPathWithoutDriveLetter = fromSpec.getWindowsPathWithoutDriveLetter(fromPath);
                        return "/win-drive-" + windowsDriveLetter + windowsPathWithoutDriveLetter.replace(fromSpec.getSeparator(), toSpec.getSeparator());
                    } else {
                        return fromPath;
                    }
                } else if (toSpec == Spec.WINDOWS_POSIX) {
                    if (fromSpec == Spec.WINDOWS || fromSpec == Spec.WINDOWS_CYGWIN || fromPath.startsWith("/win-drive-")) {
                        final String windowsDriveLetter = fromSpec.getWindowsDriveLetter(fromPath);
                        final String windowsPathWithoutDriveLetter = fromSpec.getWindowsPathWithoutDriveLetter(fromPath);
                        return "/" + windowsDriveLetter + ":" + windowsPathWithoutDriveLetter.replace(fromSpec.getSeparator(), toSpec.getSeparator());
                    } else {
                        return fromPath;
                    }
                } else if (toSpec == Spec.WINDOWS_CYGWIN) {
                    if (fromSpec == Spec.WINDOWS || fromSpec == Spec.WINDOWS_POSIX || fromPath.startsWith("/win-drive-")) {
                        final String windowsDriveLetter = fromSpec.getWindowsDriveLetter(fromPath);
                        final String windowsPathWithoutDriveLetter = fromSpec.getWindowsPathWithoutDriveLetter(fromPath);
                        return "/cygwin/" + windowsDriveLetter.toLowerCase() + windowsPathWithoutDriveLetter.replace(fromSpec.getSeparator(), toSpec.getSeparator());
                    } else {
                        return fromPath;
                    }
                }
            }

            // should never happen
            throw new UnsupportedOperationException("Unable to convert from spec " + fromSpec + " and path '" + fromPath + "' to spec " + toSpec);
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

    private PathTranslator(Spec localSpec, Spec remoteSpec) {
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

    public String toLocalPath(String remotePath) {
        return this.remoteSpec.toPath(remotePath, this.localSpec);
    }

    public String toLocalPath(Path remotePath) {
        return this.remoteSpec.toPath(remotePath.toString(), this.localSpec);
    }

    public String toRemotePath(String localPath) {
        return this.localSpec.toPath(localPath, this.remoteSpec);
    }

    public String toRemotePath(Path localPath) {
        return this.localSpec.toPath(localPath.toString(), this.remoteSpec);
    }

    static public boolean isRelative(String path) {
        return !isAbsolute(path);
    }

    static public boolean isAbsolute(String path) {
        // covers both unix and/or windows style paths
        return path.startsWith("/")
            || path.length() > 3 && path.charAt(1) == ':' && path.charAt(2) == '\\';
    }

    static public PathTranslator detectLocalRemote(String remotePath) {
        final Spec localSpec = Spec.detectLocal();
        final Spec remoteSpec = Spec.detect(remotePath);
        return new PathTranslator(localSpec, remoteSpec);
    }

}
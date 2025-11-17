package com.fizzed.blaze.util;

import org.junit.jupiter.api.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

class PathTranslatorTest {

    @Test
    public void posixRelativeRemotePath() {
        final String remotePath = "remote-build/jne";

        final PathTranslator pathTranslator = PathTranslator.detectLocalRemote(remotePath);

        if (pathTranslator.getLocalSpec() == PathTranslator.Spec.WINDOWS) {
            assertThat(pathTranslator.toLocalPath(remotePath), is("remote-build\\jne"));
        } else {
            assertThat(pathTranslator.toLocalPath(remotePath), is("remote-build/jne"));
        }
    }

    @Test
    public void windowsRelativeRemotePath() {
        final String remotePath = "remote-build\\jne";

        final PathTranslator pathTranslator = PathTranslator.detectLocalRemote(remotePath);

        if (pathTranslator.getLocalSpec() == PathTranslator.Spec.WINDOWS) {
            assertThat(pathTranslator.toLocalPath(remotePath), is("remote-build\\jne"));
        } else {
            assertThat(pathTranslator.toLocalPath(remotePath), is("remote-build/jne"));
        }
    }

    @Test
    public void posixAbsoluteRemotePath() {
        final String remotePath = "/remote-build/jne";

        final PathTranslator pathTranslator = PathTranslator.detectLocalRemote(remotePath);

        if (pathTranslator.getLocalSpec() == PathTranslator.Spec.WINDOWS) {
            assertThat(pathTranslator.toLocalPath(remotePath), is("\\remote-build\\jne"));
        } else {
            assertThat(pathTranslator.toLocalPath(remotePath), is("/remote-build/jne"));
        }
    }

    @Test
    public void windowsAbsoluteRemotePath() {
        final String remotePath = "C:\\remote-build\\jne";

        final PathTranslator pathTranslator = PathTranslator.detectLocalRemote(remotePath);

        if (pathTranslator.getLocalSpec() == PathTranslator.Spec.WINDOWS) {
            assertThat(pathTranslator.toLocalPath(remotePath), is("C:\\remote-build\\jne"));
        } else {
            assertThat(pathTranslator.toLocalPath(remotePath), is("/win-drive-C/remote-build/jne"));
        }
    }

    @Test
    public void windowsPosixAbsoluteRemotePath() {
        final String remotePath = "/C:/remote-build/jne";

        final PathTranslator pathTranslator = PathTranslator.detectLocalRemote(remotePath);

        if (pathTranslator.getLocalSpec() == PathTranslator.Spec.WINDOWS) {
            assertThat(pathTranslator.toLocalPath(remotePath), is("C:\\remote-build\\jne"));
        } else {
            assertThat(pathTranslator.toLocalPath(remotePath), is("/win-drive-C/remote-build/jne"));
        }
    }

    @Test
    public void windowsCygwinAbsoluteRemotePath() {
        final String remotePath = "/cygwin/c/remote-build/jne";

        final PathTranslator pathTranslator = PathTranslator.detectLocalRemote(remotePath);

        if (pathTranslator.getLocalSpec() == PathTranslator.Spec.WINDOWS) {
            assertThat(pathTranslator.toLocalPath(remotePath), is("C:\\remote-build\\jne"));
        } else {
            assertThat(pathTranslator.toLocalPath(remotePath), is("/cygwin/c/remote-build/jne"));
        }
    }

    @Test
    public void posixSpecRelativePath() {
        final String path = "remote-build/jne";

        assertThat(PathTranslator.Spec.detect(path), is(PathTranslator.Spec.POSIX));

        assertThat(PathTranslator.Spec.POSIX.toPath(path, PathTranslator.Spec.POSIX), is(path));
        assertThat(PathTranslator.Spec.POSIX.toPath(path, PathTranslator.Spec.WINDOWS), is("remote-build\\jne"));
    }

    @Test
    public void windowsSpecRelativePath() {
        final String path = "remote-build\\jne";

        assertThat(PathTranslator.Spec.detect(path), is(PathTranslator.Spec.WINDOWS));

        assertThat(PathTranslator.Spec.WINDOWS.toPath(path, PathTranslator.Spec.POSIX), is("remote-build/jne"));
        assertThat(PathTranslator.Spec.WINDOWS.toPath(path, PathTranslator.Spec.WINDOWS), is(path));
    }

    @Test
    public void posixSpecAbsolutePath() {
        final String path = "/home/builder/remote-build/jne";

        assertThat(PathTranslator.Spec.detect(path), is(PathTranslator.Spec.POSIX));

        assertThat(PathTranslator.Spec.POSIX.toPath(path, PathTranslator.Spec.POSIX), is(path));
        assertThat(PathTranslator.Spec.POSIX.toPath(path, PathTranslator.Spec.WINDOWS), is("\\home\\builder\\remote-build\\jne"));
        assertThat(PathTranslator.Spec.POSIX.toPath(path, PathTranslator.Spec.WINDOWS_POSIX), is(path));        // passthru
        assertThat(PathTranslator.Spec.POSIX.toPath(path, PathTranslator.Spec.WINDOWS_CYGWIN), is(path));       // passthru

        assertThat(PathTranslator.Spec.POSIX.toPath(path, PathTranslator.Spec.POSIX), is(path));
        assertThat(PathTranslator.Spec.WINDOWS.toPath("\\home\\builder\\remote-build\\jne", PathTranslator.Spec.POSIX), is(path));
        // TODO: should this work?
//        assertThat(PathTranslator.Spec.WINDOWS_POSIX.toPath(path, PathTranslator.Spec.POSIX), is(path));
        assertThat(PathTranslator.Spec.WINDOWS_CYGWIN.toPath(path, PathTranslator.Spec.POSIX), is(path));
    }

    @Test
    public void windowsSpecAbsolutePath() {
        final String path = "C:\\Users\\builder\\remote-build\\jne";

        assertThat(PathTranslator.Spec.detect(path), is(PathTranslator.Spec.WINDOWS));

        assertThat(PathTranslator.Spec.WINDOWS.toPath(path, PathTranslator.Spec.WINDOWS), is(path));
        assertThat(PathTranslator.Spec.WINDOWS.toPath(path, PathTranslator.Spec.POSIX), is("/win-drive-C/Users/builder/remote-build/jne"));
        assertThat(PathTranslator.Spec.WINDOWS.toPath(path, PathTranslator.Spec.WINDOWS_POSIX), is("/C:/Users/builder/remote-build/jne"));
        assertThat(PathTranslator.Spec.WINDOWS.toPath(path, PathTranslator.Spec.WINDOWS_CYGWIN), is("/cygwin/c/Users/builder/remote-build/jne"));

        assertThat(PathTranslator.Spec.POSIX.toPath("/win-drive-C/Users/builder/remote-build/jne", PathTranslator.Spec.WINDOWS), is(path));
        assertThat(PathTranslator.Spec.WINDOWS.toPath(path, PathTranslator.Spec.WINDOWS), is(path));
        assertThat(PathTranslator.Spec.WINDOWS_POSIX.toPath("/C:/Users/builder/remote-build/jne", PathTranslator.Spec.WINDOWS), is(path));
        assertThat(PathTranslator.Spec.WINDOWS_CYGWIN.toPath("/cygwin/c/Users/builder/remote-build/jne", PathTranslator.Spec.WINDOWS), is(path));
    }

    @Test
    public void windowsPosixSpecAbsolutePath() {
        final String path = "/C:/Users/builder/remote-build/jne";

        assertThat(PathTranslator.Spec.detect(path), is(PathTranslator.Spec.WINDOWS_POSIX));

        assertThat(PathTranslator.Spec.WINDOWS_POSIX.toPath(path, PathTranslator.Spec.POSIX), is("/win-drive-C/Users/builder/remote-build/jne"));
        assertThat(PathTranslator.Spec.WINDOWS_POSIX.toPath(path, PathTranslator.Spec.WINDOWS), is("C:\\Users\\builder\\remote-build\\jne"));
        assertThat(PathTranslator.Spec.WINDOWS_POSIX.toPath(path, PathTranslator.Spec.WINDOWS_POSIX), is(path));
        assertThat(PathTranslator.Spec.WINDOWS_POSIX.toPath(path, PathTranslator.Spec.WINDOWS_CYGWIN), is("/cygwin/c/Users/builder/remote-build/jne"));

        assertThat(PathTranslator.Spec.POSIX.toPath("/win-drive-C/Users/builder/remote-build/jne", PathTranslator.Spec.WINDOWS_POSIX), is(path));
        assertThat(PathTranslator.Spec.WINDOWS.toPath("C:\\Users\\builder\\remote-build\\jne", PathTranslator.Spec.WINDOWS_POSIX), is(path));
        assertThat(PathTranslator.Spec.WINDOWS_POSIX.toPath("/C:/Users/builder/remote-build/jne", PathTranslator.Spec.WINDOWS_POSIX), is(path));
        assertThat(PathTranslator.Spec.WINDOWS_CYGWIN.toPath("/cygwin/c/Users/builder/remote-build/jne", PathTranslator.Spec.WINDOWS_POSIX), is(path));
    }

    @Test
    public void windowsCygwinSpecAbsolutePath() {
        final String path = "/cygwin/c/Users/builder/remote-build/jne";

        assertThat(PathTranslator.Spec.detect(path), is(PathTranslator.Spec.WINDOWS_CYGWIN));

        assertThat(PathTranslator.Spec.WINDOWS_CYGWIN.toPath(path, PathTranslator.Spec.POSIX), is(path));
        assertThat(PathTranslator.Spec.WINDOWS_CYGWIN.toPath(path, PathTranslator.Spec.WINDOWS), is("C:\\Users\\builder\\remote-build\\jne"));
        assertThat(PathTranslator.Spec.WINDOWS_CYGWIN.toPath(path, PathTranslator.Spec.WINDOWS_POSIX), is("/C:/Users/builder/remote-build/jne"));
        assertThat(PathTranslator.Spec.WINDOWS_CYGWIN.toPath(path, PathTranslator.Spec.WINDOWS_CYGWIN), is(path));

        assertThat(PathTranslator.Spec.POSIX.toPath(path, PathTranslator.Spec.WINDOWS_CYGWIN), is(path));
        assertThat(PathTranslator.Spec.WINDOWS.toPath("C:\\Users\\builder\\remote-build\\jne", PathTranslator.Spec.WINDOWS_CYGWIN), is(path));
        assertThat(PathTranslator.Spec.WINDOWS_POSIX.toPath("/C:/Users/builder/remote-build/jne", PathTranslator.Spec.WINDOWS_CYGWIN), is(path));
        assertThat(PathTranslator.Spec.WINDOWS_CYGWIN.toPath("/cygwin/c/Users/builder/remote-build/jne", PathTranslator.Spec.WINDOWS_CYGWIN), is(path));
    }

}
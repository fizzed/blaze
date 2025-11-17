package com.fizzed.blaze.ssh.util;

import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.nio.file.Paths;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.*;

class RemotePathTest {

    @Test
    public void posixSpecRelativePath() {
        final String remotePath = "remote-build/jne";

        final RemotePath remotePathHelper = RemotePath.create(remotePath);

        assertThat(remotePathHelper.getRemoteSpec(), is(RemotePath.Spec.POSIX));

        final String localPath = remotePathHelper.toLocalPathString(remotePath);

        if (remotePathHelper.getLocalSpec() == RemotePath.Spec.POSIX) {
            assertThat(localPath, is(remotePath));
        } else {
            assertThat(localPath, is("remote-build\\jne"));
        }
    }

    @Test
    public void windowsSpecRelativePath() {
        final String remotePath = "remote-build\\jne";

        final RemotePath remotePathHelper = RemotePath.create(remotePath);

        assertThat(remotePathHelper.getRemoteSpec(), is(RemotePath.Spec.WINDOWS));

        final String localPath = remotePathHelper.toLocalPathString(remotePath);

        if (remotePathHelper.getLocalSpec() == RemotePath.Spec.WINDOWS) {
            assertThat(localPath, is(remotePath));
        } else {
            assertThat(localPath, is("remote-build/jne"));
        }
    }

    /*@Test
    public void posixSpecAbsolutePath() {
        final String remotePath = "/home/builder/remote-build/jne";

        final RemotePath remotePathHelper = RemotePath.create(remotePath);

        final Path localPath = remotePathHelper.toLocalPath(remotePath);

        if (remotePathHelper.getLocalSpec() == RemotePath.Spec.POSIX) {
            assertThat(localPath, is(Paths.get(remotePath)));
            assertThat(localPath.toString(), is(remotePath));
            assertThat(localPath.resolve("sub"), is(Paths.get(remotePath).resolve("sub")));
            assertThat(localPath.resolve("sub").toString(), is("/home/builder/remote-build/jne/sub"));
        }
    }

    @Test
    public void windowsSpecAbsolutePath() {
        final String remotePath = "C:\\Users\\builder\\remote-build\\jne";

        final RemotePath remotePathHelper = RemotePath.create(remotePath);

        final Path localPath = remotePathHelper.toLocalPath(remotePath);

        if (remotePathHelper.getLocalSpec() == RemotePath.Spec.POSIX) {
            assertThat(localPath, is(Paths.get(remotePath)));
            assertThat(localPath.resolve("sub"), is(Paths.get(remotePath).resolve("sub")));
            assertThat(localPath.resolve("sub").toString(), is( "/win-drive-C/Users/builder/remote-build/sub"));
        }
    }*/

    /*@Test
    public void windowsSftpServer() {
        // windows sftp server sends back paths like this
        final String rpath = "/C:/Users/builder";

        final RemotePath remotePath = RemotePath.create(rpath);

        assertThat(remotePath.toPath(rpath), is(Paths.get("/windrive-c/Users/builder")));
    }*/

}
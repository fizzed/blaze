package com.fizzed.blaze.ssh.util;

import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.nio.file.Paths;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.*;

class RemotePathTest {

    @Test
    public void posixStyleRelativePath() {
        // windows sftp server sends back paths like this
        final String remotePath = "remote-build/jne";

        final RemotePath remotePathHelper = RemotePath.create(remotePath);

        assertThat(remotePathHelper.toLocalPath(remotePath), is(Paths.get(remotePath)));
    }

    @Test
    public void windowsStyleRelativePath() {
        // windows sftp server sends back paths like this
        final String remotePath = "remote-build\\jne";

        final RemotePath remotePathHelper = RemotePath.create(remotePath);

        assertThat(remotePathHelper.toLocalPath(remotePath), is(Paths.get("remote-build/jne")));
    }

    /*@Test
    public void windowsSftpServer() {
        // windows sftp server sends back paths like this
        final String rpath = "/C:/Users/builder";

        final RemotePath remotePath = RemotePath.create(rpath);

        assertThat(remotePath.toPath(rpath), is(Paths.get("/windrive-c/Users/builder")));
    }*/

}
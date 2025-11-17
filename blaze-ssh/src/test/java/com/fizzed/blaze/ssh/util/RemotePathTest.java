package com.fizzed.blaze.ssh.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RemotePathTest {

    @Test
    public void windowsSftpServer() {
        // windows sftp server sends back paths like this
        final String rpath = "/C:/Users/builder";
    }

}
package com.fizzed.blaze.ssh.util;

import org.junit.jupiter.api.Test;

import static java.util.Arrays.asList;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

class SshArgumentsTest {

    @Test
    public void noEscapes() {
        String cmd = SshArguments.buildEscapedCommand(asList("a", "b", "c"));

        assertThat(cmd, is("a b c"));
    }

    @Test
    public void passthroughEscapes() {
        String cmd = SshArguments.buildEscapedCommand(asList("a", "'b'", "\"c\""));

        assertThat(cmd, is("a 'b' \"c\""));
    }

    @Test
    public void escapeSpaces() {
        String cmd = SshArguments.buildEscapedCommand(asList("a", " b ", " \"c "));

        assertThat(cmd, is("a \" b \" \" \\\"c \""));
    }

    /*@Test
    public void dollarSignNeedsEscaping() {
        String cmd = SshArguments.buildEscapedCommand(asList("a", "My$file", " \"c "));

        assertThat(cmd, is("a \" My$file \" \" \\\"c \""));
    }*/

}
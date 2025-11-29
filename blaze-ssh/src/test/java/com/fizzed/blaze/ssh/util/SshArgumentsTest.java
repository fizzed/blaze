package com.fizzed.blaze.ssh.util;

import org.junit.jupiter.api.Test;

import static java.util.Arrays.asList;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

class SshArgumentsTest {

    @Test
    public void noEscapes() {
        String cmd = SshArguments.smartEscapedCommandLine(asList("a", "b", "c"), false);

        assertThat(cmd, is("a b c"));
    }

    @Test
    public void disableEscaping() {
        String cmd = SshArguments.smartEscapedCommandLine(asList("a", " b ", "c"), true);

        assertThat(cmd, is("a  b  c"));
    }

    @Test
    public void passthroughEscapes() {
        String cmd = SshArguments.smartEscapedCommandLine(asList("a", "'b'", "\"c $i-know-what-i'm-doing\""), false);

        assertThat(cmd, is("a 'b' \"c $i-know-what-i'm-doing\""));
    }

    @Test
    public void escapeSpaces() {
        String cmd = SshArguments.smartEscapedCommandLine(asList("a", " b ", " \"c "), false);

        assertThat(cmd, is("a ' b ' ' \"c '"));
    }

    @Test
    public void escapeDollarSigns() {
        String cmd = SshArguments.smartEscapedCommandLine(asList("a", "$b.class", "c"), false);

        assertThat(cmd, is("a '$b.class' c"));
    }

    @Test
    public void posixPathChars() {
        String cmd = SshArguments.smartEscapedCommandLine(asList("a/b/c", "d"), false);

        assertThat(cmd, is("a/b/c d"));
    }

    @Test
    public void windowsPathChars() {
        String cmd = SshArguments.smartEscapedCommandLine(asList("a\\b\\c", "d"), false);

        assertThat(cmd, is("a\\b\\c d"));
    }

}
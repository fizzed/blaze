package com.fizzed.blaze.ssh.util;

import java.util.List;
import java.util.regex.Pattern;

public class SshArguments {

    // Pattern for characters that are safe to leave unquoted.
    // Alphanumeric, hyphen, underscore, dot, slash, and equals (often used in flags).
    static private final Pattern SAFE_CHARS = Pattern.compile("^[a-zA-Z0-9\\\\\\-_./=@:,+]+$");

    static private boolean isAlreadyQuoted(String arg) {
        if (arg.length() < 2) return false;
        char first = arg.charAt(0);
        char last = arg.charAt(arg.length() - 1);

        // Check for matching '...' or "..."
        return (first == '\'' && last == '\'') || (first == '"' && last == '"');
    }

    static public String smartEscapedArgument(String arg) {
        if (arg == null || arg.isEmpty()) {
            return "''"; // Empty string must be quoted
        }

        // 1. Pass-through check: Is it already wrapped in quotes?
        if (isAlreadyQuoted(arg)) {
            return arg;
        }

        // 2. Safety check: Does it contain only safe characters?
        if (SAFE_CHARS.matcher(arg).matches()) {
            return arg;
        }

        // 3. Escape logic: Wrap in single quotes and handle internal single quotes
        //    Input:  I'm "Cool"
        //    Output: 'I'\''m "Cool"'
        // Linux/BSD/macOS/Illumos: These all run sh, bash, dash, or zsh by default for incoming SSH connections. The
        // single-quote escaping mechanism ('...') is standard POSIX and works identically on all of them.
        // Windows (OpenSSH): the single-quote strategy is the safest baseline because most SSH implementations on Window
        // specifically try to emulate POSIX argument parsing to maintain compatibility with tools like rsync and scp.
        return "'" + arg.replace("'", "'\\''") + "'";
    }

    /**
     * The protocol itself, as defined in RFC 4254, uses a channel request message (specifically SSH_MSG_CHANNEL_REQUEST
     * with an "exec" request type) that contains a single field for the command to be run.
     *
     * When you type a command like ssh user@host ls -l /tmp, your local SSH client (like OpenSSH) is responsible for
     * taking all the arguments (ls, -l, /tmp) and concatenating them into a single string, usually separated by spaces.
     * The string sent to the server would be "ls -l /tmp"
     *
     * @return A properly formatted string, with escapes as needed
     */
    static public String smartEscapedCommandLine(List<String> arguments, boolean disableSmartEscaping) {
        final StringBuilder sb = new StringBuilder();

        for (String arg : arguments) {
            // add a command separator
            if (sb.length() > 0) {
                sb.append(" ");
            }

            if (disableSmartEscaping) {
                // pass thru argument as-is, do nothing smart
                sb.append(arg);
            } else {
                sb.append(smartEscapedArgument(arg));
            }
        }

        return sb.toString();
    }

}
package com.fizzed.blaze.ssh.util;

import java.util.List;

public class SshArguments {

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
    static public String buildEscapedCommand(List<String> arguments) {
        // building the command may be a little tricky, not sure about spaces...
        final StringBuilder sb = new StringBuilder();

        for (String arg : arguments) {
            // add a command separator
            if (sb.length() > 0) {
                sb.append(" ");
            }

            // if the argument contains no spaces, we can safely pass it thru
            if (!arg.contains(" ")) {
                sb.append(arg);
            } else {
                // argument does contain spaces, but perhaps it's already escaped properly?
                if (arg.startsWith("'") && arg.endsWith("'")) {
                    sb.append(arg);
                } else if (arg.startsWith("\"") && arg.endsWith("\"")) {
                    sb.append(arg);
                } else {
                    // we will want to use quotes around the argument
                    sb.append("\"");
                    // if the argument contains a quote char, we need to escape them too
                    sb.append(arg.replace("\"", "\\\""));
                    sb.append("\"");
                }
            }
        }

        return sb.toString();
    }

}
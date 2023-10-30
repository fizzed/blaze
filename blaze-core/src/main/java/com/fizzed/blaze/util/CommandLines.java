package com.fizzed.blaze.util;

import java.util.List;

public class CommandLines {

    static public String debug(List<String> commands) {
        StringBuilder sb = new StringBuilder();

        for (String command : commands) {
            if (sb.length() > 0) {
                sb.append(" ");
            }
            // do we need quotes?
            if (command.contains(" ")) {
                sb.append("\"");
                sb.append(command);
                sb.append("\"");
            } else {
                sb.append(command);
            }
        }

        return sb.toString();
    }

}
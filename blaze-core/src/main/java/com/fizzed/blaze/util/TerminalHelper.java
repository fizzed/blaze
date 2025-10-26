package com.fizzed.blaze.util;

public class TerminalHelper {

    static public String padRight(String value, int width) {
        StringBuilder sb = new StringBuilder();

        sb.append(value);

        for (int i = 0; i < (width - value.length()); i++) {
            sb.append(' ');
        }

        return sb.toString();
    }

    static public void clearLinePrint() {
        System.out.print(clearLineCode());
    }

    static public void clearLinePrint(String message) {
        System.out.print(clearLineCode() + message);
    }

    static public String clearLineCode() {
        return "\033[2K\r";
    }

    static public String redCode() {
        return "\033[31m";
    }

    static public String greenCode() {
        return "\033[32m";
    }

    static public String blueCode() {
        return "\033[34m";
    }

    static public String resetCode() {
        return "\033[0m";
    }

    /*static public void appendPadding(StringBuilder sb) {
        if (sb.length() > maxRenderLength) {
            this.maxRenderLength = sb.length();
        } else {
            // we need to add some spaces to the end to make sure the progress bar is always the same length
            int spacesToAdd = maxRenderLength - sb.length();
            for (int i = 0; i < spacesToAdd; i++) {
                sb.append(" ");
            }
        }
    }*/

}
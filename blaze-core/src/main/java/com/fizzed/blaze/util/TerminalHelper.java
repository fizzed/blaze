package com.fizzed.blaze.util;

public class TerminalHelper {

    static public String clearLineCode() {
        return "\033[2K\r";
    }

    static public String cursorUpCode(int count) {
        return "\033[" + count + "A";
    }

    static public String redCode() {
        return "\033[31m";
    }

    static public String greenCode() {
        return "\033[32m";
    }

    static public String yellowCode() {
        return "\033[33m";
    }

    static public String blueCode() {
        return "\033[34m";
    }

    static public String magentaCode() {
        return "\033[35m";
    }

    static public String cyanCode() {
        return "\033[36m";
    }

    static public String resetCode() {
        return "\033[0m";
    }

    static public String clearLine(String message) {
        return clearLineCode() + (message != null ? message : "");
    }

    static public String green(String message) {
        return greenCode() + message + resetCode();
    }

    static public String red(String message) {
        return redCode() + message + resetCode();
    }

    static public String yellow(String message) {
        return yellowCode() + message + resetCode();
    }

    static public String blue(String message) {
        return blueCode() + message + resetCode();
    }

    static public String magenta(String message) {
        return magentaCode() + message + resetCode();
    }

    static public String cyan(String message) {
        return cyanCode() + message + resetCode();
    }

    static public String fixedWidthCenter(String value, int len) {
        return fixedWidthCenter(value, len, ' ');
    }

    static public String fixedWidthCenter(String value, int len, char padChar) {
        int totalPad = len - 2 - value.length();
        int leftPad = totalPad / 2;
        int rightPad = totalPad - leftPad;
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < leftPad; i++) {
            sb.append(padChar);
        }
        sb.append(" ");
        sb.append(value);
        sb.append(" ");
        for (int i = 0; i < rightPad; i++) {
            sb.append(padChar);
        }
        return sb.toString();
    }

    static public String fixedWidthLeft(String value, int len) {
        return fixedWidthLeft(value, len, ' ');
    }

    static public String fixedWidthLeft(String value, int len, char padChar) {
        if (value.length() > len) {
            return value.substring(0, len);
        } else if (value.length() == len) {
            return value;
        } else {
            StringBuilder sb = new StringBuilder();
            sb.append(value);
            for (int i = value.length(); i < len; i++) {
                sb.append(padChar);
            }
            return sb.toString();
        }
    }

    static public String fixedWidthRight(String value, int len) {
        return fixedWidthRight(value, len, ' ');
    }

    static public String fixedWidthRight(String value, int len, char padChar) {
        if (value.length() > len) {
            return value.substring(0, len);
        } else if (value.length() == len) {
            return value;
        } else {
            StringBuilder sb = new StringBuilder();
            int padLen = len - value.length();
            for (int i = 0; i < padLen; i++) {
                sb.append(padChar);
            }
            sb.append(value);
            return sb.toString();
        }
    }

}
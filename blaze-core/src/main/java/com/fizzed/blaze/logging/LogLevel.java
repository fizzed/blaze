package com.fizzed.blaze.logging;

import org.slf4j.event.Level;

public enum LogLevel {

    // Order and integer value are important!
    TRACE(0, "\u001B[35m"), // Magenta
    DEBUG(10, "\u001B[32m"), // Green
    INFO(20, "\u001B[36m"),  // Cyan
    WARN(30, "\u001B[33m"),  // Yellow
    ERROR(40, "\u001B[31m"), // Red
    OFF(Integer.MAX_VALUE, "\u001B[0m"); // Reset

    public static final String ANSI_RESET = "\u001B[0m";

    private final int levelInt;
    private final String color;

    LogLevel(int levelInt, String color) {
        this.levelInt = levelInt;
        this.color = color;
    }

    public int getLevelInt() {
        return levelInt;
    }

    public String getColor() {
        return color;
    }

    /**
     * Converts an SLF4J Level to our internal LogLevel
     */
    public static LogLevel fromSlf4jLevel(Level level) {
        switch (level) {
            case TRACE: return TRACE;
            case DEBUG: return DEBUG;
            case INFO:  return INFO;
            case WARN:  return WARN;
            case ERROR: return ERROR;
            default:
                throw new IllegalArgumentException("Unknown SLF4J level: " + level);
        }
    }

}
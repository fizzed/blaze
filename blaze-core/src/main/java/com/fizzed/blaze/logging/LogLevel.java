package com.fizzed.blaze.logging;

import org.slf4j.event.Level;

public enum LogLevel {

    // Order and integer value are important!
    TRACE(0, "TRCE", "\u001B[35m"), // Magenta
    DEBUG(10, "DBG", "\u001B[32m"), // Green
    INFO(20, "INFO", "\u001B[36m"),  // Cyan
    WARN(30, "WARN", "\u001B[33m"),  // Yellow
    ERROR(40, "ERR", "\u001B[31m"), // Red
    OFF(Integer.MAX_VALUE, "OFF", "\u001B[0m"); // Reset

    public static final String ANSI_RESET = "\u001B[0m";

    private final int levelInt;
    private final String logName;
    private final String color;

    LogLevel(int levelInt, String logName, String color) {
        this.levelInt = levelInt;
        this.logName = logName;
        this.color = color;
    }

    public int getLevelInt() {
        return levelInt;
    }

    public String getLogName() {
        return logName;
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
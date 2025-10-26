package com.fizzed.blaze.logging;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

public class LoggerConfig {

    // --- Configuration Storage ---

    // Stores configured log levels. Key is the logger name (e.g., "com.example.MyClass")
    static private final Map<String,LogLevel> LEVEL_CONFIG = new ConcurrentHashMap<>();
    static private LogLevel defaultLevel = LogLevel.INFO;
    static private boolean displayDateTime = true;
    static private boolean displayThreadName = true;
    static private boolean displayAnsiColors = true;
    static private boolean displayLoggerName = true;

    // --- Listener for config changes ---

    // A simple listener to notify the factory when config changes,
    // so loggers can update their cached effective level.
    static Consumer<Void> configChangeListener = null;

    // --- Public API ---

    /**
     * Sets the default log level for all loggers that do not have a
     * specific level configured. Default is INFO.
     * @param level The new default level
     */
    public static void setDefaultLogLevel(LogLevel level) {
        defaultLevel = Objects.requireNonNull(level);
        notifyListener();
    }

    /**
     * Sets the log level for a specific logger name.
     * @param name The fully qualified class name (or logger name)
     * @param level The level to set
     */
    public static void setLogLevel(String name, LogLevel level) {
        if (level == null) {
            LEVEL_CONFIG.remove(name);
        } else {
            LEVEL_CONFIG.put(name, level);
        }
        notifyListener();
    }

    /**
     * A convenience method to set the log level for a specific class.
     * @param clazz The class
     * @param level The level to set
     */
    public static void setLogLevel(Class<?> clazz, LogLevel level) {
        setLogLevel(clazz.getName(), level);
    }

    /**
     * Clears all custom log level configurations and resets
     * the default level to INFO.
     */
    public static void resetConfiguration() {
        LEVEL_CONFIG.clear();
        defaultLevel = LogLevel.INFO;
        notifyListener();
    }

    // --- Internal API (used by the logger implementation) ---

    /**
     * Gets the effective log level for a given logger name.
     * This method walks up the "package hierarchy" to find the
     * most specific log level.
     *
     * Example: For "com.example.foo.Bar"
     * 1. Check "com.example.foo.Bar"
     * 2. Check "com.example.foo"
     * 3. Check "com.example"
     * 4. Check "com"
     * 5. Check "" (root logger)
     * 6. Use default level
     */
    static LogLevel getEffectiveLevel(String name) {
        // 1. Check for exact match
        LogLevel level = LEVEL_CONFIG.get(name);
        if (level != null) {
            return level;
        }

        // 2. Walk up the package hierarchy
        int lastDot = name.length();
        while ((lastDot = name.lastIndexOf('.', lastDot - 1)) != -1) {
            String parentName = name.substring(0, lastDot);
            level = LEVEL_CONFIG.get(parentName);
            if (level != null) {
                return level;
            }
        }

        // 3. Check "root" logger
        LogLevel rootLevel = LEVEL_CONFIG.get("");
        if (rootLevel != null) {
            return rootLevel;
        }

        // 4. Use default
        return defaultLevel;
    }

    public static boolean isDisplayDateTime() {
        return displayDateTime;
    }

    public static void setDisplayDateTime(boolean displayDateTime) {
        LoggerConfig.displayDateTime = displayDateTime;
        notifyListener();
    }

    public static boolean isDisplayThreadName() {
        return displayThreadName;
    }

    public static void setDisplayThreadName(boolean displayThreadName) {
        LoggerConfig.displayThreadName = displayThreadName;
        notifyListener();
    }

    public static boolean isDisplayAnsiColors() {
        return displayAnsiColors;
    }

    public static void setDisplayAnsiColors(boolean displayAnsiColors) {
        LoggerConfig.displayAnsiColors = displayAnsiColors;
        notifyListener();
    }

    public static boolean isDisplayLoggerName() {
        return displayLoggerName;
    }

    public static void setDisplayLoggerName(boolean displayLoggerName) {
        LoggerConfig.displayLoggerName = displayLoggerName;
        notifyListener();
    }

    private static void notifyListener() {
        if (configChangeListener != null) {
            configChangeListener.accept(null);
        }
    }

}
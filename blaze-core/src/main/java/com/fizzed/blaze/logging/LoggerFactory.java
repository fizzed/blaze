package com.fizzed.blaze.logging;

import org.slf4j.ILoggerFactory;
import org.slf4j.Logger;
import org.slf4j.Marker;
import org.slf4j.event.Level;
import org.slf4j.helpers.AbstractLogger;
import org.slf4j.helpers.MessageFormatter;

import java.io.PrintStream;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class LoggerFactory implements ILoggerFactory {

    // Cache of all loggers we've created
    private final Map<String, SimpleLogger> loggerMap = new ConcurrentHashMap<>();

    // Formatter for the timestamp
    private static final DateTimeFormatter DATE_FORMATTER =
        DateTimeFormatter.ISO_INSTANT.withZone(ZoneId.systemDefault());

    public LoggerFactory() {
        // Register a listener to config changes
        LoggerConfig.configChangeListener = (v) -> this.updateAllLoggerLevels();
    }

    /**
     * This is the main method called by LoggerFactory.getLogger(...)
     */
    @Override
    public Logger getLogger(String name) {
        // ComputeIfAbsent is a clean, thread-safe way to get-or-create
        return loggerMap.computeIfAbsent(name, this::createNewLogger);
    }

    /**
     * Factory method to create a new logger
     */
    private SimpleLogger createNewLogger(String name) {
        return new SimpleLogger(name);
    }

    /**
     * Called by the config listener. Iterates all known loggers
     * and tells them to update their cached log level.
     */
    private void updateAllLoggerLevels() {
        for (SimpleLogger logger : loggerMap.values()) {
            logger.updateEffectiveLevel();
        }
    }

    // ##################################################################
    // ##
    // ##   THE ACTUAL LOGGER IMPLEMENTATION
    // ##
    // ##################################################################

    /**
     * Our actual Logger implementation.
     * <p>
     * We extend AbstractLogger, which is a fantastic helper from SLF4J.
     * It handles all the complex logic like:
     * - Message formatting (replacing "{}" with arguments)
     * - Checking if a level is enabled before formatting (for performance)
     * - Handling Markers
     * <p>
     * We only need to implement a few key methods.
     */
    static class SimpleLogger extends AbstractLogger {

        // The cached effective level for this specific logger.
        private LogLevel effectiveLevel;

        // A shortened name for display
        private final String shortName;

        protected SimpleLogger(String name) {
            this.name = name;
            this.shortName = shortenName(name);
            // Get the initial level from the config
            updateEffectiveLevel();
        }

        /**
         * Re-calculates and caches the effective log level for this logger.
         */
        void updateEffectiveLevel() {
            this.effectiveLevel = LoggerConfig.getEffectiveLevel(this.name);
        }

        /**
         * The core log-handling method. AbstractLogger calls this ONLY IF
         * the corresponding level (e.g., INFO) is enabled.
         */
        @Override
        protected void handleNormalizedLoggingCall(
            Level level,
            org.slf4j.Marker marker, // We ignore markers
            String messagePattern,
            Object[] arguments,
            Throwable throwable) {

            // 1. Convert to our internal level (for color, etc.)
            LogLevel myLevel = LogLevel.fromSlf4jLevel(level);

            // 2. Format the message (e.g., "Hello {}" + "World" -> "Hello World")
//            String formattedMessage = MessageFormatter.arrayFormat(messagePattern, arguments).getMessage();
            String formattedMessage = MessageFormatter.basicArrayFormat(messagePattern, arguments);

            // 3. Get the right color and output stream
            String color = myLevel.getColor();
            //PrintStream out = (myLevel.getLevelInt() >= LogLevel.WARN.getLevelInt()) ? errorStream : outputStream;
            PrintStream out = System.out;

            // 4. Build the final log string
            // [Timestamp] [Thread] LEVEL [LoggerName] - Message
            StringBuilder sb = new StringBuilder();
            //sb.append(color); // Start color
            //sb.append(DATE_FORMATTER.format(Instant.now()));
            //sb.append(" [");
            //sb.append(Thread.currentThread().getName());
            //sb.append("] ");
            sb.append("[");
            sb.append(color);                   // start color
            sb.append(String.format("%-5s", myLevel.name())); // "INFO ", "ERROR"
            sb.append(LogLevel.ANSI_RESET);     // end color
            sb.append("] ");
            //sb.append(" [");
            //sb.append(this.shortName);
            //sb.append("] - ");
            sb.append(formattedMessage);

            // 5. Print it!
            out.println(sb);

            // 6. Print the stack trace if it exists
            if (throwable != null) {
                //out.print(color); // Keep the color for the stack trace
                throwable.printStackTrace(out);
                //out.print(LogLevel.ANSI_RESET);
                out.flush();
            }
        }

        // --- These methods are for the fast-path check ---
        // AbstractLogger calls these *before* formatting the message.

        private boolean isLevelEnabled(LogLevel level) {
            // Compare integer values.
            // e.g., if effectiveLevel is INFO (20),
            // a TRACE (0) or DEBUG (10) request will be false.
            // a WARN (30) request will be true.
            return this.effectiveLevel.getLevelInt() <= level.getLevelInt();
        }

        @Override
        public boolean isTraceEnabled() {
            return isLevelEnabled(LogLevel.TRACE);
        }

        @Override
        public boolean isDebugEnabled() {
            return isLevelEnabled(LogLevel.DEBUG);
        }

        @Override
        public boolean isInfoEnabled() {
            return isLevelEnabled(LogLevel.INFO);
        }

        @Override
        public boolean isWarnEnabled() {
            return isLevelEnabled(LogLevel.WARN);
        }

        @Override
        public boolean isErrorEnabled() {
            return isLevelEnabled(LogLevel.ERROR);
        }

        @Override
        public boolean isTraceEnabled(Marker marker) {
            return isLevelEnabled(LogLevel.TRACE);
        }

        @Override
        public boolean isDebugEnabled(Marker marker) {
            return isLevelEnabled(LogLevel.DEBUG);
        }

        @Override
        public boolean isInfoEnabled(Marker marker) {
            return isLevelEnabled(LogLevel.INFO);
        }

        @Override
        public boolean isWarnEnabled(Marker marker) {
            return isLevelEnabled(LogLevel.WARN);
        }

        @Override
        public boolean isErrorEnabled(Marker marker) {
            return isLevelEnabled(LogLevel.ERROR);
        }

        /**
         * Not used by AbstractLogger, but required by Logger interface.
         */
        @Override
        public String getName() {
            return name;
        }

        @Override
        protected String getFullyQualifiedCallerName() {
            return "";
        }

        /**
         * Shortens a logger name "com.example.package.MyClass" to "c.e.p.MyClass"
         */
        private String shortenName(String longName) {
            String[] parts = longName.split("\\.");
            if (parts.length <= 1) {
                return longName;
            }
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < parts.length - 1; i++) {
                if (parts[i].length() > 0) {
                    sb.append(parts[i].charAt(0));
                    sb.append('.');
                }
            }
            sb.append(parts[parts.length - 1]);
            return sb.toString();
        }
    }
}
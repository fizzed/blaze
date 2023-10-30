package com.fizzed.blaze.util;

import com.fizzed.blaze.core.Verbosity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class VerboseLogger {

    private final Logger wrapped;
    private Verbosity level;

    public VerboseLogger(Object object) {
        this(LoggerFactory.getLogger(object.getClass()));
    }

    private VerboseLogger(Logger log) {
        this.wrapped = log;
        this.level = Verbosity.DEFAULT;
    }

    public Logger wrapped() {
        return wrapped;
    }

    public Verbosity getLevel() {
        return level;
    }

    public VerboseLogger setLevel(Verbosity level) {
        this.level = level;
        return this;
    }

    public boolean isInfo() {
        return wrapped.isInfoEnabled();
    }

    public void info(String s, Object... arguments) {
        // no conversion needed
        wrapped.info(s, arguments);
    }

    public boolean isVerbose() {
        return this.level.gte(Verbosity.VERBOSE) || wrapped.isDebugEnabled();
    }

    public void verbose(String s, Object... arguments) {
        if (this.isVerbose()) {
            wrapped.info(s, arguments);
        } else {
            wrapped.debug(s, arguments);
        }
    }

    public boolean isDebug() {
        return this.level.gte(Verbosity.DEBUG) || wrapped.isDebugEnabled();
    }

    public void debug(String s, Object... arguments) {
        if (this.isDebug()) {
            wrapped.info(s, arguments);
        } else {
            wrapped.debug(s, arguments);
        }
    }

    public boolean isTrace() {
        return this.level.gte(Verbosity.TRACE) || wrapped.isTraceEnabled();
    }

    public void trace(String s, Object... arguments) {
        if (this.isTrace()) {
            wrapped.info(s, arguments);
        } else {
            wrapped.trace(s, arguments);
        }
    }

    public boolean isWarn() {
        return wrapped.isWarnEnabled();
    }

    public void warn(String s, Object... arguments) {
        wrapped.warn(s, arguments);
    }

    public boolean isError() {
        return wrapped.isErrorEnabled();
    }

    public void error(String s, Object... arguments) {
        wrapped.error(s, arguments);
    }

}
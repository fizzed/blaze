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

    public void info(String s, Object... arguments) {
        // no conversion needed
        wrapped.info(s, arguments);
    }

    public void verbose(String s, Object... arguments) {
        if (this.level.gte(Verbosity.VERBOSE)) {
            wrapped.info(s, arguments);
        } else {
            wrapped.debug(s, arguments);
        }
    }

    public void debug(String s, Object... arguments) {
        if (this.level.gte(Verbosity.DEBUG)) {
            wrapped.info(s, arguments);
        } else {
            wrapped.debug(s, arguments);
        }
    }

    public void trace(String s, Object... arguments) {
        if (this.level.gte(Verbosity.TRACE)) {
            wrapped.info(s, arguments);
        } else {
            wrapped.trace(s, arguments);
        }
    }

    public void warn(String s, Object... arguments) {
        wrapped.warn(s, arguments);
    }

    public void error(String s, Object... arguments) {
        wrapped.error(s, arguments);
    }

}
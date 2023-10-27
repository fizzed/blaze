package com.fizzed.blaze.core;

public enum Verbosity {

    DEFAULT(0),
    VERBOSE(1),
    DEBUG(2),
    TRACE(3);

    final int level;

    Verbosity(int level) {
        this.level = level;
    }

    public int getLevel() {
        return this.level;
    }

    public Verbosity from(int value) {
        for (Verbosity v : Verbosity.values()) {
            if (value == v.level) {
                return v;
            }
        }
        throw new IllegalArgumentException("Unable to find Verbosity for value " + value);
    }

    public boolean gte(Verbosity other) {
        return this.level >= other.level;
    }

}
package com.fizzed.blaze.util;

public class IntRange {

    private final Integer from;
    private final Integer to;

    public IntRange(Integer from, Integer to) {
        this.from = from;
        this.to = to;
    }

    public Integer getFrom() {
        return from;
    }

    public Integer getTo() {
        return to;
    }

    public boolean matches(Integer value) {
        return value >= from && value <= to;
    }

    static public IntRange intRange(Integer from, Integer to) {
        return new IntRange(from, to);
    }

}

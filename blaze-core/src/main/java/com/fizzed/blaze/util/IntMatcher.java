package com.fizzed.blaze.util;

public class IntMatcher {

    private final Integer from;
    private final Integer to;

    public IntMatcher(Integer from, Integer to) {
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

}

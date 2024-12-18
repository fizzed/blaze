package com.fizzed.blaze.util;

public class IntRange {

    private final Integer from;
    private final Integer to;

    public IntRange(Integer from, Integer to) {
        this.from = from;
        this.to = to;
        if (from > to) {
            throw new IllegalArgumentException("From must be less than to");
        }
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

    @Override
    public String toString() {
        return from + "->" + to;
    }

    @Override
    public final boolean equals(Object o) {
        if (!(o instanceof IntRange)) return false;

        IntRange intRange = (IntRange) o;
        return from.equals(intRange.from) && to.equals(intRange.to);
    }

    @Override
    public int hashCode() {
        int result = from.hashCode();
        result = 31 * result + to.hashCode();
        return result;
    }

    static public IntRange intRange(Integer from, Integer to) {
        return new IntRange(from, to);
    }

}

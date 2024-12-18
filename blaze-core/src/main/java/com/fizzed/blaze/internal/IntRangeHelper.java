package com.fizzed.blaze.internal;

import com.fizzed.blaze.util.IntRange;

import java.util.ArrayList;
import java.util.List;

public class IntRangeHelper {

    static public boolean contains(List<IntRange> values, Integer value) {
        for (IntRange range : values) {
            if (range.matches(value)) {
                return true;
            }
        }
        return false;
    }

    static public Integer[] toExpandedArray(List<IntRange> values) {
        // create an array of the entire int range
        final List<Integer> _values = new ArrayList<>();
        for (IntRange v : values) {
            for (int i = v.getFrom(); i <= v.getTo(); i++) {
                _values.add(i);
            }
        }
        return _values.toArray(new Integer[0]);
    }

}

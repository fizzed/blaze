package com.fizzed.blaze.jsync;

import com.fizzed.blaze.Contexts;

public class Jsyncs {

    static public Jsync jsync() {
        return new Jsync(Contexts.currentContext());
    }

}
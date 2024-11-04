package com.fizzed.blaze.util;

public interface ProcessHelper {

    void destroy(Process process, long normalTerminationTimeoutMillis) throws InterruptedException;

    void destroyWithDescendants(Process process, long normalTerminationTimeoutMillis) throws InterruptedException;

    static ProcessHelper get() {
        if (ProcessHelper9.isAvailable()) {
            return new ProcessHelper9();
        } else {
            return new ProcessHelper8();
        }
    }

}
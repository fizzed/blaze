package com.fizzed.blaze.vfs;

import java.io.IOException;

public class PathOverwriteException extends IOException {
    public PathOverwriteException(String message) {
        super(message);
    }
}
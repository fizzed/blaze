package com.fizzed.blaze.vfs;

import java.io.IOException;

public class ParentDirectoryMissingException extends IOException {
    public ParentDirectoryMissingException(String message) {
        super(message);
    }
}
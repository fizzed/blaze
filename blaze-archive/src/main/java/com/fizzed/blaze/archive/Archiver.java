package com.fizzed.blaze.archive;

public enum Archiver {

    ZIP(".zip"),
    TAR(".tar"),
    SEVENZ(".7z");

    private final String extension;

    Archiver(String extension) {
        this.extension = extension;
    }

    public String getExtension() {
        return extension;
    }

}
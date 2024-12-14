package com.fizzed.blaze.archive;

public enum Compressor {

    GZ(".gz"),
    BZ2(".bz2"),
    XZ(".xz"),
    ZSTD(".zst");

    private final String extension;

    Compressor(String extension) {
        this.extension = extension;
    }

    public String getExtension() {
        return extension;
    }

}
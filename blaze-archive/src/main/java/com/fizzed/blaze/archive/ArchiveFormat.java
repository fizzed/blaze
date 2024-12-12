package com.fizzed.blaze.archive;

public class ArchiveFormat {

    final private String compressMethod;
    final private String archiveMethod;
    final private String[] extensions;

    public ArchiveFormat(String archiveMethod, String compressMethod, String... extensions) {
        this.compressMethod = compressMethod;
        this.archiveMethod = archiveMethod;
        this.extensions = extensions;
    }

    public String getCompressMethod() {
        return compressMethod;
    }

    public String getArchiveMethod() {
        return archiveMethod;
    }

    public String[] getExtensions() {
        return extensions;
    }

}
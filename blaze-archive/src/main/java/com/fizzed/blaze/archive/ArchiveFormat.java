package com.fizzed.blaze.archive;

public class ArchiveFormat {

    final private Archiver archiver;
    final private Compressor compressor;
    final private String[] extensions;

    public ArchiveFormat(Archiver archiver, Compressor compressor, String... extensions) {
        this.archiver = archiver;
        this.compressor = compressor;
        this.extensions = extensions;
    }

    public Archiver getArchiver() {
        return archiver;
    }

    public Compressor getCompressor() {
        return compressor;
    }

    public String[] getExtensions() {
        return extensions;
    }

}
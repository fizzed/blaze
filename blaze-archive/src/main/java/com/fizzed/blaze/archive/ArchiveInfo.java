package com.fizzed.blaze.archive;

public class ArchiveInfo {

    private Archiver archiver;
    private Compressor compressor;
    private String archivedName;
    private String unarchivedName;

    public ArchiveInfo(Archiver archiver, Compressor compressor, String archivedName, String unarchivedName) {
        this.archiver = archiver;
        this.compressor = compressor;
        this.archivedName = archivedName;
        this.unarchivedName = unarchivedName;
    }

    public Archiver getArchiver() {
        return archiver;
    }

    public ArchiveInfo setArchiver(Archiver archiver) {
        this.archiver = archiver;
        return this;
    }

    public Compressor getCompressor() {
        return compressor;
    }

    public ArchiveInfo setCompressor(Compressor compressor) {
        this.compressor = compressor;
        return this;
    }

    public String getArchivedName() {
        return archivedName;
    }

    public ArchiveInfo setArchivedName(String archivedName) {
        this.archivedName = archivedName;
        return this;
    }

    public String getUnarchivedName() {
        return unarchivedName;
    }

    public ArchiveInfo setUnarchivedName(String unarchivedName) {
        this.unarchivedName = unarchivedName;
        return this;
    }
}
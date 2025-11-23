package com.fizzed.blaze.jsync;

public class JsyncResult {

    private int checksums;
    private int filesCreated;
    private int filesUpdated;
    private int filesDeleted;
    private int dirsCreated;
    private int dirsDeleted;

    public int getChecksums() {
        return checksums;
    }

    public int getFilesCreated() {
        return filesCreated;
    }

    public int getFilesUpdated() {
        return filesUpdated;
    }

    public int getFilesDeleted() {
        return filesDeleted;
    }

    public int getDirsCreated() {
        return dirsCreated;
    }

    public int getDirsDeleted() {
        return dirsDeleted;
    }

    // increment methods for all

    public void incrementChecksums(int amount) {
        checksums += amount;
    }

    public void incrementFilesCreated() {
        filesCreated++;
    }

    public void incrementFilesUpdated() {
        filesUpdated++;
    }

    public void incrementFilesDeleted() {
        filesDeleted++;
    }

    public void incrementDirsCreated() {
        dirsCreated++;
    }

    public void incrementDirsDeleted() {
        dirsDeleted++;
    }

    @Override
    public String toString() {
        return "checksums=" + checksums + ", filesCreated=" + filesCreated + ", filesUpdated=" + filesUpdated + ", filesDeleted=" + filesDeleted + ", dirsCreated=" + dirsCreated + ", dirsDeleted=" + dirsDeleted ;
    }

}
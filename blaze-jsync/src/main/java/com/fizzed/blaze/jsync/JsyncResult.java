package com.fizzed.blaze.jsync;

public class JsyncResult {

    final private JsyncMode mode;
    private int checksums;
    private int filesCreated;
    private int filesUpdated;
    private int filesDeleted;
    private int dirsCreated;
    private int dirsDeleted;
    private int statsUpdated;

    public JsyncResult(JsyncMode mode) {
        this.mode = mode;
    }

    public JsyncMode getMode() {
        return mode;
    }

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

    public int getStatsUpdated() {
        return statsUpdated;
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

    public void incrementStatsUpdated() {
        statsUpdated++;
    }

    @Override
    public String toString() {
        return "checksums=" + checksums + ", filesCreated=" + filesCreated + ", filesUpdated=" + filesUpdated + ", filesDeleted=" + filesDeleted + ", dirsCreated=" + dirsCreated + ", dirsDeleted=" + dirsDeleted + ", statsUpdated=" + statsUpdated;
    }

}
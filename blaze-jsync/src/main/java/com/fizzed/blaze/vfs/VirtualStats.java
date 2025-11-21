package com.fizzed.blaze.vfs;

public class VirtualStats {

    final private long size;
    final private long modifiedTime;
    // there are values that can be populated later
    private Long cksum;

    public VirtualStats(long size, long modifiedTime) {
        this.size = size;
        this.modifiedTime = modifiedTime;
    }

    public long getSize() {
        return size;
    }

    public long getModifiedTime() {
        return modifiedTime;
    }

    public Long getCksum() {
        return cksum;
    }

    public VirtualStats setCksum(Long cksum) {
        this.cksum = cksum;
        return this;
    }

}

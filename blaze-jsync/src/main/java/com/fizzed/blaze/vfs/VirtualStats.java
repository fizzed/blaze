package com.fizzed.blaze.vfs;

public class VirtualStats {

    final private long size;
    final private long modifiedTime;
    final private long accessedTime;
    // there are values that can be populated later
    private Long cksum;
    private String md5;
    private String sha1;

    public VirtualStats(long size, long modifiedTime, long accessedTime) {
        this.size = size;
        this.modifiedTime = modifiedTime;
        this.accessedTime = accessedTime;
    }

    public long getSize() {
        return size;
    }

    public long getModifiedTime() {
        return modifiedTime;
    }

    public long getAccessedTime() {
        return accessedTime;
    }

    public Long getCksum() {
        return cksum;
    }

    public VirtualStats setCksum(Long cksum) {
        this.cksum = cksum;
        return this;
    }

    public String getMd5() {
        return md5;
    }

    public VirtualStats setMd5(String md5) {
        this.md5 = md5;
        return this;
    }

    public String getSha1() {
        return sha1;
    }

    public VirtualStats setSha1(String sha1) {
        this.sha1 = sha1;
        return this;
    }

}

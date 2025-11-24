package com.fizzed.blaze.vfs;

public class VirtualFileStat {

    final private VirtualFileType type;
    final private long size;
    final private long modifiedTime;
    final private long accessedTime;
    // there are values that can be populated later as they are expensive operations
    private Long cksum;
    private String md5;
    private String sha1;

    public VirtualFileStat(VirtualFileType type, long size, long modifiedTime, long accessedTime) {
        this.size = size;
        this.type = type;
        this.modifiedTime = modifiedTime;
        this.accessedTime = accessedTime;
    }

    public VirtualFileType getType() {
        return type;
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

    public VirtualFileStat setCksum(Long cksum) {
        this.cksum = cksum;
        return this;
    }

    public String getMd5() {
        return md5;
    }

    public VirtualFileStat setMd5(String md5) {
        this.md5 = md5;
        return this;
    }

    public String getSha1() {
        return sha1;
    }

    public VirtualFileStat setSha1(String sha1) {
        this.sha1 = sha1;
        return this;
    }

}

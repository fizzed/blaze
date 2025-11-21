package com.fizzed.blaze.vfs;

abstract public class AbstractVirtualFileSystem implements VirtualFileSystem {

    protected final String name;
    protected final VirtualPath pwd;

    public AbstractVirtualFileSystem(String name, VirtualPath pwd) {
        this.name = name;
        this.pwd = pwd;
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public VirtualPath pwd() {
        return this.pwd;
    }

    @Override
    public String toString() {
        return this.name;
    }

}
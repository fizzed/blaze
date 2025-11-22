package com.fizzed.blaze.vfs;

abstract public class AbstractVirtualFileSystem implements VirtualFileSystem {

    protected final String name;
    protected final VirtualPath pwd;
    protected final boolean caseSensitive;

    public AbstractVirtualFileSystem(String name, VirtualPath pwd, boolean caseSensitive) {
        this.name = name;
        this.pwd = pwd;
        this.caseSensitive = caseSensitive;
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
    public boolean isCaseSensitive() {
        return this.caseSensitive;
    }

    @Override
    public String toString() {
        return this.name;
    }

}
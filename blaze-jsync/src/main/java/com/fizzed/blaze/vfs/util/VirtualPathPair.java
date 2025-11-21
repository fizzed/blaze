package com.fizzed.blaze.vfs.util;

import com.fizzed.blaze.vfs.VirtualPath;

public class VirtualPathPair {

    private final VirtualPath source;
    private final VirtualPath target;

    public VirtualPathPair(VirtualPath source, VirtualPath target) {
        this.source = source;
        this.target = target;
    }

    public VirtualPath getSource() {
        return source;
    }

    public VirtualPath getTarget() {
        return target;
    }

}
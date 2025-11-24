package com.fizzed.blaze.jsync;

import com.fizzed.blaze.Contexts;
import com.fizzed.blaze.vfs.VirtualVolume;

public class Jsyncs {

    static public Jsync jsync() {
        return new Jsync(Contexts.currentContext());
    }

    /**
     * Creates and configures a Jsync instance using the specified source, target, and synchronization mode.
     *
     * @param source the source VirtualVolume to synchronize from
     * @param target the target VirtualVolume to synchronize to
     * @param mode the synchronization mode to determine how the synchronization is performed
     * @return a configured Jsync instance ready for execution
     */
    static public Jsync jsync(VirtualVolume source, VirtualVolume target, JsyncMode mode) {
        return new Jsync(Contexts.currentContext())
            .source(source)
            .target(target, mode);
    }

}
package com.fizzed.blaze.archive.zstd;

import com.fizzed.blaze.Systems;
import com.github.luben.zstd.ZstdInputStream;
import com.github.luben.zstd.util.Native;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.atomic.AtomicReference;

public class ZstdUtils {

    static public boolean isZstdAvailable() {
        // check jni first, and "zstd" exe second
        return isZstdJniAvailable() || isZstdNativeAvailable();
    }

    static private final AtomicReference<Boolean> nativeAvailableRef = new AtomicReference<>(null);

    /**
     * Checks whether the native "zstd" executable is available and executable on the system.
     * This method uses a double-checked locking mechanism to determine availability and caches the result.
     *
     * @return true if the "zstd" native executable is available and executable, false otherwise
     */
    static public boolean isZstdNativeAvailable() {
        Boolean available = nativeAvailableRef.get();

        if (available != null) {
            return available;
        }

        synchronized (nativeAvailableRef) {
            // need a double lock here
            available = nativeAvailableRef.get();

            if (available != null) {
                return available;
            }

            try {
                final Path zstdExe = Systems.which("zstd").run();

                available = zstdExe != null && Files.isExecutable(zstdExe);
            } catch (Exception e) {
                available = false;
            }

            nativeAvailableRef.set(available);
            return available;
        }
    }

    static private final AtomicReference<Boolean> jniAvailableRef = new AtomicReference<>(null);

    static public boolean isZstdJniAvailable() {
        Boolean available = jniAvailableRef.get();

        if (available != null) {
            return available;
        }

        synchronized (jniAvailableRef) {
            // need a double lock here
            available = jniAvailableRef.get();

            if (available != null) {
                return available;
            }

            try {
                // leverage zstd-jni, load method to detect if the library  would load correctly
                Native.load();
                available = true;
            } catch (Throwable t) {
                // problem loading library, this is now false
                available = false;
            }

            jniAvailableRef.set(available);
            return available;
        }
    }

}
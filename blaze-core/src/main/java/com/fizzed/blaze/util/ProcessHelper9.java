package com.fizzed.blaze.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class ProcessHelper9 implements ProcessHelper {
    static private final Logger log = LoggerFactory.getLogger(ProcessHelper9.class);

    static public boolean isAvailable() {
        return ProcessHandleReflected.isAvailable();
    }

    @Override
    public void destroy(Process process, long normalTerminationTimeoutMillis) throws InterruptedException {
        final ProcessHandleReflected handle = ProcessHandleReflected.from(process);
        this.destroy(handle, normalTerminationTimeoutMillis);
    }

    private void destroy(ProcessHandleReflected handle, long normalTerminationTimeoutMillis) throws InterruptedException {
        final long pid = handle.pid();
        if (handle.isAlive()) {
            long started = System.currentTimeMillis();
            log.debug("Destroying/killing process {} (trying with normal termination for {} ms)...", pid, normalTerminationTimeoutMillis);
            // try to destroy process normally, then wait till timeout
            handle.destroy();
            boolean killed = new WaitFor(() -> !handle.isAlive()).await(normalTerminationTimeoutMillis, 100L);
            if (!killed) {
                log.debug("Normal termination timed out. Destroying/killing process {} forcibly!", pid);
                handle.destroyForcibly();
            } else {
                log.debug("Destroyed process {} (in {} ms)", pid, (System.currentTimeMillis() - started));
            }
        }
    }

    @Override
    public void destroyWithDescendants(Process process, long normalTerminationTimeoutMillis) throws InterruptedException {
        final ProcessHandleReflected handle = ProcessHandleReflected.from(process);
        final List<ProcessHandleReflected> descendantHandles = handle.descendants();

        // we will destroy the descendants first
        for (ProcessHandleReflected descendantHandle : descendantHandles) {
            this.destroy(descendantHandle, normalTerminationTimeoutMillis);
        }

        // finally, destroy the actual process sent to us
        this.destroy(handle, normalTerminationTimeoutMillis);
    }

}
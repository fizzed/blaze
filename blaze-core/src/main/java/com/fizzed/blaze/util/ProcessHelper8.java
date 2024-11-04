package com.fizzed.blaze.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ProcessHelper8 implements ProcessHelper {
    static private final Logger log = LoggerFactory.getLogger(ProcessHelper8.class);

    @Override
    public void destroy(Process process, long normalTerminationTimeoutMillis) throws InterruptedException {
        if (process.isAlive()) {
            log.debug("Destroying/killing process w/ normal termination {} (will wait {} ms)", process, normalTerminationTimeoutMillis);
            // try to destroy process normally, then wait till timeout
            process.destroy();
            boolean killed = new WaitFor(() -> !process.isAlive()).await(normalTerminationTimeoutMillis, 100L);
            if (!killed) {
                log.debug("Normal termination timed out. Destroying/killing process {} forcibly", process);
                process.destroyForcibly();
            }
        }
    }

    @Override
    public void destroyWithDescendants(Process process, long normalTerminationTimeoutMillis) throws InterruptedException {
        // this is not supported on java 8, so we'll only do the main process
        log.debug("Destroying processes with descendants is only supported on Java 9+ (so we will only destroy parent process instead)");
        this.destroy(process, normalTerminationTimeoutMillis);
    }

}
package com.fizzed.blaze.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.locks.ReentrantLock;

public class ProcessReaper {
    static private final Logger log = LoggerFactory.getLogger(ProcessReaper.class);

    private boolean shutdownHookAdded;
    private final Set<Process> processes;
    private final ReentrantLock lock;

    static public final ProcessReaper INSTANCE = new ProcessReaper();

    public ProcessReaper() {
        this.processes = new HashSet<>();
        this.lock = new ReentrantLock();
    }

    public void register(Process process) {
        this.lock.lock();
        try {
            this.processes.add(process);

            // we will also register ourselves as a shutdown hook upon the first registration
            if (!shutdownHookAdded) {
                Runtime.getRuntime().addShutdownHook(this.newShutdownThread());
                shutdownHookAdded = true;
            }
        } finally {
            this.lock.unlock();
        }
    }

    public void unregister(Process process) {
        this.lock.lock();
        try {
            this.processes.remove(process);
        } finally {
            this.lock.unlock();
        }
    }

    private Thread newShutdownThread() {
        return new Thread(() -> {
            if (!this.processes.isEmpty()) {
                log.debug("Will destroy {} process(es) along with its children (to properly cleanup what we started)", this.processes.size());
                ProcessHelper processHelper = ProcessHelper.get();
                for (Process process : this.processes) {
                    try {
                        processHelper.destroyWithDescendants(process, 5000L);
                    } catch (InterruptedException e) {
                        log.error("Interrupted while destroying process", e);
                    }
                }
            }
        });
    }

}
package com.fizzed.blaze.local;

import java.util.HashSet;
import java.util.Set;

public class ProcessDestroyer {

    static public final ProcessDestroyer INSTANCE = new ProcessDestroyer();

    private final Set<Process> processes;

    public ProcessDestroyer() {
        this.processes = new HashSet<>();
    }

    public void addProcess(Process process) {
        synchronized (this) {
            this.processes.add(process);
        }
    }

    public void removeProcess(Process process) {
        synchronized (this) {
            this.processes.remove(process);
        }
    }

}
package com.fizzed.blaze.util;

import java.lang.reflect.Method;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Uses reflection to allow for Java 8 compiled source to leverage Java 9+ process API.
 */
public class ProcessHandleReflected {

    // reflected methods we'll store for faster access
    static private volatile boolean attempted = false;
    static private final Object lock = new Object();
    static private Class<?> processHandleClass = null;
    static private Method processClassToHandleMethod;
    static private Method processHandleClassCurrentMethod;
    static private Method processHandleClassPidMethod;
    static private Method processHandleClassIsAliveMethod;
    static private Method processHandleClassDestroyMethod;
    static private Method processHandleClassDestroyForciblyMethod;
    static private Method processHandleClassDescendantsMethod;

    static public boolean isAvailable() {
        if (!attempted) {
            synchronized (lock) {
                if (!attempted) {
                    try {
                        processHandleClass = Class.forName("java.lang.ProcessHandle");
                        processClassToHandleMethod = Process.class.getMethod("toHandle");
                        processHandleClassCurrentMethod = processHandleClass.getMethod("current");
                        processHandleClassPidMethod = processHandleClass.getMethod("pid");
                        processHandleClassIsAliveMethod = processHandleClass.getMethod("isAlive");
                        processHandleClassDestroyMethod = processHandleClass.getMethod("destroy");
                        processHandleClassDestroyForciblyMethod = processHandleClass.getMethod("destroyForcibly");
                        processHandleClassDescendantsMethod = processHandleClass.getMethod("descendants");
                    } catch (Exception e) {
                        processHandleClass = null;
                    }
                    attempted = true;
                }
            }
        }
        return processHandleClass != null;
    }

    private final Object instance;

    static public ProcessHandleReflected from(Process process) {
        if (!isAvailable()) {
            throw new UnsupportedOperationException("Process API is not available on this JVM (are you running Java 9+ ?)");
        }

        try {
            final Object handle = processClassToHandleMethod.invoke(process);

            return new ProcessHandleReflected(handle);
        } catch (ReflectiveOperationException | SecurityException e) {
            throw new UnsupportedOperationException(e);
        }
    }

    static public ProcessHandleReflected current() {
        if (!isAvailable()) {
            throw new UnsupportedOperationException("Process API is not available on this JVM (are you running Java 9+ ?)");
        }

        try {
            final Object handle = processHandleClassCurrentMethod.invoke(null);

            return new ProcessHandleReflected(handle);
        } catch (ReflectiveOperationException | SecurityException e) {
            throw new UnsupportedOperationException(e);
        }
    }

    public ProcessHandleReflected(Object instance) {
        this.instance = instance;
    }

    public long pid() {
        try {
            final Object value = processHandleClassPidMethod.invoke(this.instance);

            return (long) value;
        } catch (ReflectiveOperationException | SecurityException e) {
            throw new RuntimeException(e);
        }
    }

    public boolean isAlive() {
        try {
            final Object value = processHandleClassIsAliveMethod.invoke(this.instance);

            return (boolean) value;
        } catch (ReflectiveOperationException | SecurityException e) {
            throw new RuntimeException(e);
        }
    }

    public boolean destroy() {
        try {
            final Object value = processHandleClassDestroyMethod.invoke(this.instance);

            return (boolean) value;
        } catch (ReflectiveOperationException | SecurityException e) {
            throw new RuntimeException(e);
        }
    }

    public boolean destroyForcibly() {
        try {
            final Object value = processHandleClassDestroyForciblyMethod.invoke(this.instance);

            return (boolean) value;
        } catch (ReflectiveOperationException | SecurityException e) {
            throw new RuntimeException(e);
        }
    }

    public List<ProcessHandleReflected> descendants() {
        try {
            final Stream<?> valueStream = (Stream<?>)processHandleClassDescendantsMethod.invoke(this.instance);

            return valueStream.map(ProcessHandleReflected::new).collect(Collectors.toList());
        } catch (ReflectiveOperationException | SecurityException e) {
            throw new RuntimeException(e);
        }
    }
}

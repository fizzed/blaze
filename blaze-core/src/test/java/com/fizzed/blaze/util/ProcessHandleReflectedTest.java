package com.fizzed.blaze.util;

import com.fizzed.jne.JavaVersion;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public class ProcessHandleReflectedTest {

    static private final JavaVersion JAVA_VERSION = JavaVersion.current();

    @Test
    public void current() {
        // this should 100% work on Java 9+
        if (JAVA_VERSION.getMajor() >= 9) {
            ProcessHandleReflected currentHandle = ProcessHandleReflected.current();

            long pid = currentHandle.pid();

            assertThat(pid, greaterThan(0L));
        }
    }

    @Test
    public void isAlive() {
        if (JAVA_VERSION.getMajor() >= 9) {
            ProcessHandleReflected currentHandle = ProcessHandleReflected.current();

            boolean alive = currentHandle.isAlive();

            assertThat(alive, is(true));
        }
    }

    @Test
    public void descendants() {
        if (JAVA_VERSION.getMajor() >= 9) {
            ProcessHandleReflected currentHandle = ProcessHandleReflected.current();

            List<ProcessHandleReflected> descendants = currentHandle.descendants();

            // should just be the single jvm process or no descendants at all
            assertThat(descendants.size(), lessThan(2));
        }
    }

}
package com.fizzed.blaze.util;

import com.fizzed.crux.util.Resources;
import com.fizzed.jne.OperatingSystem;
import com.fizzed.jne.PlatformInfo;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zeroturnaround.exec.ProcessExecutor;
import org.zeroturnaround.exec.StartedProcess;

import java.nio.file.Path;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public class ProcessHelperTest {
    static private final Logger log = LoggerFactory.getLogger(ProcessHelperTest.class);

    static private final OperatingSystem OS = PlatformInfo.detectOperatingSystem();
    static private final ProcessHelper PROCESS_HELPER = ProcessHelper.get();

    @Test
    public void destroy() throws Exception {
        Path binFile;
        if (OS == OperatingSystem.WINDOWS) {
            binFile = Resources.file("/bin/echo-sleep-test.bat");
        } else {
            binFile = Resources.file("/bin/echo-sleep-test");
        }

        final StartedProcess startedProcess = new ProcessExecutor()
            .command(binFile.toAbsolutePath().toString())
            .start();

        assertThat(startedProcess.getProcess().isAlive(), is(true));

        PROCESS_HELPER.destroy(startedProcess.getProcess(), 5000L);

        assertThat(startedProcess.getProcess().isAlive(), is(false));
    }

    @Test
    public void destroyWithDescendants() throws Exception {
        Path binFile;
        if (OS == OperatingSystem.WINDOWS) {
            binFile = Resources.file("/bin/echo-sleep-test.bat");
        } else {
            binFile = Resources.file("/bin/echo-sleep-test");
        }

        final StartedProcess startedProcess = new ProcessExecutor()
            .command(binFile.toAbsolutePath().toString())
            .start();

        assertThat(startedProcess.getProcess().isAlive(), is(true));

        PROCESS_HELPER.destroyWithDescendants(startedProcess.getProcess(), 5000L);

        assertThat(startedProcess.getProcess().isAlive(), is(false));
    }

}
// File: BlazeArgumentsTest.java
package com.fizzed.blaze.core;

import org.junit.Test;

import java.nio.file.Paths;

import static java.util.Arrays.asList;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

/**
 * Unit tests for the {@link BlazeArguments#parse(List)} method.
 * This method parses command-line arguments and sets corresponding fields in the BlazeArguments object.
 */
public class BlazeArgumentsTest {

    @Test
    public void parseShowVersionArg() {
        BlazeArguments blazeArguments = BlazeArguments.parse(asList("-v"));

        assertThat(blazeArguments.isShowVersion(), is(true));
    }

    @Test
    public void parseShowHelpArg() {
        BlazeArguments blazeArguments = BlazeArguments.parse(asList("--help"));

        assertTrue(blazeArguments.isShowHelp());
    }

    @Test
    public void parseListTasksArg() {
        BlazeArguments blazeArguments = BlazeArguments.parse(asList("-l"));

        assertTrue(blazeArguments.isListTasks());
    }

    @Test
    public void parseGenerateMavenProjectArg() {
        BlazeArguments blazeArguments = BlazeArguments.parse(asList("--generate-maven-project"));

        assertTrue(blazeArguments.isGenerateMavenProject());
    }

    @Test
    public void parseInstallDirArg() {
        BlazeArguments blazeArguments = BlazeArguments.parse(asList("-i", "/path/to/install"));

        assertEquals(Paths.get("/path/to/install"), blazeArguments.getInstallDir());
    }

    @Test
    public void parseBlazeFileArg() {
        BlazeArguments blazeArguments = BlazeArguments.parse(asList("-f", "/path/to/blaze.java"));

        assertEquals(Paths.get("/path/to/blaze.java"), blazeArguments.getBlazeFile());
    }

    @Test
    public void parseBlazeDirArg() {
        BlazeArguments blazeArguments = BlazeArguments.parse(asList("-d", "/path/to/dir"));

        assertEquals(Paths.get("/path/to/dir"), blazeArguments.getBlazeDir());
    }

    @Test
    public void parseLoggingLevelArg() {
        String[][] argsAndExpected = {
            {"-q", "-1"},
            {"-qq", "-2"},
            {"-x", "1"},
            {"-xx", "2"},
            {"-xxx", "3"}
        };

        for (String[] argsAndLevel : argsAndExpected) {
            BlazeArguments blazeArguments = BlazeArguments.parse(asList(argsAndLevel[0]));
            assertEquals(Integer.parseInt(argsAndLevel[1]), blazeArguments.getLoggingLevel());
        }
    }

    @Test
    public void parseSystemPropertyArg() {
        BlazeArguments blazeArguments = BlazeArguments.parse(asList("-Dkey=value", "-Dflag"));

        assertThat(blazeArguments.getSystemProperties().get("key"), is("value"));
        assertThat(blazeArguments.getSystemProperties().get("flag"), is(nullValue()));
    }

    @Test
    public void parseConfigPropertiesArg() {
        BlazeArguments blazeArguments = BlazeArguments.parse(asList("--config", "value", "--flag"));

        assertThat(blazeArguments.getConfigProperties().get("config"), is("value"));
        assertThat(blazeArguments.getConfigProperties().get("flag"), is("true"));
    }

    @Test
    public void parseTasksArg() {
        BlazeArguments blazeArguments = BlazeArguments.parse(asList("task1", "task2"));

        assertEquals("task1", blazeArguments.getTasks().get(0));
        assertEquals("task2", blazeArguments.getTasks().get(1));
    }

    @Test
    public void parseBlazeFileDetection() {
        BlazeArguments blazeArguments = BlazeArguments.parse(asList("script.java"));

        assertThat(blazeArguments.getBlazeFile(), is(Paths.get("script.java")));

        // 2nd one should be interpreted as a task name
        try {
            BlazeArguments.parse(asList("script.java", "script.java"));
            fail();
        } catch (IllegalArgumentException e) {
            // expected
            assertThat(e.getMessage(), containsString("Task name"));
        }

        // should also not be a script if -f blaze.java was used
        try {
            BlazeArguments.parse(asList("-f", "script1.java", "script.java"));
            fail();
        } catch (IllegalArgumentException e) {
            // expected
            assertThat(e.getMessage(), containsString("Task name"));
        }
    }

    @Test
    public void parseInvalidTaskName() {
        try {
            BlazeArguments.parse(asList("1task"));
            fail();
        } catch (IllegalArgumentException e) {
            // expected
            assertThat(e.getMessage(), containsString("Task name"));
        }
    }

    @Test
    public void parseIgnoreBlankStrings() {
        // for bash completion, its possible empty command line arguments are provided and we don't want to break
        // they should just be ignored and skipped like they don't exist
        BlazeArguments blazeArguments = BlazeArguments.parse(asList("task1", "", "   ", "task2", "  --flag1  ", "--arg2", "  2  "));

        assertThat(blazeArguments.isShowVersion(), is(false));
        assertThat(blazeArguments.getTasks(), contains("task1", "task2"));
        assertThat(blazeArguments.getConfigProperties(), aMapWithSize(2));
        assertThat(blazeArguments.getConfigProperties().get("flag1"), is("true"));
        assertThat(blazeArguments.getConfigProperties().get("arg2"), is("2"));
        assertThat(blazeArguments.getConfigProperties(), not(hasKey("task1")));
        assertThat(blazeArguments.getConfigProperties(), not(hasKey("task2")));
    }

    @Test
    public void parseRealWorld1() {
        BlazeArguments blazeArguments = BlazeArguments.parse(asList("task1", "task2", "--version", "hijacked", "--flag1"));

        assertThat(blazeArguments.isShowVersion(), is(false));
        assertThat(blazeArguments.getTasks(), contains("task1", "task2"));
        assertThat(blazeArguments.getConfigProperties(), aMapWithSize(2));
        assertThat(blazeArguments.getConfigProperties().get("version"), is("hijacked"));
        assertThat(blazeArguments.getConfigProperties().get("flag1"), is("true"));
        assertThat(blazeArguments.getConfigProperties(), not(hasKey("task1")));
        assertThat(blazeArguments.getConfigProperties(), not(hasKey("task2")));

        // NOTE: if we want to support multiple flags at once with the '-' command and then letters, we'd remove this check
        try {
            BlazeArguments.parse(asList("task1", "-v"));
            fail();
        } catch (IllegalArgumentException e) {
            assertThat(e.getMessage(), containsString("Arguments must"));
        }

        // invalid config key
        try {
            BlazeArguments.parse(asList("task1", "--"));
            fail();
        } catch (IllegalArgumentException e) {
            assertThat(e.getMessage(), containsString("Argument name"));
        }

        // invalid config key
        try {
            BlazeArguments.parse(asList("task1", "--_"));
            fail();
        } catch (IllegalArgumentException e) {
            assertThat(e.getMessage(), containsString("Argument name"));
        }

        // invalid config key
        try {
            BlazeArguments.parse(asList("task1", "--&"));
            fail();
        } catch (IllegalArgumentException e) {
            assertThat(e.getMessage(), containsString("Argument name"));
        }

        // invalid config key
        try {
            BlazeArguments.parse(asList("task1", "-- "));
            fail();
        } catch (IllegalArgumentException e) {
            assertThat(e.getMessage(), containsString("Argument name"));
        }
    }
}
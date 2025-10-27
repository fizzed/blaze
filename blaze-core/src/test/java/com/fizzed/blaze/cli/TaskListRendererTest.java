package com.fizzed.blaze.cli;

import com.fizzed.blaze.core.Blaze;
import com.fizzed.blaze.core.BlazeTask;
import com.fizzed.blaze.core.BlazeTaskGroup;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class TaskListRendererTest {

    @Test
    public void renderWithNoTasks() {
        Blaze mockBlaze = mock(Blaze.class);
        TaskListRenderer renderer = new TaskListRenderer();

        when(mockBlaze.getTasks()).thenReturn(Collections.emptyList());
        when(mockBlaze.getTaskGroups()).thenReturn(Collections.emptyList());

        String result = renderer.render(mockBlaze);

        assertThat(result, is("Available tasks =>\n"));
    }

    @Test
    public void renderWithDefaultGroupOnly() {
        Blaze mockBlaze = mock(Blaze.class);
        TaskListRenderer renderer = new TaskListRenderer();

        BlazeTask task1 = new BlazeTask("task1", "First task description", 0, null);
        BlazeTask task2 = new BlazeTask("task2", null, 0, null);
        List<BlazeTask> tasks = new ArrayList<>();
        tasks.add(task1);
        tasks.add(task2);

        when(mockBlaze.getTasks()).thenReturn(tasks);
        when(mockBlaze.getTaskGroups()).thenReturn(Collections.emptyList());

        String result = renderer.render(mockBlaze);

        String expected = "" +
            "Available tasks =>\n" +
            "  task1     First task description\n" +
            "  task2\n";
        assertThat(result, is(expected));
    }

    @Test
    public void renderWithMultipleGroups() {
        Blaze mockBlaze = mock(Blaze.class);
        TaskListRenderer renderer = new TaskListRenderer();

        BlazeTask task1 = new BlazeTask("task1", "Task in group 1", 0, "group1");
        BlazeTask task2 = new BlazeTask("task2", "Task in group 2", 0, "group2");
        List<BlazeTask> tasks = new ArrayList<>();
        tasks.add(task1);
        tasks.add(task2);

        BlazeTaskGroup group1 = new BlazeTaskGroup("group1", "Group 1", 1);
        BlazeTaskGroup group2 = new BlazeTaskGroup("group2", "Group 2", 2);
        List<BlazeTaskGroup> taskGroups = new ArrayList<>();
        taskGroups.add(group1);
        taskGroups.add(group2);

        when(mockBlaze.getTasks()).thenReturn(tasks);
        when(mockBlaze.getTaskGroups()).thenReturn(taskGroups);

        String result = renderer.render(mockBlaze);

        String expected = "" +
            "Group 1 tasks =>\n" +
            "  task1     Task in group 1\n" +
            "\n" +
            "Group 2 tasks =>\n" +
            "  task2     Task in group 2\n";
        assertThat(result, is(expected));
    }

    @Test
    public void renderWithMultipleGroupsSortedByOrder() {
        Blaze mockBlaze = mock(Blaze.class);
        TaskListRenderer renderer = new TaskListRenderer();

        BlazeTask task1 = new BlazeTask("task1", "Task in group 1", 2, "group1");
        BlazeTask task2 = new BlazeTask("task2", "Task in group 2", 0, "group2");
        List<BlazeTask> tasks = new ArrayList<>();
        tasks.add(task1);
        tasks.add(task2);

        BlazeTaskGroup group1 = new BlazeTaskGroup("group1", "Group 1", 2);
        BlazeTaskGroup group2 = new BlazeTaskGroup("group2", "Group 2", 1);
        List<BlazeTaskGroup> taskGroups = new ArrayList<>();
        taskGroups.add(group1);
        taskGroups.add(group2);

        when(mockBlaze.getTasks()).thenReturn(tasks);
        when(mockBlaze.getTaskGroups()).thenReturn(taskGroups);

        String result = renderer.render(mockBlaze);

        String expected = "" +
            "Group 2 tasks =>\n" +
            "  task2     Task in group 2\n" +
            "\n" +
            "Group 1 tasks =>\n" +
            "  task1     Task in group 1\n";
        assertThat(result, is(expected));
    }

    @Test
    public void renderWithUnmappedGroup() {
        Blaze mockBlaze = mock(Blaze.class);
        TaskListRenderer renderer = new TaskListRenderer();

        BlazeTask task1 = new BlazeTask("task1", "Task in group 1", 0, "group1");
        BlazeTask task2 = new BlazeTask("task2", "Task in unmapped group", 0, null);
        List<BlazeTask> tasks = new ArrayList<>();
        tasks.add(task1);
        tasks.add(task2);

        BlazeTaskGroup group1 = new BlazeTaskGroup("group1", "Group 1", 1);
        List<BlazeTaskGroup> taskGroups = new ArrayList<>();
        taskGroups.add(group1);

        when(mockBlaze.getTasks()).thenReturn(tasks);
        when(mockBlaze.getTaskGroups()).thenReturn(taskGroups);

        String result = renderer.render(mockBlaze);

        String expected = "" +
            "Group 1 tasks =>\n" +
            "  task1     Task in group 1\n" +
            "\n" +
            "Other tasks =>\n" +
            "  task2     Task in unmapped group\n";
        assertThat(result, is(expected));
    }

    @Test
    public void renderWithGroupsButNoTaskGroups() {
        Blaze mockBlaze = mock(Blaze.class);
        TaskListRenderer renderer = new TaskListRenderer();

        BlazeTask task1 = new BlazeTask("task1", "Task in group 1", 4, "group1");
        BlazeTask task2 = new BlazeTask("task2", "Task in group 2", 0, "group2");
        List<BlazeTask> tasks = new ArrayList<>();
        tasks.add(task1);
        tasks.add(task2);

        List<BlazeTaskGroup> taskGroups = new ArrayList<>();

        when(mockBlaze.getTasks()).thenReturn(tasks);
        when(mockBlaze.getTaskGroups()).thenReturn(taskGroups);

        String result = renderer.render(mockBlaze);

        String expected = "" +
            "group1 tasks =>\n" +
            "  task1     Task in group 1\n" +
            "\n" +
            "group2 tasks =>\n" +
            "  task2     Task in group 2\n";
        assertThat(result, is(expected));
    }

}
package com.fizzed.blaze.cli;

import com.fizzed.blaze.TaskGroup;
import com.fizzed.blaze.core.Blaze;
import com.fizzed.blaze.core.BlazeTask;
import com.fizzed.blaze.core.BlazeTaskGroup;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import static com.fizzed.blaze.util.TerminalHelper.padRight;
import static java.util.Optional.ofNullable;

public class TaskListRenderer {

    public String render(Blaze blaze) {
        StringBuilder sb = new StringBuilder();

        final List<BlazeTaskGroup> taskGroups = blaze.getTaskGroups();
        final List<BlazeTask> tasks = blaze.getTasks();

        // calculate max width of task name (so everything is aligned)
        int maxTaskNameWidth = 0;
        for (BlazeTask t : tasks) {
            maxTaskNameWidth =  Math.max(t.getName().length(), maxTaskNameWidth);
        }

        // collect all the group ids that are present on tasks
        final Set<String> groupIds = tasks.stream()
            .map(BlazeTask::getGroup)
            .collect(Collectors.toSet());

        // group mode if more than one group is used
        if (groupIds.size() > 1) {
            // is the default group used?
            if (groupIds.contains(null)) {{
                // add the default group to the list
                taskGroups.add(new BlazeTaskGroup(null, "Other", TaskGroup.OTHER_GROUP_ORDER));
            }}

            // remove task groups that are not used
            taskGroups.removeIf(v -> !groupIds.contains(v.getId()));

            // create task groups that are used but not present in the list
            for (String id : groupIds) {
                if (taskGroups.stream().noneMatch(g -> Objects.equals(g.getId(), id))) {
                    taskGroups.add(new BlazeTaskGroup(id, null, TaskGroup.DEFAULT_ORDER));
                }
            }

            // sort the groups by natural order
            Collections.sort(taskGroups);

            int count = 0;
            for (BlazeTaskGroup g : taskGroups) {
                if (count > 0) {
                    sb.append("\n");
                }

                // build a list of tasks for this group
                final List<BlazeTask> tasksInGroup = tasks.stream()
                    .filter(t -> Objects.equals(g.getId(), t.getGroup()))
                    .collect(Collectors.toList());

                this.renderGroup(sb, g.toString(), tasksInGroup, maxTaskNameWidth);

                count++;
            }
        } else {
            // non-group mode
            this.renderGroup(sb, "Available", tasks, maxTaskNameWidth);
        }

        return sb.toString();
    }

    private void renderGroup(StringBuilder sb, String groupName, List<BlazeTask> tasks, int maxTaskNameWidth) {
        sb.append(groupName).append(" tasks =>\n");

        Collections.sort(tasks);

        // output task name & description w/ padding
        for (BlazeTask t : tasks) {
            if (t.getDescription() != null) {
                sb.append("  ").append(padRight(t.getName(), maxTaskNameWidth + 5)).append(t.getDescription()).append("\n");
            } else {
                sb.append("  ").append(t.getName()).append("\n");
            }
        }
    }

}
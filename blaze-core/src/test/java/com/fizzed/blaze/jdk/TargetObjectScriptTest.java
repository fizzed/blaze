package com.fizzed.blaze.jdk;

import com.fizzed.blaze.core.BlazeTask;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.hamcrest.CoreMatchers.hasItems;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;

public class TargetObjectScriptTest {

    static public class TestA {

        static public void helloStatic() {}

        public void helloWorldA() {}

    }

    static public class TestB extends TestA {

        static public void helloStatic() {}

        public void helloWorldB() {}

    }

    @Test
    public void tasksFromA() {
        TargetObjectScript tos = new TargetObjectScript(new TestA());

        List<BlazeTask> tasks = tos.tasks();

        assertThat(tasks, hasSize(1));
        assertThat(tasks.get(0).getName(), is("helloWorldA"));
    }

    @Test
    public void tasksFromB() {
        TargetObjectScript tos = new TargetObjectScript(new TestB());

        List<BlazeTask> tasks = tos.tasks();

        assertThat(tasks, hasSize(2));

        // get a hashset of names
        Set<String> taskNames = tasks.stream().map(v -> v.getName()).collect(Collectors.toSet());

        assertThat(taskNames, hasItems("helloWorldB", "helloWorldA"));
    }

}
/*
 * Copyright 2015 Fizzed, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import com.fizzed.blaze.Contexts;
import static com.fizzed.blaze.Contexts.withBaseDir;

import com.fizzed.blaze.Task;
import com.fizzed.blaze.TaskGroup;
import com.fizzed.blaze.core.Blaze;
import com.fizzed.blaze.project.PublicBlaze;

import java.util.List;
import java.util.stream.Collectors;

import com.fizzed.buildx.Target;

@TaskGroup(value="project", order=1, name="Project")
@TaskGroup(value="maintainers", order=2, name="Maintainers Only")
public class blaze extends PublicBlaze {

    @Task(group="project", order=1, value="Hello, world! example")
    public void hello() {
        log.info("Hello, world!");
    }

    @Task(group="project", order=2, value="Try all scripts in examples/ dir")
    public void try_all() throws Exception {
        // execute another blaze script in this jvm
        new Blaze.Builder()
            .file(withBaseDir("../examples/try_all.java"))
            .build()
            .execute();

        Contexts.withBaseDir(".");
    }

    @Task(group="project", order=3, value="Run unit tests across all LTS Java releases")
    public void jdk_tests() throws Exception {
        super.jdk_tests();
    }

    @Override
    protected List<Target> crossTestTargets() {
        return super.crossTestTargets().stream()
            .filter(v -> !v.getArch().contains("riscv64"))
            .filter(v -> !v.getOs().contains("freebsd"))
            .filter(v -> !v.getOs().contains("openbsd"))
            .collect(Collectors.toList());
    }

}
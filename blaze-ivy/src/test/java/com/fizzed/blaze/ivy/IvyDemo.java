/*
 * Copyright 2016 Fizzed, Inc.
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
package com.fizzed.blaze.ivy;

import com.fizzed.blaze.Config;
import com.fizzed.blaze.Config.Value;
import com.fizzed.blaze.Context;
import com.fizzed.blaze.Contexts;
import com.fizzed.blaze.Systems;
import com.fizzed.blaze.core.Dependency;
import com.fizzed.blaze.internal.ContextImpl;
import com.fizzed.blaze.logging.LogLevel;
import com.fizzed.blaze.logging.LoggerConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class IvyDemo {
    
    static public void main(String[] args) throws Exception {
        LoggerConfig.setDefaultLogLevel(LogLevel.TRACE);

        Context context = Contexts.currentContext();

        IvyDependencyResolver resolver = new IvyDependencyResolver();

        List<Dependency> dependencies = asList(
            Dependency.parse("com.fizzed:cloudns:0.0.1")
        );

        // clear maven local
        //Systems.rm(Paths.get("/home/jjlauer/.m2/repository/com/fizzed/crux-util")).recursive().force().run();
        // clear ivy cache
        //Systems.rm(Paths.get("/home/jjlauer/.blaze/ivy2")).recursive().force().run();

        final List<File> artifacts = resolver.resolve(context, emptyList(), dependencies);

        for (File artifact : artifacts) {
            System.out.println(artifact);
        }
    }

}

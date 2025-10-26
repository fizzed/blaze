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
package com.fizzed.blaze.internal;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;

import org.junit.jupiter.api.Test;

public class ContextImplTest {

    @Test
    public void dirIfExists() {
        assertThat(ContextImpl.dirIfExists(null), is(Optional.empty()));
        assertThat(ContextImpl.dirIfExists("."), is(Optional.of(Paths.get("."))));
    }
    
    @Test
    public void findUserDir() {
        Path userDir = ContextImpl.findUserDir();
        assertThat(userDir, is(not(nullValue())));
        assertThat(Files.isDirectory(userDir), is(true));
    }
    
}

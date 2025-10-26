/*
 * Copyright 2020 Fizzed, Inc.
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
package com.fizzed.blaze.docker;

import com.fizzed.blaze.Contexts;
import com.fizzed.blaze.core.BlazeException;
import com.fizzed.blaze.util.MutableUri;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

public class DockerExecTest {
 
    @Test
    public void noDockerContainer() {
        DockerTestHelper.assumeDockerIsPresent();
    
        DockerSession session = new DockerSession(Contexts.currentContext(), new MutableUri("docker://nosuchcontainer"));
    
        BlazeException exception = assertThrows(BlazeException.class, () -> {
            session.newExec()
                .command("bash")
                .runCaptureOutput()
                .asString();
        });
        
        assertThat(exception.getMessage(), containsString("is not running"));
    }

}
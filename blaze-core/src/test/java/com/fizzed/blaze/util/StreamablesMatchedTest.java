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
package com.fizzed.blaze.util;

import com.fizzed.blaze.internal.FileHelper;
import static com.fizzed.blaze.util.Streamables.input;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertThat;
import org.junit.Test;

public class StreamablesMatchedTest {
    
    @Test
    public void matchedLines() throws Exception {
        Path path = FileHelper.resourceAsPath("/fixtures/pattern.txt");
        
        // find lines containing something like "lite-0.1.0.jar"
        List<String> lines
            = Streamables.matchedLines(input(path), ".*lite-\\d+\\.\\d+\\.\\d+\\.jar.*")
                .collect(Collectors.toList());
        
        assertThat(lines, hasSize(3));
    }
    
    @Test
    public void matchedLinesExtractGroup() throws Exception {
        Path path = FileHelper.resourceAsPath("/fixtures/pattern.txt");
        
        List<String> lines
            = Streamables.matchedLines(input(path), ".*lite-(\\d+\\.\\d+\\.\\d+)\\.jar.*", (m) -> m.group(1))
                .collect(Collectors.toList());
        
        assertThat(lines, hasSize(3));
        assertThat(lines.get(0), is("0.9.1"));
    }
    
    @Test
    public void matchedLinesFirst() throws Exception {
        Path path = FileHelper.resourceAsPath("/fixtures/pattern.txt");
        
        String match
            = Streamables.matchedLines(input(path), ".*lite-(\\d+\\.\\d+\\.\\d+)\\.jar.*", (m) -> m.group(1))
                .findFirst()
                .get();
        
        assertThat(match, is("0.9.1"));
    }
    
}

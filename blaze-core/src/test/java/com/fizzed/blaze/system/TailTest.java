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
package com.fizzed.blaze.system;

import java.io.StringReader;
import java.util.Deque;
import org.apache.commons.io.input.ReaderInputStream;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import org.junit.jupiter.api.Test;

/**
 *
 * @author Joe Lauer
 */
public class TailTest {
    
    @Test
    public void works() throws Exception {
        Tail tail = new Tail(null);
        
        String s = "a\nb\nc\n";
        
        StringReader sr = new StringReader(s);
        
        tail.pipeInput(new ReaderInputStream(sr));
        
        Deque<String> output = tail.run();
        
        assertThat(output.size(), is(3));
        assertThat(output.remove(), is("a"));
        assertThat(output.remove(), is("b"));
        assertThat(output.remove(), is("c"));
    }
    
}
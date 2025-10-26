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
package com.fizzed.blaze.util;

import com.fizzed.blaze.util.LineOutputStream.BufferingProcessor;
import com.fizzed.blaze.util.LineOutputStream.LastLineProcessor;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicInteger;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import org.junit.jupiter.api.Test;

/**
 *
 * @author joelauer
 */
public class LineOutputStreamTest {
    
    @Test
    public void accumulate() throws IOException {
        LineOutputStream<BufferingProcessor> lpos = LineOutputStream.buffering();
        BufferingProcessor processor = lpos.processor();
        
        assertThat(processor.lines(), hasSize(0));
        
        // entire line written in one write (\r\n chopped off)
        lpos.write("hello\r\n".getBytes(StandardCharsets.UTF_8));
        
        assertThat(processor.lines(), hasSize(1));
        assertThat(processor.lines().poll(), is("hello"));
        
        // whole line not witten in 1 call
        lpos.write("hello".getBytes(StandardCharsets.UTF_8));
        
        assertThat(processor.lines(), hasSize(0));
        
        lpos.write("\n".getBytes(StandardCharsets.UTF_8));
        
        assertThat(processor.lines(), hasSize(1));
        assertThat(processor.lines().poll(), is("hello"));
        
        assertThat(processor.lines(), hasSize(0));
        
        String s = "daemon:x:1:1:daemon:/usr/sbin:/usr/sbin/nologin\n" +
                    "bin:x:2:2:bin:/bin:/usr/sbin/no";
        
        lpos.write(s.getBytes(StandardCharsets.UTF_8));
        
        assertThat(processor.lines(), hasSize(1));
        assertThat(processor.lines().poll(), is("daemon:x:1:1:daemon:/usr/sbin:/usr/sbin/nologin"));
        
        lpos.write("\n".getBytes(StandardCharsets.UTF_8));
        
        assertThat(processor.lines(), hasSize(1));
        assertThat(processor.lines().poll(), is("bin:x:2:2:bin:/bin:/usr/sbin/no"));
    }
    
    @Test
    public void processor() throws IOException {
        AtomicInteger count = new AtomicInteger(0);
        
        LineOutputStream lpos = new LineOutputStream(
            (line) -> count.incrementAndGet()
        );
        
        assertThat(count.get(), is(0));
        
        lpos.write("hello\r\n".getBytes(StandardCharsets.UTF_8));
        
        assertThat(count.get(), is(1));
    }
    
    @Test
    public void lastLine() throws IOException {
        LineOutputStream<LastLineProcessor> lpos = LineOutputStream.lastLine();

        lpos.write("hello\r\n".getBytes(StandardCharsets.UTF_8));
        
        assertThat(lpos.processor().lastLine(), is("hello"));
        
        lpos.write("world\r\n".getBytes(StandardCharsets.UTF_8));
        
        assertThat(lpos.processor().lastLine(), is("world"));
    }
    
}

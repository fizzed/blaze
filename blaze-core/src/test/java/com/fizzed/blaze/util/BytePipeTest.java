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

import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import org.junit.Test;

public class BytePipeTest {
    
    @Test
    public void worksWith1() throws Exception {
        BytePipe pipe = new BytePipe(1);
        final OutputStream os = pipe.getOutputStream();
        final InputStream is = pipe.getInputStream();
        final byte[] bytes = new byte[100];
        int read;
        
        os.write("h".getBytes(StandardCharsets.UTF_8));
        
        read = is.read(bytes);
        
        assertThat(new String(bytes, 0, read, StandardCharsets.UTF_8), is("h"));
        
        os.write("e".getBytes(StandardCharsets.UTF_8));
        
        read = is.read(bytes);
        
        assertThat(new String(bytes, 0, read, StandardCharsets.UTF_8), is("e"));
    }
    
    @Test
    public void worksWith2() throws Exception {
        BytePipe pipe = new BytePipe(2);
        final OutputStream os = pipe.getOutputStream();
        final InputStream is = pipe.getInputStream();
        final byte[] bytes = new byte[100];
        int read;
        
        os.write("h".getBytes(StandardCharsets.UTF_8));
        
        read = is.read(bytes);
        
        assertThat(new String(bytes, 0, read, StandardCharsets.UTF_8), is("h"));
        
        os.write("e".getBytes(StandardCharsets.UTF_8));
        
        read = is.read(bytes);
        
        assertThat(new String(bytes, 0, read, StandardCharsets.UTF_8), is("e"));
        
        os.write("ll".getBytes(StandardCharsets.UTF_8));
        
        read = is.read(bytes);
        
        assertThat(new String(bytes, 0, read, StandardCharsets.UTF_8), is("ll"));
    }
    
}

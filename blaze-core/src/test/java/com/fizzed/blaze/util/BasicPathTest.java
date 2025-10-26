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

import java.nio.file.Paths;
import java.util.List;
import org.junit.jupiter.api.Test;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.fail;

/**
 *
 * @author joelauer
 */
public class BasicPathTest {
    
    @Test
    public void split() {
        List<String> result;
        
        try {
            BasicPaths.split((String)null);
            fail();
        } catch (IllegalArgumentException e) {
            // expected
        }
        
        try {
            BasicPaths.split("");
            fail();
        } catch (IllegalArgumentException e) {
            // expected
        }
        
        result = BasicPaths.split("/");
        assertThat(result, hasSize(2));
        assertThat(result.get(0), is(""));
        assertThat(result.get(1), is(""));
        
        result = BasicPaths.split("test");
        assertThat(result, hasSize(1));
        assertThat(result.get(0), is("test"));
        
        result = BasicPaths.split("this/test");
        assertThat(result, hasSize(2));
        assertThat(result.get(0), is("this"));
        assertThat(result.get(1), is("test"));
        
        result = BasicPaths.split("/this/test");
        assertThat(result, hasSize(3));
        assertThat(result.get(0), is(""));
        assertThat(result.get(1), is("this"));
        assertThat(result.get(2), is("test"));
        
        result = BasicPaths.split("this/test/");
        assertThat(result, hasSize(3));
        assertThat(result.get(0), is("this"));
        assertThat(result.get(1), is("test"));
        assertThat(result.get(2), is(""));
        
        result = BasicPaths.split("test/**");
        assertThat(result, hasSize(2));
        assertThat(result.get(0), is("test"));
        assertThat(result.get(1), is("**"));
        
        result = BasicPaths.split("test/.java");
        assertThat(result, hasSize(2));
        assertThat(result.get(0), is("test"));
        assertThat(result.get(1), is(".java"));
        
        // escaped
        
        result = BasicPaths.split("te\\/st");
        assertThat(result, hasSize(1));
        assertThat(result.get(0), is("te/st"));
        
        result = BasicPaths.split("\\/");
        assertThat(result, hasSize(1));
        assertThat(result.get(0), is("/"));
    }
    
    @Test
    public void isAbsolute() {
        List<String> result;
        
        result = BasicPaths.split("/");
        assertThat(BasicPaths.isAbsolute(result), is(true));
        
        result = BasicPaths.split("test");
        assertThat(BasicPaths.isAbsolute(result), is(false));
        
        result = BasicPaths.split("/test");
        assertThat(BasicPaths.isAbsolute(result), is(true));
    }
    
    @Test
    public void toStringTest() {
        List<String> result;
        
        result = BasicPaths.split("/");
        assertThat(BasicPaths.toString(result), is("/"));
        
        result = BasicPaths.split("test");
        assertThat(BasicPaths.toString(result), is("test"));
        
        result = BasicPaths.split("/test");
        assertThat(BasicPaths.toString(result), is("/test"));
        
        result = BasicPaths.split("/test/");
        assertThat(BasicPaths.toString(result), is("/test/"));
        
        result = BasicPaths.split("/test/**");
        assertThat(BasicPaths.toString(result), is("/test/**"));
    }
    
    @Test
    public void toPath() {
        List<String> result;
        
        result = BasicPaths.split("/");
        assertThat(BasicPaths.toPath(result), is(Paths.get("/")));
        
        result = BasicPaths.split("test");
        assertThat(BasicPaths.toPath(result), is(Paths.get("test")));
        
        result = BasicPaths.split("/test");
        assertThat(BasicPaths.toPath(result), is(Paths.get("/test")));
        
        result = BasicPaths.split("/test/");
        assertThat(BasicPaths.toPath(result), is(Paths.get("/test/")));
    }
   
}

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
package com.fizzed.blaze.http;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import org.junit.Test;

/**
 *
 * @author joelauer
 */
public class UriBuilderTest {
    
    @Test
    public void of() {
        String uri;
        
        uri = UriBuilder.of("http://localhost:8080").toString();
        
        assertThat(uri, is("http://localhost:8080"));
        
        uri = UriBuilder.of("http://localhost:{}", 8080).toString();
        
        assertThat(uri, is("http://localhost:8080"));
        
        uri = UriBuilder.of("http://localhost:{}/", 8080).toString();
        
        assertThat(uri, is("http://localhost:8080/"));
        
        uri = UriBuilder.of("https://{}:{}/", "localhost", 8080).toString();
        
        assertThat(uri, is("https://localhost:8080/"));
        
        uri = UriBuilder.of("https://{}:{}/path?a={}", "localhost", 8080, 1).toString();
        
        assertThat(uri, is("https://localhost:8080/path?a=1"));
        
        // does it encode?
        uri = UriBuilder.of("https://{}:{}/path?a={}", "localhost", 8080, "=&").toString();
        
        assertThat(uri, is("https://localhost:8080/path?a=%3D%26"));
    }
    
    /**
    @Test
    public void parse() throws Exception {
        UriBuilder ub;
        
        ub = UriBuilder.of("http://localhost:8080");
        
        assertThat(ub.toString(), is("http://localhost:8080"));
        
        ub.parse("/path");
        
        assertThat(ub.toString(), is("http://localhost:8080/path"));
        
        ub.parse("?a=1");
        
        assertThat(ub.toString(), is("http://localhost:8080/path?a=1"));
    }
    */
    
}

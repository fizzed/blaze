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
package com.fizzed.blaze.core;

import com.fizzed.blaze.core.MutableUri;
import java.net.URI;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import org.junit.Test;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

/**
 *
 * @author joelauer
 */
public class MutableUriTest {
    
    @Test
    public void of() {
        String uri;

        uri = MutableUri.of("http://localhost:8080").toString();
        
        assertThat(uri, is("http://localhost:8080"));
        
        uri = MutableUri.of("http://localhost:{}", 8080).toString();
        
        assertThat(uri, is("http://localhost:8080"));
        
        uri = MutableUri.of("http://localhost:{}/", 8080).toString();
        
        assertThat(uri, is("http://localhost:8080/"));
        
        uri = MutableUri.of("https://{}:{}/", "localhost", 8080).toString();
        
        assertThat(uri, is("https://localhost:8080/"));
        
        uri = MutableUri.of("https://{}:{}/path?a={}", "localhost", 8080, 1).toString();
        
        assertThat(uri, is("https://localhost:8080/path?a=1"));

        // does it encode?
        uri = MutableUri.of("https://{}:{}/path?a={}", "localhost", 8080, "=&").toString();
        
        assertThat(uri, is("https://localhost:8080/path?a=%3D%26"));
        
        uri = MutableUri.of("https://{}:{}/path?a={}#{}", "localhost", 8080, "=&", "page").toString();
        
        assertThat(uri, is("https://localhost:8080/path?a=%3D%26#page"));
    }
    
    @Test
    public void uriAndUriBuilderMatch() {
        URI uri;
        
        uri = MutableUri.of("http://localhost:8080").build();
        
        assertThat(uri, is(URI.create("http://localhost:8080")));
        
        uri = MutableUri.of("https://localhost:8080").build();
        
        assertThat(uri, is(URI.create("https://localhost:8080")));
        
        uri = MutableUri.of("https://localhost/path").build();
        
        assertThat(uri, is(URI.create("https://localhost/path")));
        
        uri = MutableUri.of("http://localhost:80/path").build();
        
        assertThat(uri, is(URI.create("http://localhost:80/path")));
        
        uri = MutableUri.of("http://localhost:80/path?a=1&b=2").build();
        
        assertThat(uri, is(URI.create("http://localhost:80/path?a=1&b=2")));
        
        uri = MutableUri.of("http://localhost:80/path?a=%3D%26").build();
        
        assertThat(uri, is(URI.create("http://localhost:80/path?a=%3D%26")));
        
        uri = MutableUri.of("http://localhost:80/path?a=%3D%26#page").build();
        
        assertThat(uri, is(URI.create("http://localhost:80/path?a=%3D%26#page")));
        
        uri = MutableUri.of("http://user@localhost/path?a=%3D%26#page").build();
        
        assertThat(uri, is(URI.create("http://user@localhost/path?a=%3D%26#page")));
    }
    
}

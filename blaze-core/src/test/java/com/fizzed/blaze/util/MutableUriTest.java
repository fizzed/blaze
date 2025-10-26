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

import com.fizzed.blaze.util.MutableUri;
import java.net.URI;
import static org.hamcrest.CoreMatchers.is;
import org.junit.jupiter.api.Test;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;

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
    public void matchesJdkURIParsing() {
        URI uri;
        
        uri = MutableUri.of("http://localhost:8080").toURI();
        
        assertThat(uri, is(URI.create("http://localhost:8080")));
        
        uri = MutableUri.of("https://localhost:8080").toURI();
        
        assertThat(uri, is(URI.create("https://localhost:8080")));
        
        uri = MutableUri.of("https://localhost/path").toURI();
        
        assertThat(uri, is(URI.create("https://localhost/path")));
        
        uri = MutableUri.of("http://localhost:80/path").toURI();
        
        assertThat(uri, is(URI.create("http://localhost:80/path")));
        
        uri = MutableUri.of("http://localhost:80/path?a=1&b=2").toURI();
        
        assertThat(uri, is(URI.create("http://localhost:80/path?a=1&b=2")));
        
        uri = MutableUri.of("http://localhost:80/path?a=%3D%26").toURI();
        
        assertThat(uri, is(URI.create("http://localhost:80/path?a=%3D%26")));
        
        uri = MutableUri.of("http://localhost:80/path?a=%3D%26#page").toURI();
        
        assertThat(uri, is(URI.create("http://localhost:80/path?a=%3D%26#page")));
        
        uri = MutableUri.of("http://user@localhost/path?a=%3D%26#page").toURI();
        
        assertThat(uri, is(URI.create("http://user@localhost/path?a=%3D%26#page")));
    }
    
    @Test
    public void opensshStyle() {
        URI uri;
        
        uri = MutableUri.of("ssh://joe@localhost").toURI();
        
        assertThat(uri, is(URI.create("ssh://joe@localhost")));
        assertThat(uri.getUserInfo(), is("joe"));
        assertThat(uri.getHost(), is("localhost"));
 
        /**
        uri = MutableUri.of("joe@localhost").toURI();
        
        assertThat(uri, is(URI.create("//joe@localhost")));
        assertThat(uri.getUserInfo(), is("joe"));
        assertThat(uri.getHost(), is("localhost"));
        assertThat(uri.getPath(), is(""));
        */
    }
    
    @Test
    public void browserStyle() {
        URI uri;
        
        uri = MutableUri.of("//ajax.googleapis.com/ajax/jquery.js").toURI();
        
        assertThat(uri, is(URI.create("//ajax.googleapis.com/ajax/jquery.js")));
        assertThat(uri.getUserInfo(), is(nullValue()));
        assertThat(uri.getHost(), is("ajax.googleapis.com"));
        assertThat(uri.getPath(), is("/ajax/jquery.js"));
    }
    
}

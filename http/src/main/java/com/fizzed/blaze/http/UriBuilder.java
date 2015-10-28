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

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import org.apache.http.client.utils.URIBuilder;

/**
 * Helps to build a URI.  Why another one?  This one has a simple fluent style
 * and accepts Objects for parameter values.  It also has an interesting "format"
 * style it'll accept which does placeholder swapping.
 * 
 * @author joelauer
 */
public class UriBuilder {
    
    final private URIBuilder uriBuilder;

    public UriBuilder() {
        this.uriBuilder = new URIBuilder();
    }
    
    public UriBuilder(URI uri) {
        this.uriBuilder = new URIBuilder(uri);
    }
    
    public UriBuilder(String uri) {
        try {
            this.uriBuilder = new URIBuilder(uri);
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException(e.getMessage());
        }
    }
    
    public UriBuilder(String uri, Object... parameters) {
        this(format(uri, parameters));
    }
    
    public URI build() {
        try {
            return this.uriBuilder.build();
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException(e.getMessage());
        }
    }
    
    @Override
    public String toString() {
        return build().toString();
    }
    
    public UriBuilder scheme(String scheme) {
        this.uriBuilder.setScheme(scheme);
        return this;
    }
    
    public UriBuilder userInfo(String username, String password) {
        this.uriBuilder.setUserInfo(username, password);
        return this;
    }
    
    public UriBuilder host(String host) {
        this.uriBuilder.setHost(host);
        return this;
    }
    
    public UriBuilder port(Integer port) {
        this.uriBuilder.setPort(port);
        return this;
    }
    
    public UriBuilder path(String path) {
        this.uriBuilder.setPath(path);
        return this;
    }
    
    public UriBuilder query(String name, Object value) {
        Objects.requireNonNull(name, "name cannot be null");
        Objects.requireNonNull(value, "value cannot be null");
        this.uriBuilder.addParameter(name, Objects.toString(value));
        return this;
    }
    
    public UriBuilder fragment(String fragment) {
        this.uriBuilder.setFragment(fragment);
        return this;
    }
    
    /** this doesn't really work well
    public UriBuilder parse(String uri, Object... parameters) {
        String s = format(uri, parameters);
        try {
            URI parsed = new URI(s);
            // set what was parsed
            if (parsed.getScheme() != null) {
                this.scheme(parsed.getScheme());
            }
            // TODO: this is probably wrong...
            if (parsed.getUserInfo() != null) {
                this.uriBuilder.setUserInfo(parsed.getUserInfo());
            }
            if (parsed.getHost() != null) {
                this.host(parsed.getHost());
            }
            if (parsed.getPort() >= 0) {
                this.port(parsed.getPort());
            }
            if (parsed.getPath() != null) {
                this.path(parsed.getPath());
            }
            if (parsed.getRawQuery() != null) {
                // clear the current params
                this.uriBuilder.clearParameters();
  
                // split on ampersand...
                String[] pairs = parsed.getRawQuery().split("&");
                for (String pair : pairs) {
                    String[] nv = pair.split("=");
                    if (nv.length != 2) {
                        throw new IllegalArgumentException("Name value pair [" + pair + "] in query [" + parsed.getRawQuery() + "] missing = char");
                    }
                    this.query(nv[0], nv[1]);
                }
            }
            if (parsed.getFragment() != null) {
                this.fragment(parsed.getFragment());
            }
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException(e.getMessage());
        }
        return this;
    }
    */
    
    static public UriBuilder of(String uri, Object... parameters) {
        return new UriBuilder(uri, parameters);
    }
    
    static public URI uri(String uri, Object... parameters) {
        return new UriBuilder(uri, parameters).build();
    }
    
    /**
     * Builds a String that accepts place holders and replaces them with URL encoded
     * values.
     * @param uri A string with place holders (e.g. "http://localhost:{}/path?a={}&b={}", 80, "valForA", "valForB")
     * @param parameters
     * @return 
     */
    static public String format(String uri, Object... parameters) {
        if (parameters == null || parameters.length == 0 || !uri.contains("{}")) {
            return uri;
        }
        
        List<String> tokens = splitter(uri, "{}");
        
        // there should be tokens.length - 1 parameters supplied
        if (tokens.size() - 1 != parameters.length) {
            throw new IllegalArgumentException("Incorrect number of parameters (expected " + (tokens.size() - 1)
                + "; actual " + parameters.length + ")");
        }
        
        StringBuilder sb = new StringBuilder();
        
        for (int i = 0; i < tokens.size(); i++) {
            if (i > 0) {
                String v = parameters[i-1].toString();
                try {
                    sb.append(URLEncoder.encode(v, "UTF-8"));
                } catch (UnsupportedEncodingException e) {
                    // should not happen
                    throw new IllegalArgumentException("Unsupported encoding (should not happen)");
                }
            }
            sb.append(tokens.get(i));
        }
        
        return sb.toString();
    }
    
    static private List<String> splitter(String s, String delimiter) {
        List<String> tokens = new ArrayList<>();
        
        int index = 0;
        int matches = 0;
        while (index < s.length()) {
            int pos = s.indexOf(delimiter, index);
            
            tokens.add(s.substring(index, (pos < 0 ? s.length() : pos)));
            
            if (pos < 0) {
                break;
            }
            
            matches++;
            index = pos + delimiter.length();
        }
        
        // add an empty value at end if needed
        if (matches + 1 != tokens.size()) {
            tokens.add("");
        }
        
        return tokens;
    }
    
}

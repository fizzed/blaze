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

import static com.fizzed.blaze.util.MutableUri.decode;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Helps to build a URI.  Why another one?  This one has a simple fluent style
 * and accepts Objects for parameter values.  It also has an interesting "format"
 * style it'll accept which does placeholder swapping.
 * 
 * @author joelauer
 */
public class MutableUri implements ImmutableUri {
    
    private String scheme;
    private String username;                // userInfo split up
    private String password;
    private String host;
    private Integer port;
    private String path;
    private List<NameValue> parameters;
    private String fragment;

    public MutableUri() {
        // empty
    }
    
    public MutableUri(String uri) {
        this(URI.create(uri));
    }
    
    public MutableUri(URI uri) {
        this.with(uri);
    }
    
    public MutableUri(String uri, Object... parameters) {
        this(format(uri, parameters));
    }
    
    public MutableUri(MutableUri uri) {
        this.scheme = uri.scheme;
        this.username = uri.username;
        this.password = uri.password;
        this.host = uri.host;
        this.port = uri.port;
        this.path = uri.path;
        this.fragment = uri.fragment;
        if (uri.parameters != null) {
            this.parameters = new ArrayList<>(uri.parameters);
        }
    }

    public ImmutableUri toImmutableUri() {
        return new MutableUri(this);
    }
    
    public URI toURI() {
        try {
            // only way to correctly set query string
            return new URI(toString());
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException(e.getMessage());
        }
    }
    
    public MutableUri scheme(String scheme) {
        this.scheme = scheme;
        return this;
    }
    
    public MutableUri username(String username) {
        this.username = username;
        return this;
    }
    
    public MutableUri password(String password) {
        this.password = password;
        return this;
    }
    
    public MutableUri host(String host) {
        this.host = host;
        return this;
    }
    
    public MutableUri port(Integer port) {
        this.port = port;
        return this;
    }
    
    public MutableUri path(String path) {
        this.path = path;
        return this;
    }
    
    public MutableUri query(String name, Object value) {
        Objects.requireNonNull(name, "name cannot be null");
        Objects.requireNonNull(value, "value cannot be null");
        if (this.parameters == null) {
            this.parameters = new ArrayList<>();
        }
        this.parameters.add(new NameValue(name, Objects.toString(value)));
        return this;
    }
    
    public MutableUri fragment(String fragment) {
        this.fragment = fragment;
        return this;
    }
    
    private String encodedUserInfo() {
        StringBuilder s = new StringBuilder();
        
        if (username != null) {
            s.append(encode(username));
        }
        
        if (password != null) {
            s.append(":");
            s.append(encode(password));
        }
        
        return s.toString();
    }
    
    private String encodedQueryString() {
        if (this.parameters == null) {
            return null;
        }
        
        StringBuilder s = new StringBuilder();
        
        this.parameters.forEach((nv) -> {
            if (s.length() != 0) {
                s.append("&");
            }
            s.append(nv.getName());
            s.append("=");
            s.append(encode(nv.getValue()));
        });
        
        return s.toString();
    }
    
    @Override
    public String getScheme() {
        return scheme;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public String getPassword() {
        return password;
    }
    
    @Override
    public String getHost() {
        return host;
    }

    @Override
    public Integer getPort() {
        return port;
    }

    @Override
    public String getPath() {
        return path;
    }

    @Override
    public List<NameValue> getParameters() {
        return parameters;
    }

    @Override
    public String getFragment() {
        return fragment;
    }
    
    private MutableUri with(URI uri) {
        // there are a few cases where URI simply parses a value as a path
        // but we'd ideally like to handle it a bit smarter
        
        /**
        // ssh: user@host
        // now this would break relative urls like "images/path", but we only
        // plan on using mutable uris to access services etc.
        if (uri.getScheme() == null && uri.getHost() == null && uri.getPath() != null && !uri.getPath().startsWith("/")) {
            // simply re-parsing with a // on the front will fix it
            uri = URI.create("//" + uri.toString());
        }
        */
        
        if (uri.getScheme() != null) {
            this.scheme(uri.getScheme());
        }
        
        if (uri.getRawUserInfo() != null) {
            String userInfo = uri.getRawUserInfo();
            
            String[] tokens = userInfo.split(":");
            
            if (tokens.length > 0) {
                this.username = decode(tokens[0]);
            }
            
            if (tokens.length > 1) {
                this.password = decode(tokens[1]);
            }
        }
        
        if (uri.getHost() != null) {
            this.host(uri.getHost());
        }
        
        if (uri.getPort() >= 0) {
            this.port(uri.getPort());
        }
        
        if (uri.getRawPath() != null) {
            this.path(uri.getRawPath());
        }
        
        if (uri.getRawQuery() != null) {
            if (this.parameters != null) {
                this.parameters.clear();
            }

            // split on ampersand...
            String[] pairs = uri.getRawQuery().split("&");
            for (String pair : pairs) {
                String[] nv = pair.split("=");
                if (nv.length != 2) {
                    throw new IllegalArgumentException("Name value pair [" + pair + "] in query [" + uri.getRawQuery() + "] missing = char");
                }
                
                this.query(nv[0], decode(nv[1]));
            }
        }
        
        if (uri.getRawFragment() != null) {
            this.fragment(uri.getRawFragment());
        }
        
        return this;
    }
    
    @Override
    public String toString() {
        // Note this code is essentially a copy of 'java.net.URI.defineString',
        // which is private. We cannot use the 'new URI( scheme, userInfo, ... )' or
        // 'new URI( scheme, authority, ... )' constructors because they double
        // encode the query string using 'java.net.URI.quote'
        StringBuilder s = new StringBuilder();
        
        if (this.scheme != null) {
            s.append(this.scheme);
            s.append(':');
        }
        
        if (this.host != null) {
            s.append("//");
            
            if (this.username != null) {
                s.append(encodedUserInfo());
                s.append('@');
            }
            
            s.append(this.host);
            if (this.port != null) {
                s.append(':');
                s.append(this.port);
            }
        } /**else if (uri.getAuthority() != null) {
            builder.append("//");
            builder.append(uri.getAuthority());/**else if (uri.getAuthority() != null) {
            builder.append("//");
            builder.append(uri.getAuthority());
        }*/
        if (this.path != null) {
            s.append(this.path);
        }

        if (parameters != null && !parameters.isEmpty()) {
            s.append('?');
            s.append(encodedQueryString());
        }
        
        if (this.fragment != null) {
            s.append('#');
            s.append(this.fragment);
        }
       
        return s.toString();
    }
    
    static public MutableUri of(String uri, Object... parameters) {
        return new MutableUri(uri, parameters);
    }
    
    static public URI uri(String uri, Object... parameters) {
        return new MutableUri(uri, parameters).toURI();
    }
    
    static public String encode(String value) {
        try {
            return java.net.URLEncoder.encode(value, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new IllegalArgumentException(e.getMessage());
        }
    }
    
    static public String decode(String value) {
        try {
            return java.net.URLDecoder.decode(value, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new IllegalArgumentException(e.getMessage());
        }
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
                sb.append(encode(v));
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

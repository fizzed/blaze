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

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author joelauer
 */
public class Globber {
    
    // http://docs.oracle.com/javase/7/docs/api/java/nio/file/FileSystem.html#getPathMatcher(java.lang.String)
    static public final char[] JAVA_GLOBBING_CHARS = new char[] { '*', '{', '}', '?', '[', ']' };

    private final Path root;
    private final List<PathMatcher> includes;
    private final List<PathMatcher> excludes;
    private boolean recursive;
    private boolean filesOnly;
    private boolean visibleOnly;

    public Globber(Path root) {
        this.root = (root != null ? root : Paths.get("."));
        this.includes = new ArrayList<>();
        this.excludes = new ArrayList<>();
        this.recursive = true;
        this.filesOnly = false;
        this.visibleOnly = false;
    }

    public Globber include(String glob) {
        this.includes.add(FileSystems.getDefault().getPathMatcher("glob:" + glob));
        return this;
    }
    
    public Globber include(PathMatcher matcher) {
        this.includes.add(matcher);
        return this;
    }
    
    public Globber exclude(String glob) {
        this.excludes.add(FileSystems.getDefault().getPathMatcher("glob:" + glob));
        return this;
    }
    
    public Globber exclude(PathMatcher matcher) {
        this.excludes.add(matcher);
        return this;
    }
    
    public Globber recursive() {
        return recursive(true);
    }

    public Globber recursive(boolean recursive) {
        this.recursive = recursive;
        return this;
    }
    
    public Globber filesOnly() {
        return filesOnly(true);
    }
    
    public Globber filesOnly(boolean filesOnly) {
        this.filesOnly = filesOnly;
        return this;
    }
    
    public Globber visibleOnly() {
        return visibleOnly(true);
    }
    
    public Globber visibleOnly(boolean visibleOnly) {
        this.visibleOnly = visibleOnly;
        return this;
    }
    
    private boolean matched(Path relativized, Path path) throws IOException {
        boolean matched = false;
        
        if (visibleOnly) {
            // if it starts with a period
            if (path.getFileName().toString().startsWith(".")) {
                return false;
            }
        }
        
        for (PathMatcher include : includes) {
            if (include.matches(relativized)) {
                matched = true;
                break;
            }
        }

        if (matched) {
            for (PathMatcher exclude : excludes) {
                if (exclude.matches(relativized)) {
                    matched = false;
                    break;
                }
            }
        }
        
        return matched;
    }
    
    public List<Path> scan() throws IOException {
        ArrayList<Path> paths = new ArrayList<>();
        
        Files.walkFileTree(root, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult preVisitDirectory(Path path, BasicFileAttributes attrs) throws IOException {
                // relativize path to root to perform match
                Path relativized = root.relativize(path);
                
                if (matched(relativized, path)) {
                    if (!filesOnly) {
                        paths.add(path.normalize());
                    }
                }
                
                if (!path.equals(root) && !recursive) {
                    return FileVisitResult.SKIP_SUBTREE;
                } else {
                    return FileVisitResult.CONTINUE;
                }
            }
            
            @Override
            public FileVisitResult visitFile(Path path, BasicFileAttributes attrs) throws IOException {
                // relativize path to root to perform match
                Path relativized = root.relativize(path);
                
                if (matched(relativized, path)) {
                    paths.add(path.normalize());
                }
                
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
                return FileVisitResult.CONTINUE;
            }
        });
        
        return paths;
    }
    
    
    static public boolean containsUnescapedChars(String s, char[] targets) {
        boolean escaped = false;
        
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            
            if (c == '\\' && !escaped) {
                escaped = true;
            } else {
                if (!escaped) {
                    for (int j = 0; j < targets.length; j++) {
                        if (c == targets[j]) {
                            return true;
                        }
                    }
                }
                escaped = false;
            }
        }
        
        return false;
    }
    
    static public Globber glob(String pathWithGlob) {
        return glob(Paths.get(pathWithGlob));
    }
    
    static public Globber glob(Path pathWithGlob) {
        int count = pathWithGlob.getNameCount();
        
        Path root = null;
        
        if (pathWithGlob.isAbsolute()) {
            root = Paths.get("/");
        }
        
        StringBuilder pattern = null;
        
        boolean patternFound = false;
        for (int i = 0; i < count; i++) {
            Path p = pathWithGlob.getName(i);
            String s = p.toString();
            
            if (!patternFound) {
                // does it contain any unescaped special chars?
                patternFound = containsUnescapedChars(s, JAVA_GLOBBING_CHARS);
            }
            
            if (patternFound) {
                // this component onward needs to be globbed!
                if (pattern != null) {
                    pattern.append('/');
                } else {
                    pattern = new StringBuilder();
                }
                pattern.append(s);
            } else {
                // root or root + p
                root = (root == null ? p : root.resolve(p));
            }
        }
        
        // build globber
        Globber globber = new Globber(root);
        
        if (pattern != null) {
            globber.include(pattern.toString());
        }
        
        return globber;
    }
    
    static public void main(String[] args) throws Exception {
        
        //Globber globber = Globber.glob(Paths.get("/usr/java/*"));
        Globber globber = Globber.glob(Paths.get("src/**/*.java"));
        
        for (Path path : globber.scan()) {
            System.out.println(path);
        }
        
        /**
        Globber globber
            = new Globber()
                //.filesOnly()
                .visibleOnly()
                .include("*");
        
        for (Path path : globber.scan(Paths.get(".."))) {
            System.out.println(path);
        }
        */
    }
    
}

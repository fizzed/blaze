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
    private boolean dirsOnly;
    private boolean visibleOnly;
    
    public Globber() {
        this((Path)null);
    }
    
    public Globber(String root) {
        this((root != null ? Paths.get(root) : null));
    }

    public Globber(Path root) {
        this.root = (root != null ? root : Paths.get("."));
        this.includes = new ArrayList<>();
        this.excludes = new ArrayList<>();
        this.recursive = true;
        this.dirsOnly = false;
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
    
    public Globber dirsOnly() {
        return dirsOnly(true);
    }
    
    public Globber dirsOnly(boolean dirsOnly) {
        this.dirsOnly = dirsOnly;
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
                
                if (!path.equals(root)) {
                    if (matched(relativized, path)) {
                        if (!filesOnly) {
                            paths.add(path.normalize());
                        }
                    }
                }
                
                if (path.equals(root)) {
                    return FileVisitResult.CONTINUE;
                } else {
                    if (!recursive) {
                        return FileVisitResult.SKIP_SUBTREE;
                    } else {
                        return FileVisitResult.CONTINUE;
                    }
                }
            }
            
            @Override
            public FileVisitResult visitFile(Path path, BasicFileAttributes attrs) throws IOException {
                // relativize path to root to perform match
                Path relativized = root.relativize(path);
                
                if (matched(relativized, path)) {
                    if (!dirsOnly) {
                        paths.add(path.normalize());
                    }
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
    
    static public Globber globber(String root, String glob) {
        return new Globber(root)
           .include(glob);
    }
    
    static public Globber globber(Path root, String glob) {
        return new Globber(root)
           .include(glob);
    }
    
    static public Globber globber(String rootWithGlob) {
        List<String> paths = BasicPaths.split(rootWithGlob);
        
        int globIndex = detectGlobIndex(paths);
        
        String root = null;
        
        // does it have a root?
        if (globIndex > 0) {
            root = BasicPaths.toString(paths, 0, globIndex, "/");
        }
        
        Globber globber = new Globber(root);
        
        // does it have a glob?
        if (globIndex > -1) {
            String glob = BasicPaths.toString(paths, globIndex, paths.size(), "/");
            globber.include(glob);
        }
        
        return globber;
    }
    
    // "src" -> "." and "src"
    static public int detectGlobIndex(List<String> paths) {
        int i = 0;
        
        for (; i < paths.size(); i++) {
            String path = paths.get(i);
            
            if (path.equals("..") || path.equals(".") || paths.equals("")) {
                // keep searching
            } else {
                return i;
            }
        }
        
        return i;
    }
    
    static boolean containsUnescapedChars(String s, char[] targets) {
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
}

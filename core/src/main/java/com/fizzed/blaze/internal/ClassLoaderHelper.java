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
package com.fizzed.blaze.internal;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author joelauer
 */
public class ClassLoaderHelper {
    static private final Logger log = LoggerFactory.getLogger(ClassLoaderHelper.class);
    
    static public ClassLoader currentThreadContextClassLoader() {
        return Thread.currentThread().getContextClassLoader();
    }
    
    static public URLClassLoader requireURLClassLoader(ClassLoader classLoader) {
        if (!(classLoader instanceof URLClassLoader)) {
            throw new IllegalArgumentException("Only classloaders of type URLClassLoader supported");
        }
        
        return (URLClassLoader)classLoader;
    }
    
    static public boolean addClassPath(ClassLoader classLoader, File file) {
        return addClassPath(classLoader, file.toURI());
    }
    
    static public boolean addClassPath(ClassLoader classLoader, Path path) {
        return addClassPath(classLoader, path.toUri());
    }

    static public boolean addClassPath(ClassLoader classLoader, URI uri) {
        URLClassLoader urlClassLoader = requireURLClassLoader(classLoader);
        boolean isJar = uri.getScheme().startsWith("jar:");
        File file = new File(uri);
        
        try {
            // prevent duplicates
            for (URL url : urlClassLoader.getURLs()) {
                URI loadedUri = url.toURI();
                
                // exact match is a duplicate
                if (loadedUri.equals(uri)) {
                    log.trace("URI " + uri + " already on classpath");
                    return false;
                }
                
                if (isJar) {
                    File loadedFile = new File(loadedUri);
                    if (file.getName().equals(loadedFile.getName())) {
                        log.trace("File " + file + " already on classpath");
                        return false;
                    }
                }
            }
            
            // add url via reflection (to workaround private access)
            invokeDeclared(URLClassLoader.class, classLoader, "addURL", new Class[] { URL.class }, new Object[] { uri.toURL() });
            
            return true;
        } catch (IllegalAccessException | NoSuchMethodException | InvocationTargetException | URISyntaxException | MalformedURLException e) {
            throw new IllegalArgumentException("Unable to add " + uri + " to classpath", e);
        }
    }

    static public Object invokeDeclared(Class c, Object obj, String method, Class[] paramClasses, Object[] params) throws IllegalAccessException, NoSuchMethodException, InvocationTargetException {
        Method m = c.getDeclaredMethod(method, paramClasses);
        m.setAccessible(true);
        return m.invoke(obj, params);
    }

    static public File findContainingJar(ClassLoader classLoader, String resourceName) {
        File jarFile;
        URL url = Thread.currentThread().getContextClassLoader().getResource(resourceName);
        
        if ("jar".equals(url.getProtocol())) { //NOI18N
            
            String path = url.getPath();
            int index = path.indexOf("!/"); //NOI18N

            if (index >= 0) {
                try {
                    String jarPath = path.substring(0, index);
                    if (jarPath.contains("file://") && !jarPath.contains("file:////")) {  //NOI18N
                        /* Replace because JDK application classloader wrongly recognizes UNC paths. */
                        jarPath = jarPath.replaceFirst("file://", "file:////");  //NOI18N
                    }
                    url = new URL(jarPath);

                } catch (MalformedURLException mue) {
                    throw new RuntimeException(mue);
                }
            }
        }
        try {
            jarFile = new File(url.toURI());
        } catch (URISyntaxException ex) {
            throw new RuntimeException(ex);
        }
        assert jarFile.exists();
        return jarFile;
    }
    
    static public List<URL> buildClassPath(ClassLoader classLoader) {
        if (!(classLoader instanceof URLClassLoader)) {
            throw new IllegalArgumentException("Only classloaders of type URLClassLoader supported");
        }
        
        URLClassLoader urlClassLoader = (URLClassLoader)classLoader;
        
        return java.util.Arrays.asList(urlClassLoader.getURLs());
    }
    
    static public String buildClassPathAsString(ClassLoader classLoader) {
        List<File> files = buildClassPathAsFiles(classLoader);
        
        StringBuilder cp = new StringBuilder();
        
        for (File file : files) {
            if (cp.length() > 0) {
                cp.append(File.pathSeparator);
            }
            
            cp.append(file.getAbsolutePath());
        }
        
        return cp.toString();
    }
    
    static public List<File> buildClassPathAsFiles(ClassLoader classLoader) {
        List<URL> urls = buildClassPath(classLoader);
        
        List<File> files = new ArrayList<>();
        
        for (URL u : urls) {
            try {
                files.add(new File(u.toURI()));
            } catch (Exception e) {
                // do nothing...
            }
        }
        
        return files;
    }
}

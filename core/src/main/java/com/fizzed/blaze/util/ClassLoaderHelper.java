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

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Paths;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author joelauer
 */
public class ClassLoaderHelper {
    static private final Logger log = LoggerFactory.getLogger(ClassLoaderHelper.class);

    /**
     * Adds additional file or path to classpath during runtime.
     *
     * @param file
     * @param classLoader
     * @return 
     * @see #addUrlToClassPath(java.net.URL, ClassLoader)
     */
    public static int addFileToClassPath(File file, ClassLoader classLoader) {
        return addUrlToClassPath(file.toURI(), classLoader);
    }

    /**
     * Adds the content pointed by the URL to the classpath during runtime. Uses
     * reflection since <code>addURL</code> method of
     * <code>URLClassLoader</code> is protected.
     * @param url
     * @param classLoader
     * @return 
     */
    public static int addUrlToClassPath(URI uri, ClassLoader classLoader) {
        try {
            // does the jar already exist on claspath?
            URLClassLoader urlClassLoader = (URLClassLoader)classLoader;
            String jarName = Paths.get(uri).getFileName().toString();
            for (URL u : urlClassLoader.getURLs()) {
                String loadedJarName = Paths.get(u.toURI()).getFileName().toString();
                if (jarName.equals(loadedJarName)) {
                    log.trace("Jar " + jarName + " already exists on classpath with " + u);
                    return 0;
                }
            }
            
            // use reflection to add url
            invokeDeclared(
                    URLClassLoader.class, classLoader, "addURL", new Class[] { URL.class }, new Object[] { uri.toURL() });
            
            return 1;
        } catch (IllegalAccessException | NoSuchMethodException | InvocationTargetException | URISyntaxException | MalformedURLException e) {
            throw new IllegalArgumentException("Unable to add url to classloader: " + uri, e);
        }
    }

    /**
     * Invokes any method of a class, even private ones.
     *
     * @param c class to examine
     * @param obj object to inspect
     * @param method method to invoke
     * @param paramClasses	parameter types
     * @param params parameters
     */
    public static Object invokeDeclared(Class c, Object obj, String method, Class[] paramClasses, Object[] params) throws IllegalAccessException, NoSuchMethodException, InvocationTargetException {
        Method m = c.getDeclaredMethod(method, paramClasses);
        m.setAccessible(true);
        return m.invoke(obj, params);
    }

}

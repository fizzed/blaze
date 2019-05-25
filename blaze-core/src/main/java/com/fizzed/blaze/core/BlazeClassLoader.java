/*
 * Copyright 2019 Fizzed, Inc.
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

import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Enumeration;

public class BlazeClassLoader extends URLClassLoader {
    
    public BlazeClassLoader(URL[] urls, ClassLoader parent) {
        super(urls, parent);
    }

    @Override
    public void addURL(URL url) {
//        System.out.println("CUSTOM: addURL=" + url);
        
        super.addURL(url);
    }

    @Override
    protected Class<?> findClass(String name) throws ClassNotFoundException {
//        System.out.println("CUSTOM: findClass=" + name);
        
        return super.findClass(name);
    }

    @Override
    protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
//        System.out.println("CUSTOM: loadClass=" + name + ", resolve=" + resolve);
        
        return super.loadClass(name, resolve);
    }

    @Override
    public Enumeration<URL> getResources(String name) throws IOException {
        return super.getResources(name); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public URL getResource(String name) {
        return super.getResource(name); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Class<?> loadClass(String name) throws ClassNotFoundException {
//        System.out.println("CUSTOM: loadClass=" + name);
        
        return super.loadClass(name); //To change body of generated methods, choose Tools | Templates.
    }
 
    
    
}
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

import com.fizzed.blaze.BlazeException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;

/**
 *
 * @author joelauer
 */
public class FileHelper {
    
    static public File resourceAsFile(String resourceName) throws URISyntaxException, MalformedURLException, IOException {
        URL url = ConfigHelper.class.getResource(resourceName);
        
        if (url == null) {
            throw new FileNotFoundException("Resource " + resourceName + " not found");
        }
        
        File file = new File(url.toURI());
        
        // can it look better if we relativize it to the working dir?
        File workingDir = new File(System.getProperty("user.dir"));
        
        return workingDir.toPath().relativize(file.toPath()).toFile();
    }
    
    static public File relativizeToJavaWorkingDir(File file) {
        try {
            // can it look better if we relativize it to the working dir?
            File workingDir = new File(System.getProperty("user.dir"));
            Path workingPath = workingDir.getCanonicalFile().toPath();
            
            Path filePath = file.getCanonicalFile().toPath();
        
            //System.out.println("workingPath: " + workingPath);
            //System.out.println("filePath: " + filePath);

            return workingPath.relativize(filePath).toFile();
        } catch (Exception e) {
            throw new BlazeException("Unable to canonicalize file", e);
        }
    }
    
    static public String fileExtension(File file) {
        String name = file.getName();
        int lastIndexOf = name.lastIndexOf('.');
        if (lastIndexOf < 0) {
            throw new IllegalArgumentException("File " + file + " missing file extension");
        }
        return name.substring(lastIndexOf);
    }
    
}

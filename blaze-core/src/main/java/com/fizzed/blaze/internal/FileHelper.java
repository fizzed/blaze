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

import com.fizzed.blaze.core.FileNotFoundException;
import com.fizzed.blaze.core.BlazeException;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

/**
 *
 * @author joelauer
 */
public class FileHelper {
    
    static public Path resourceAsPath(String resourceName) throws URISyntaxException, MalformedURLException, IOException {
        URL url = ConfigHelper.class.getResource(resourceName);
        
        if (url == null) {
            throw new FileNotFoundException("Resource " + resourceName + " not found");
        }
        
        File file = new File(url.toURI());
        
        // can it look better if we relativize it to the working dir?
        File workingDir = new File(System.getProperty("user.dir"));
        
        return workingDir.toPath().relativize(file.toPath());
    }
    
    static public File resourceAsFile(String resourceName) throws URISyntaxException, MalformedURLException, IOException {        
        return resourceAsPath(resourceName).toFile();
    }
    
    static public Path relativizeToJavaWorkingDir(Path path) {
        try {
            // can it look better if we relativize it to the working dir?
            Path workingDir = Paths.get(System.getProperty("user.dir"));
            Path workingPath = workingDir.toAbsolutePath().normalize();
            
            Path filePath = path.toAbsolutePath().normalize();
        
            //System.out.println("workingPath: " + workingPath);
            //System.out.println("filePath: " + filePath);

            return workingPath.relativize(filePath);
        } catch (Exception e) {
            throw new BlazeException("Unable to canonicalize file", e);
        }
    }
    
    static public String fileExtension(File file) {
        return fileExtension(file.toPath());
    }
    
    static public String fileExtension(Path path) {
        String name = path.getFileName().toString();
        int lastIndexOf = name.lastIndexOf('.');
        if (lastIndexOf < 0) {
            throw new IllegalArgumentException("Path " + path + " missing file extension");
        }
        return name.substring(lastIndexOf);
    }
    
    static public Path concatToFileName(Path path, String moreFileName) {
        return path.resolveSibling(path.getFileName().toString() + moreFileName);
    }
    
    static public byte[] md5(Path path) throws IOException, NoSuchAlgorithmException {
        byte[] buf = new byte[1024];
        MessageDigest complete = MessageDigest.getInstance("MD5");
        int read;
        try (InputStream is = Files.newInputStream(path)) {
            do {
                read = is.read(buf);
                if (read > 0) {
                    complete.update(buf, 0, read);
                }
            } while (read != -1);
        }
        return complete.digest();
    }
    
    static public String md5hash(Path path) throws IOException, NoSuchAlgorithmException {
        byte[] md5 = md5(path);
        return Base64.getUrlEncoder().encodeToString(md5).trim();
    }
    
    static public void writeHashFileFor(Path path, String hash) throws IOException {
        Path hashPath = concatToFileName(path, ".hash");
        
        Files.write(hashPath, hash.getBytes(StandardCharsets.UTF_8), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
    }
    
    static public boolean verifyHashFileFor(Path path, String hash) throws IOException {
        Path hashPath = concatToFileName(path, ".hash");
        
        if (Files.notExists(hashPath)) {
            return false;
        }
        
        String currentHash = new String(Files.readAllBytes(hashPath), StandardCharsets.UTF_8).trim();
        
        //System.out.println("currentHash " + currentHash);
        //System.out.println("newHash " + hash);
        
        return hash.equals(currentHash);
    }
    
}

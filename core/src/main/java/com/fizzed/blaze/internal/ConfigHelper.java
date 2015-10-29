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

import com.fizzed.blaze.Config;
import com.fizzed.blaze.Context;
import com.fizzed.blaze.core.ContextImpl;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Base64;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author joelauer
 */
public class ConfigHelper {
    static private final Logger log = LoggerFactory.getLogger(ConfigHelper.class);
    
    static public File file(File directory, File file) {
        String fileExt = FileHelper.fileExtension(file);

        String confFileName = file.getName();
        confFileName = confFileName.substring(0, confFileName.length() - fileExt.length());
        confFileName += ".conf";

        return new File(directory, confFileName);
    }
    
    static public Config create(File file) {
        //
        // configuration
        //
        com.typesafe.config.Config typesafeConfig = null;

        // build a typesafe config that we'll wrap w/ our interface (just in case we swap out down the road)
        if (file != null && file.exists()) {
            log.debug("Configuring with {}", file);
            typesafeConfig = com.typesafe.config.ConfigFactory.parseFile(file);
        } else {
            typesafeConfig = com.typesafe.config.ConfigFactory.empty();
        }

        // overlay system properties on top of it
        typesafeConfig = com.typesafe.config.ConfigFactory.load(typesafeConfig);

        return new ConfigImpl(typesafeConfig);
    }
    
    static public boolean isSuperDebugEnabled() {
        return System.getProperty("blaze.superdebug", "false").equalsIgnoreCase("true");
    }

    static public List<String> commandExtensions(Config config) {
        if (OperatingSystem.windows()) {
            return config.find(Config.KEY_COMMAND_EXTS, List.class).or(Config.DEFAULT_COMMAND_EXTS_WINDOWS);
            //return config.getStringList(Config.KEY_COMMAND_EXTS, Config.DEFAULT_COMMAND_EXTS_WINDOWS);
        } else {
            return config.find(Config.KEY_COMMAND_EXTS, List.class).or(Config.DEFAULT_COMMAND_EXTS_UNIX);
            //return config.getStringList(Config.KEY_COMMAND_EXTS, Config.DEFAULT_COMMAND_EXTS_UNIX);
        }
    }
    
    static public List<Path> systemEnvironmentPaths() {
        List<String> pathStrings = systemEnvironmentPathsAsStrings();
        return pathStrings.stream().map(s -> Paths.get(s)).collect(Collectors.toList());
    }

    static public List<String> systemEnvironmentPathsAsStrings() {
        String path = System.getenv("PATH");
        if (path != null) {
            return Arrays.asList(path.split(File.pathSeparator));
        } else {
            return Collections.emptyList();
        }
    }
 
    static public class OperatingSystem {
        private static final String OS = System.getProperty("os.name").toLowerCase();

        static public boolean windows() {
            return (OS.contains("win"));
        }

        static public boolean mac() {
            return (OS.contains("mac"));
        }

        static public boolean unix() {
            return (OS.contains("nix") || OS.contains("nux") || OS.contains("aix") );
        }

        static public boolean solaris() {
            return (OS.contains("sunos"));
        }
    }
    
    static public Path contextSemiPersistentTempPath(Context context) throws IOException {
        String tmpDirStr = System.getProperty("java.io.tmpdir");
        
        if (tmpDirStr == null) {
            log.warn("Java system property java.io.tmpdir not set");
            return null;
        }
        
        Path systemTempPath = Paths.get(tmpDirStr);
        
        if (!Files.exists(systemTempPath)) {
            log.warn("Java java.io.tmpdir of {} does not exist", systemTempPath);
            return null;
        }
        
        // md5 of the canonical path of this application's base directory
        // should be a consistent hash very usable for generating classes in
        String md5 = md5(context.baseDir().toFile().getCanonicalPath());
        
        // /tmp/blaze/<md5hash>
        Path contextTempPath = systemTempPath.resolve("blaze").resolve(md5);
        
        Files.createDirectories(contextTempPath);
        
        return contextTempPath;
    }
    
    static public Path semiPersistentClassesPath(Context context) throws IOException {
        Path path = contextSemiPersistentTempPath(context);
        
        if (path == null) {
            return null;
        }
        
        Path classesPath = path.resolve("classes");
        
        Files.createDirectories(classesPath);
        
        return classesPath;
    }
    
    static public String md5(String value) {
        try {
            byte[] bytes = value.getBytes("UTF-8");
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] digest = md.digest(bytes);
            return Base64.getUrlEncoder().encodeToString(digest);
        } catch (UnsupportedEncodingException | NoSuchAlgorithmException e) {
            throw new IllegalStateException("MD5 hash failed", e);
        }
    }
    
}

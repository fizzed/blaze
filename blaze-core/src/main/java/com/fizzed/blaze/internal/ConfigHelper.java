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
import com.fizzed.blaze.Version;
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

public class ConfigHelper {
    static private final Logger log = LoggerFactory.getLogger(ConfigHelper.class);

    static public ConfigPaths paths(Path scriptDir, Path scriptFile) {
        // e.g. blaze.java ---> extract out the .java part
        final String ext = FileHelper.fileExtension(scriptFile);

        final String name = scriptFile.getFileName().toString();
        // e.g. blaze.java --> without extension such as "blaze"
        final String nameWithoutExt = name.substring(0, name.length() - ext.length());
        final String primaryConfName = nameWithoutExt + ".conf";
        final String localConfName = nameWithoutExt + ".local.conf";

        final Path primaryFile;
        final Path localFile;
        if (scriptDir == null) {
            primaryFile = Paths.get(primaryConfName);
            localFile = Paths.get(localConfName);
        } else {
            primaryFile = scriptDir.resolve(primaryConfName);
            localFile = scriptDir.resolve(localConfName);
        }

        return new ConfigPaths(primaryFile, localFile);
    }
    
    static public Config create(ConfigPaths configPaths) {
        //
        // configuration
        //
        com.typesafe.config.Config typesafeConfig = null;

        // there are 2 config files -- one is the primary and then there is a local conf that can override it
        // the local conf is intended to allow you to NOT commit a file to your repository
        final Path primaryFile = configPaths != null ? configPaths.getPrimaryFile() : null;
        final Path localFile = configPaths != null ? configPaths.getLocalFile() : null;

        // build a typesafe config that we'll wrap w/ our interface (just in case we swap out down the road)
        if (primaryFile != null && Files.exists(primaryFile)) {
            log.debug("Configuring with file {}", primaryFile);
            typesafeConfig = com.typesafe.config.ConfigFactory.parseFile(primaryFile.toFile());
        } else {
            typesafeConfig = com.typesafe.config.ConfigFactory.empty();
        }

        // use the local file to override anything in the default?
        if (localFile != null && Files.exists(localFile)) {
            log.debug("Configuring with local/overlay file {}", localFile);
            com.typesafe.config.Config typesafeLocalConfig = com.typesafe.config.ConfigFactory.parseFile(localFile.toFile());
            // we'll actually use the local version first, making the primary the new fallback
            typesafeConfig = typesafeLocalConfig.withFallback(typesafeConfig);
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
            return config.value(Config.KEY_COMMAND_EXTS, List.class).getOr(Config.DEFAULT_COMMAND_EXTS_WINDOWS);
        } else {
            return config.value(Config.KEY_COMMAND_EXTS, List.class).getOr(Config.DEFAULT_COMMAND_EXTS_UNIX);
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
    
    static public Path userBlazeDir(Context context) throws IOException {
        Path userBlazeDir = context.withUserDir(".blaze");
        
        if (Files.notExists(userBlazeDir)) {
            Files.createDirectory(userBlazeDir);
        }
        
        return userBlazeDir;
    }
    
    static public Path userBlazeCacheDir(Context context) throws IOException {
        Path userCacheDir = userBlazeDir(context).resolve("cache");
        
        if (Files.notExists(userCacheDir)) {
            Files.createDirectory(userCacheDir);
        }
        
        return userCacheDir;
    }
    
    static public Path userBlazeEngineDir(Context context, String engineName) throws IOException {
        Path userBlazeDir = userBlazeDir(context);
        
        // ~/.blaze/engine/{engineName}
        Path userBlazeEngineDir
            = userBlazeDir
                .resolve("engine")
                .resolve(engineName);
        
        Files.createDirectories(userBlazeEngineDir);
        
        return userBlazeEngineDir;
    }
    
    static public Path userBlazeEngineScriptClassesDir(Context context, String engineName) throws IOException {
        // md5 of the canonical path of this application's base directory
        // should be a consistent hash very usable for generating classes in
        String key = new StringBuilder()
            // base directory where script is from
            .append(context.baseDir().toFile().getCanonicalPath())
            // version of blaze
            .append(Version.getVersion())
            // v1.3.0+ -- the version of the JVM currently running (in case the user switches)
            .append(System.getProperty("java.version"))
            .toString();
        
        String md5hash = md5(key);
        
        // ~/.blaze/engine/{engineName}/{md5hash}
        Path userBlazeEngineScriptClassesDir
            = userBlazeEngineDir(context, engineName)
                .resolve(md5hash)
                .resolve("classes");
        
        Files.createDirectories(userBlazeEngineScriptClassesDir);
        
        return userBlazeEngineScriptClassesDir;
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

    static public int getJavaSourceVersion(Context context) {
        String configValue = null;
        try {
            configValue = context.config().value("java.source.version").orNull();
            if (configValue != null) {
                return Integer.parseInt(configValue);
            }
        } catch (Exception e) {
            throw new IllegalArgumentException("java.source.version value [" + configValue + "] was not an integer");
        }

        // otherwise, we need detect the current java major version
        String javaVersion = System.getProperty("java.version");
        // e.g. 11.0.1, 12.0.1, 1.8.0, etc

        if (javaVersion.startsWith("1.8")) {
            return 8;
        }

        int periodPos = javaVersion.indexOf('.');
        if (periodPos < 0) {
            // treat the whole string as the major version
            periodPos = javaVersion.length();
            //throw new IllegalStateException("java.version [" + javaVersion + "] not of format X.X.X");
        }

        try {
            return Integer.parseInt(javaVersion.substring(0, periodPos));
        } catch (Exception e) {
            throw new IllegalArgumentException("java.version [" + javaVersion + "] did not start with an integer");
        }
    }
    
}

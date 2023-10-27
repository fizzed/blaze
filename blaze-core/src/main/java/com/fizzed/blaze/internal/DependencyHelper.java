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

import com.fizzed.blaze.core.Dependency;
import com.fizzed.blaze.core.BlazeException;
import com.fizzed.blaze.Config;
import com.fizzed.blaze.Version;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static java.util.Arrays.asList;

/**
 *
 * @author joelauer
 */
public class DependencyHelper {
    static private final Logger log = LoggerFactory.getLogger(DependencyHelper.class);
    
    static private final Map<String, List<Dependency>> WELL_KNOWN_ENGINE_DEPENDENCIES = new HashMap<>();
    static {
        WELL_KNOWN_ENGINE_DEPENDENCIES.put(".js",
            asList(new Dependency("com.fizzed", "blaze-nashorn", Version.getVersion())));
        WELL_KNOWN_ENGINE_DEPENDENCIES.put(".groovy",
            asList(new Dependency("com.fizzed", "blaze-groovy", Version.getVersion())));
        WELL_KNOWN_ENGINE_DEPENDENCIES.put(".kt",
            asList(new Dependency("com.fizzed", "blaze-kotlin", Version.getVersion())));
    }
    
    static public List<Dependency> wellKnownEngineDependencies(String fileExtension) {
        return WELL_KNOWN_ENGINE_DEPENDENCIES.get(fileExtension);
    }
    
    /**
     * Get the dependency list from the application configuration file.
     * @param config
     * @return 
     */
    static public List<Dependency> applicationDependencies(Config config) {
        List<String> ds = config.valueList(Config.KEY_DEPENDENCIES).getOrNull();
        
        if (ds == null || ds.isEmpty()) {
            return null;
        }
         
        List<Dependency> dependencies = new ArrayList<>(ds.size());
        
        ds.stream().forEach((d) -> {
            dependencies.add(Dependency.parse(d));
        });
        
        return dependencies;
    }
    
    /**
     * Dependencies outputted by the maven dependency plugin are of this form
     *      groupId:artifactId:type:version
     * and we actually want groupId:artifactId:type:version
     * @param dependency
     * @return 
     */
    static public String cleanMavenDependencyLine(String dependency) {
        return dependency.replace(":jar:", ":");
    }
    
    static public List<Dependency> alreadyBundled() {
        String resourceName = "/com/fizzed/blaze/bundled.txt";
        
        URL url = DependencyHelper.class.getResource(resourceName);
        
        if (url == null) {
            throw new BlazeException("Unable to find resource " + resourceName + ". Maybe not packaged as jar correctly?");
        }
        
        List<Dependency> dependencies = new ArrayList<>();
        
        try (InputStream is = url.openStream()) {
            List<String> lines = IOUtils.readLines(is, "UTF-8");
            for (String line : lines) {
                line = line.trim();
                if (!line.equals("")) {
                    if (line.contains("following files have been resolved")) {
                        // skip
                    } else {
                        String d = cleanMavenDependencyLine(line);
                        dependencies.add(Dependency.parse(d));
                    }
                }
            }
        } catch (IOException e) {
            throw new BlazeException("Unable to detect bundled dependencies", e);
        }
        
        return dependencies;
    }
    
    static public void collect(List<Dependency> collect, List<Dependency> dependencies) {
        // collect cannot be null
        if (collect == null) {
            throw new IllegalArgumentException("Collect list cannot be null");
        }
        
        if (dependencies == null || dependencies.isEmpty()) {
            return;
        }
        
        collect.addAll(dependencies);
    }
    
    static public Set<String> toGroupArtifactSet(List<Dependency> dependencies) { 
        return dependencies.stream()
                .map((d) -> d.getGroupId() + ":" + d.getArtifactId())
                .collect(Collectors.toSet());
    }
    
}

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
package com.fizzed.blaze.core;

import com.fizzed.blaze.Version;
import java.util.Objects;

/**
 *
 * @author joelauer
 */
public class Dependency {
    
    final private String groupId;
    final private String artifactId;
    final private String version;
    
    public Dependency(String groupId, String artifactId, String version) {
        this.groupId = groupId;
        this.artifactId = artifactId;
        this.version = version;
    }

    public String getGroupId() {
        return groupId;
    }

    public String getArtifactId() {
        return artifactId;
    }

    public String getVersion() {
        return version;
    }
    
    static public Dependency parse(String dependency) {
        String[] tokens = dependency.split(":");
        
        if (tokens.length < 2) {
            throw new IllegalArgumentException("Invalid dependency (not in format groupId:artifactId:version)");
        }
        
        String groupId = tokens[0].trim();
        String artifactId = tokens[1].trim();
        String version = (tokens.length != 3 ? "" : tokens[2].trim());
        
        // special case for blaze dependencies
        if (groupId.equals("com.fizzed") && artifactId.startsWith("blaze-") && version.equals("")) {
            version = Version.getVersion();
        }
        
        if (version.equals("")) {
            throw new IllegalArgumentException("Invalid dependency (not in format groupId:artifactId:version)");
        }

        // https://ant.apache.org/ivy/history/2.5.0-rc1/settings/version-matchers.html
        // https://stackoverflow.com/questions/30571/how-do-i-tell-maven-to-use-the-latest-version-of-a-dependency
        // convert "latest" into the .ivy2 equivalent
        if (version.equalsIgnoreCase("latest")) {
            version = "latest.integration";
        }
        if (version.equalsIgnoreCase("release")) {
            version = "latest.release";
        }
        
        return new Dependency(groupId, artifactId, version);
    }

    @Override
    public String toString() {
        return groupId + ":" + artifactId + ":" + version;
    }
    
    @Override
    public int hashCode() {
        int hash = 7;
        hash = 79 * hash + Objects.hashCode(this.groupId);
        hash = 79 * hash + Objects.hashCode(this.artifactId);
        hash = 79 * hash + Objects.hashCode(this.version);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Dependency other = (Dependency) obj;
        if (!Objects.equals(this.groupId, other.groupId)) {
            return false;
        }
        if (!Objects.equals(this.artifactId, other.artifactId)) {
            return false;
        }
        if (!Objects.equals(this.version, other.version)) {
            return false;
        }
        return true;
    }
    
}

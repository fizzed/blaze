/*
 * Copyright 2020 Fizzed, Inc.
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
package com.fizzed.blaze.docker;

import com.fizzed.blaze.Contexts;
import com.fizzed.blaze.core.BlazeException;
import com.fizzed.blaze.docker.impl.HeaderToken;
import com.fizzed.blaze.docker.impl.ProcessRow;
import com.fizzed.blaze.local.LocalExec;
import com.fizzed.blaze.system.Which;
import com.fizzed.blaze.util.MutableUri;
import java.net.URI;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Dockers {
    static private final Logger log = LoggerFactory.getLogger(Dockers.class);
 
    static public DockerConnect dockerConnect(String uri) {
        return dockerConnect(MutableUri.of(uri));
    }
    
    static public DockerConnect dockerConnect(URI uri) {
        return dockerConnect(new MutableUri(uri));
    }
    
    static public DockerConnect dockerConnect(MutableUri uri) {
        return new DockerConnect(Contexts.currentContext(), uri);
    }
    
    static public boolean isContainerRunning(
            String name) {
        
        final String output = new LocalExec(Contexts.currentContext())
            .command("docker")
            .args("ps")
            .runCaptureOutput()
            .asString()
            .trim();
        
        String[] lines = output.split("\n");
        
        // header line
        if (lines == null || lines.length < 1) {
            throw new BlazeException("No docker ps output detected");
        }
        
        String headerLine = lines[0];
        
        // position of column header is where the values start too...
        final List<HeaderToken> headers = HeaderToken.parseHeaderLine(headerLine);
        final List<ProcessRow> processes = new ArrayList<>();
        
        for (int i = 1; i < lines.length; i++) {
            String line = lines[i];
            ProcessRow row = new ProcessRow();
            for (int j = 0; j < headers.size(); j++) {
                HeaderToken header = headers.get(j);
                // last header?
                int end = ((j+1) < headers.size()) ? headers.get(j+1).getStart() : line.length();
                String value = line.substring(header.getStart(), end);
                switch(header.getName().toLowerCase()) {
                    case "container id":
                        row.setContainerId(value);
                        break;
                    case "status":
                        row.setStatus(value);
                        break;
                    case "names":
                        row.setNames(value);
                        break;
                }
            }
            processes.add(row);
        }
        
        return processes.stream()
            .filter(v -> v.getNames().toLowerCase().contains(name.toLowerCase()))
            .anyMatch(v -> v.getStatus().toLowerCase().contains("up"));
    }
    
}
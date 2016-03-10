/*
 * Copyright 2016 Fizzed, Inc.
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
package com.fizzed.crux.vagrant;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zeroturnaround.exec.InvalidExitValueException;
import org.zeroturnaround.exec.ProcessExecutor;
import org.zeroturnaround.exec.ProcessResult;

public class Vagrant {
    static private final Logger log = LoggerFactory.getLogger(Vagrant.class);
    
    private final File workingDirectory;
    private final boolean running;
    private final Map<String,Status> status;
    private final File sshConfigFile;

    private Vagrant(File workingDirectory,
                    boolean running,
                    Map<String,Status> status,
                    File sshConfigFile) {
        this.workingDirectory = workingDirectory;
        this.running = running;
        this.status = status;
        this.sshConfigFile = sshConfigFile;
    }

    public File workingDirectory() {
        return this.workingDirectory;
    }

    public boolean running() {
        return this.running;
    }
    
    public Map<String, Status> status() {
        return this.status;
    }

    public File sshConfigFile() {
        return sshConfigFile;
    }
    
    static public class Loader {
        
        private File workingDirectory;

        public Loader() {
        }

        public Loader workingDirectory(File workingDirectory) {
            this.workingDirectory = workingDirectory;
            return this;
        }
        
        public Vagrant load() throws VagrantException {
            Map<String,Status> status = fetchStatus();
            
            // all running?
            boolean running = true;
            for (Map.Entry<String,Status> entry : status.entrySet()) {
                String state = entry.getValue().getValues().get("state");
                if (!Objects.equals(state, "running")) {
                    running = false;
                }
            }
            
            File sshConfigFile = saveSshConfig(null);
            
            return new Vagrant(workingDirectory, running, status, sshConfigFile);
        }
        
        public Vagrant safeLoad() {
            try {
                return load();
            } catch (VagrantException e) {
                return new Vagrant(workingDirectory, false, new HashMap<>(), null);
            }
        }
    }
    
    static Map<String,Status> fetchStatus() throws VagrantException {
        try {
            ProcessResult result
                = new ProcessExecutor()
                    .command("vagrant", "status", "--machine-readable")
                    .readOutput(true)
                    .execute();

            List<String> lines = lines(result);

            return parseStatus(lines);
        } catch (IOException | InterruptedException | TimeoutException | InvalidExitValueException e) {
            throw new VagrantException(e.getMessage(), e);
        }
    }
    
    static File saveSshConfig(File sshConfigFile) throws VagrantException {
        try {
            ProcessResult result
                = new ProcessExecutor()
                    .command("vagrant", "ssh-config")
                    .readOutput(true)
                    .execute();
            
            if (sshConfigFile == null) {
                sshConfigFile = File.createTempFile("vagrant.", ".sshconfig");
            }
            
            // save .ssh-config
            Files.write(sshConfigFile.toPath(),
                        result.output(),
                        StandardOpenOption.TRUNCATE_EXISTING);
            
            // fix ssh-config for windows
            // remove UserKnownHostsFile line
            // identity file probably wrong too
            List<String> filteredConfig
                = Files.lines(sshConfigFile.toPath())
                    .filter((line) -> !line.contains("UserKnownHostsFile"))
                    .map((line) -> (!line.contains("IdentityFile") ? line : line.replace("\"", "")))
                    .collect(Collectors.toList());
        
            Files.write(sshConfigFile.toPath(),
                        filteredConfig,
                        StandardOpenOption.TRUNCATE_EXISTING);
            
            return sshConfigFile;
        } catch (IOException | InterruptedException | TimeoutException | InvalidExitValueException e) {
            throw new VagrantException(e.getMessage(), e);
        }
    }
    
    static List<String> lines(ProcessResult result) {
        String output = result.outputUTF8();
        String[] split = output.split("[\\r\\n]");
        List<String> lines = new ArrayList<>();
        for (String s : split) {
            lines.add(s.trim());
        }
        return lines;
    }
    
    static Map<String,Status> parseStatus(List<String> lines) {
        Map<String,Status> statuses = new LinkedHashMap<>();
        
        lines.forEach((line) -> {
            String[] values = line.split(",");
            String id = values[0];
            String name = values[1];
            String key = values[2];
            String value = values[3];
            
            Status status = statuses.get(id);
            
            if (status == null) {
                status = new Status(id, name);
                statuses.put(id, status);
            }
            
            status.getValues().put(key, value);
        });
        
        return statuses;
    }
    
}

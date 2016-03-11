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
import java.util.HashMap;
import java.util.Map;

/**
 * An empty vagrant client to simplify use.
 */
public class EmptyVagrantClient implements VagrantClient {

    private final File workingDirectory;

    public EmptyVagrantClient(File workingDirectory) {
        this.workingDirectory = workingDirectory;
    }
    
    @Override
    public File workingDirectory() {
        return this.workingDirectory;
    }
    
    @Override
    public boolean areAllMachinesRunning() throws UncheckedVagrantException {
        return false;
    }

    @Override
    public boolean areAllMachinesRunning(boolean refresh) throws VagrantException {
        return false;
    }

    @Override
    public boolean areAnyMachinesRunning() throws UncheckedVagrantException {
        return false;
    }

    @Override
    public boolean areAnyMachinesRunning(boolean refresh) throws VagrantException {
        return false;
    }

    @Override
    public File fetchSshConfig() throws UncheckedVagrantException {
        return null;
    }

    @Override
    public File fetchSshConfig(boolean refresh) throws VagrantException {
        return null;
    }

    @Override
    public void fetchSshConfig(File sshConfigFile) throws VagrantException {
        // do nothing
    }

    @Override
    public Map<String, VagrantStatus> fetchStatus() throws UncheckedVagrantException {
        return new HashMap<>();
    }

    @Override
    public Map<String, VagrantStatus> fetchStatus(boolean refresh) throws VagrantException {
        return new HashMap<>();
    }

}

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
package com.fizzed.blaze.ssh;

import java.nio.file.Path;

public class SshFile {

    private final String path2;
    private final Path path;
    private final SshFileAttributes attributes;

    public SshFile(String path2, Path path, SshFileAttributes attributes) {
        this.path2 = path2;
        this.path = path;
        this.attributes = attributes;
    }

    public String path2() {
        return this.path2;
    }

    public Path path() {
        return this.path;
    }
    
    public String fileName() {
        return this.path.getFileName().toString();
    }

    public SshFileAttributes attributes() {
        return this.attributes;
    }
    
}

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
package com.fizzed.blaze.ivy;

public class MavenMirror {
    
    private String id;
    private String name;
    private String url;
    private String mirrorOf;

    public String getId() {
        return id;
    }

    public MavenMirror setId(String id) {
        this.id = id;
        return this;
    }

    public String getName() {
        return name;
    }

    public MavenMirror setName(String name) {
        this.name = name;
        return this;
    }

    public String getUrl() {
        return url;
    }

    public MavenMirror setUrl(String url) {
        this.url = url;
        return this;
    }

    public String getMirrorOf() {
        return mirrorOf;
    }

    public MavenMirror setMirrorOf(String mirrorOf) {
        this.mirrorOf = mirrorOf;
        return this;
    }

}
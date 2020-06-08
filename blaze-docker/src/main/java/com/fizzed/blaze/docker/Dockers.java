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
import com.fizzed.blaze.util.MutableUri;
import java.net.URI;

public class Dockers {
 
    static public DockerConnect dockerConnect(String uri) {
        return dockerConnect(MutableUri.of(uri));
    }
    
    static public DockerConnect dockerConnect(URI uri) {
        return dockerConnect(new MutableUri(uri));
    }
    
    static public DockerConnect dockerConnect(MutableUri uri) {
        return new DockerConnect(Contexts.currentContext(), uri);
    }
    
}
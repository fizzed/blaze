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
package com.fizzed.blaze.util;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Path;

public class StreamableOutput extends Streamable<OutputStream> {

    private final boolean flushable;
    
    public StreamableOutput(OutputStream stream, String name, Path path, Long size, boolean closeable, boolean flushable) {
        super(stream, name, path, size, closeable);
        this.flushable = flushable;
    }
    
    public boolean flushable() {
        return flushable;
    }

    public void flush() throws IOException {
        if (flushable()) {
            stream().flush();
        }
    }
    
    @Override
    public void close() throws IOException {
        flush();
        super.close();
    }
    
}

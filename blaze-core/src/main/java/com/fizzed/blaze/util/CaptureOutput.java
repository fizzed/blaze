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

import com.fizzed.blaze.core.WrappedBlazeException;
import org.apache.commons.io.output.TeeOutputStream;

import java.io.ByteArrayOutputStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

/**
 *
 * @author joelauer
 */
public class CaptureOutput extends StreamableOutput {

    private final ByteArrayOutputStream captureOutputStream;
    private final StreamableOutput otherOutput;
    
    public CaptureOutput() {
        this(Streamables.standardOutput());
    }

    public CaptureOutput(StreamableOutput otherOutput) {
        this(new ByteArrayOutputStream(), otherOutput);
    }
    
    private CaptureOutput(ByteArrayOutputStream captureOutputStream, StreamableOutput otherOutput) {
        super(new TeeOutputStream(captureOutputStream, otherOutput.stream()), "<capture>", null, null);
        this.captureOutputStream = captureOutputStream;
        this.otherOutput = otherOutput;
    }
    
    @Override
    public String toString() {
        return asString();
    }
    
    public String asString() {
        return asString(StandardCharsets.UTF_8);
    }
    
    public String asString(Charset charset) {
        try {
            return this.captureOutputStream.toString(charset.name());
        } catch (UnsupportedEncodingException e) {
            throw new WrappedBlazeException(e);
        }
    }
    
    public byte[] asBytes() {
        return this.captureOutputStream.toByteArray();
    }
    
}

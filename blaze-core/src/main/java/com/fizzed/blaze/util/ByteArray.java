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

import java.nio.charset.Charset;

public class ByteArray {
    
    static private final int DEFAULT_SIZE = 16384;
    static private final int INFINITE_MAX_SIZE = -1;
    
    private final int maxSize;
    private byte[] buffer;
    private int length;

    public ByteArray() {
        this(new byte[DEFAULT_SIZE], INFINITE_MAX_SIZE);
    }
    
    public ByteArray(int initialSize, int maxSize) {
        this(new byte[initialSize], maxSize);
    }
    
    public ByteArray(byte[] buffer, int maxSize) {
        if (maxSize != INFINITE_MAX_SIZE && buffer.length > maxSize) {
            throw new IllegalArgumentException("maxSize exceeded initialSize");
        }
        this.maxSize = maxSize;
        this.buffer = buffer;
        this.length = 0;
    }
    
    public byte[] backingArray() {
        return this.buffer;
    }
    
    public byte get(int index) {
        return this.buffer[index];
    }
    
    public boolean isInfinite() {
        return this.maxSize == INFINITE_MAX_SIZE;
    }
    
    public int remaining() {
        return this.buffer.length - this.length;
    }
    
    public int length() {
        return this.length;
    }
    
    public void reset() {
        this.length = 0;
    }
    
    public void ensureSize(int size) {
        int delta = size - this.remaining();
        if (delta > 0) {
            byte[] newBuffer = new byte[size];
            System.arraycopy(this.buffer, 0, newBuffer, 0, this.length);
            this.buffer = newBuffer;
        }
    }
    
    public void append(byte[] buffer, int offset, int length) {
        ensureSize(this.length + length);
        System.arraycopy(buffer, offset, this.buffer, this.length, length);
        this.length += length;
    }
    
    public String toString(Charset charset) {
        return new String(this.buffer, 0, this.length, charset);
    }
    
}

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
    
    private byte[] buffer;
    private int length;

    public ByteArray() {
        this(new byte[1024]);
    }
    
    public ByteArray(int initialCapacity) {
        this(new byte[initialCapacity]);
    }
    
    public ByteArray(byte[] buffer) {
        this.buffer = buffer;
        this.length = 0;
    }
    
    public byte[] array() {
        return this.buffer;
    }
    
    public byte get(int index) {
        return this.buffer[index];
    }
    
    public int length() {
        return this.length;
    }
    
    public int capacity() {
        return this.buffer.length - this.length;
    }
    
    public void reset() {
        this.length = 0;
    }
    
    public void ensureCapacity(int capacity) {
        int delta = capacity - this.capacity();
        if (delta > 0) {
            byte[] newBuffer = new byte[capacity];
            System.arraycopy(this.buffer, 0, newBuffer, 0, this.length);
            this.buffer = newBuffer;
        }
    }
    
    public void append(byte[] buffer, int offset, int length) {
        ensureCapacity(this.length + length);
        System.arraycopy(buffer, offset, this.buffer, this.length, length);
        this.length += length;
    }
    
    public String toString(Charset charset) {
        return new String(this.buffer, 0, this.length, charset);
    }
    
}

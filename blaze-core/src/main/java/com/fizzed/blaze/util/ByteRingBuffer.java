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

//
// Copyright 2014 Christian d'Heureuse, Inventec Informatik AG, Zurich, Switzerland
// www.source-code.biz, www.inventec.ch/chdh
//
// This module is multi-licensed and may be used under the terms
// of any of the following licenses:
//
//  EPL, Eclipse Public License, V1.0 or later, http://www.eclipse.org/legal
//  LGPL, GNU Lesser General Public License, V2.1 or later, http://www.gnu.org/licenses/lgpl.html
//
// Please contact the author if you need another license.
// This module is provided "as is", without warranties of any kind.
/**
 * A ring buffer (circular buffer, FIFO) for bytes.
 *
 * <p>
 * All methods of this class are non-blocking and not synchronized (not
 * thread-safe).
 */
public class ByteRingBuffer {

    private byte[] rBuf;                         // ring buffer data
    private int rBufSize;                     // ring buffer size
    private int rBufPos;                      // position of first (oldest) data byte within the ring buffer
    private int rBufUsed;                     // number of used data bytes within the ring buffer

    /**
     * Creates a ring buffer.
     */
    public ByteRingBuffer(int size) {
        if (size <= 0) {
            throw new IllegalArgumentException();
        }
        rBufSize = size;
        rBuf = new byte[rBufSize];
    }

    /**
     * Resizes the ring buffer by preserving it's data.
     *
     * <p>
     * If the new size is not enough to keep all used data in the ring buffer,
     * the excess old data is discarded.
     */
    public void resize(int newSize) {
        if (newSize <= 0) {
            throw new IllegalArgumentException();
        }
        if (newSize < rBufUsed) {                               // if new buffer is too small to contain all data
            discard(rBufUsed - newSize);
        }                       // discard oldest data
        byte[] newBuf = new byte[newSize];
        int newBufUsed = get(newBuf, 0, newSize);              // transfer data to new buffer
        rBuf = newBuf;
        rBufSize = newSize;
        rBufPos = 0;
        rBufUsed = newBufUsed;
    }

    /**
     * Returns the size of the ring buffer.
     * @return 
     */
    public int getSize() {
        return rBufSize;
    }

    /**
     * Returns the number of free bytes within the ring buffer.
     * @return 
     */
    public int getFree() {
        return rBufSize - rBufUsed;
    }

    /**
     * Returns the number of used bytes within the ring buffer.
     * @return 
     */
    public int getUsed() {
        return rBufUsed;
    }

    /**
     * Clears the ring buffer.
     */
    public void clear() {
        rBufPos = 0;
        rBufUsed = 0;
    }

    /**
     * Discards the oldest <code>len</code> bytes within the ring buffer. This
     * has the same effect as calling <code>read(new byte[len], 0, len)</code>.
     *
     * @param len The number of bytes to be discarded.
     */
    public void discard(int len) {
        if (len < 0) {
            throw new IllegalArgumentException();
        }
        int trLen = Math.min(len, rBufUsed);
        rBufPos = clip(rBufPos + trLen);
        rBufUsed -= trLen;
    }

    /**
     * Puts data to the ring buffer.
     *
     * @param buf
     * @param pos
     * @param len
     * @return The number of bytes written. This is guaranteed to be
     * <code>min(len, getFree())</code>.
     */
    public int put(byte[] buf, int pos, int len) {
        if (len < 0) {
            throw new IllegalArgumentException();
        }
        if (rBufUsed == 0) {
            rBufPos = 0;
        }                                       // (speed optimization)
        int p1 = rBufPos + rBufUsed;
        if (p1 < rBufSize) {                                    // free space in two pieces
            int trLen1 = Math.min(len, rBufSize - p1);
            append(buf, pos, trLen1);
            int trLen2 = Math.min(len - trLen1, rBufPos);
            append(buf, pos + trLen1, trLen2);
            return trLen1 + trLen2;
        } else {                                                 // free space in one piece
            int trLen = Math.min(len, rBufSize - rBufUsed);
            append(buf, pos, trLen);
            return trLen;
        }
    }

    /**
     * Puts data to the ring buffer.
     *
     * <p>
     * Convenience method for: <code>write(buf, 0, buf.length)</code>
     * @param buf
     * @return 
     */
    public int put(byte[] buf) {
        return put(buf, 0, buf.length);
    }

    private void append(byte[] buf, int pos, int len) {
        if (len == 0) {
            return;
        }
        if (len < 0) {
            throw new AssertionError();
        }
        int p = clip(rBufPos + rBufUsed);
        System.arraycopy(buf, pos, rBuf, p, len);
        rBufUsed += len;
    }

    /**
     * Gets data from the ring buffer.
     *
     * @return The number of bytes read. This is guaranteed to be
     * <code>min(len, getUsed())</code>.
     */
    public int get(byte[] buf, int pos, int len) {
        if (len < 0) {
            throw new IllegalArgumentException();
        }
        int trLen1 = Math.min(len, Math.min(rBufUsed, rBufSize - rBufPos));
        remove(buf, pos, trLen1);
        int trLen2 = Math.min(len - trLen1, rBufUsed);
        remove(buf, pos + trLen1, trLen2);
        return trLen1 + trLen2;
    }

    /**
     * Gets data from the ring buffer.
     *
     * <p>
     * Convenience method for: <code>read(buf, 0, buf.length)</code>
     */
    public int get(byte[] buf) {
        return get(buf, 0, buf.length);
    }

    private void remove(byte[] buf, int pos, int len) {
        if (len == 0) {
            return;
        }
        if (len < 0) {
            throw new AssertionError();
        }
        System.arraycopy(rBuf, rBufPos, buf, pos, len);
        rBufPos = clip(rBufPos + len);
        rBufUsed -= len;
    }

    private int clip(int p) {
        return (p < rBufSize) ? p : (p - rBufSize);
    }
}
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
package com.fizzed.blaze.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.InterruptedIOException;
import java.util.concurrent.atomic.AtomicReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An InputStream whose read() methods can be interrupted by way of utilizing
 * available() and Thread.sleep() before entering a blocking call.
 */
public class InterruptibleInputStream extends WrappedInputStream {
    static private final Logger log = LoggerFactory.getLogger(InterruptibleInputStream.class);
        
    private final AtomicReference<Thread> readThreadRef;
    private final long timeout;
    
    public InterruptibleInputStream(InputStream input) {
        this(input, 50L);
    }
    
    public InterruptibleInputStream(InputStream input, long timeout) {
        super(input);
        this.readThreadRef = new AtomicReference<>();
        this.timeout = timeout;
    }

    @SuppressWarnings("SleepWhileInLoop")
    private void sleepUntilReadWouldBeNonBlocking() throws IOException {
        // this is the thread that would be blocked on the read call
        this.readThreadRef.set(Thread.currentThread());
        try {
            while (this.available() == 0) {
                try {
                    Thread.sleep(timeout);
                } catch (InterruptedException e) {
                    throw new InterruptedIOException("Interrupted while waiting for data");
                }
            }
        } finally {
            this.readThreadRef.set(null);
        }
    }
    
    @Override
    public int read() throws IOException {
        sleepUntilReadWouldBeNonBlocking();
        return super.read();
    }

    @Override
    public int read(byte[] b) throws IOException {
        sleepUntilReadWouldBeNonBlocking();
        return super.read(b);
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        sleepUntilReadWouldBeNonBlocking();
        return super.read(b, off, len);
    }

    @Override
    public void close() throws IOException {
        // close the input then interrupt the thread waiting on it
        super.close();
        // atomically get thread if blocked in read, interrupt it, then set to null
        this.readThreadRef.getAndUpdate((Thread readThread) -> {
            if (readThread != null) {
                log.trace("Interrupting thread {}", readThread);
                readThread.interrupt();
            }
            return readThread;
        });
    }
    
}

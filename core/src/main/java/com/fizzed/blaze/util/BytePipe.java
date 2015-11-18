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
import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BytePipe {
    static private final Logger log = LoggerFactory.getLogger(BytePipe.class);
    
    private final ReentrantLock lock;
    private final Condition writeSignal;
    private final Condition readSignal;
    private final ByteRingBuffer buffer;
    private final AtomicBoolean outputClosed;
    private final AtomicBoolean inputClosed;
    private final BytePipeOutputStream output;
    private final BytePipeInputStream input;
    
    public BytePipe() {
        this(16384);
    }
    
    public BytePipe(int bufferSize) {
        if (bufferSize <= 0) {
            throw new IllegalArgumentException("bufferSize must be > 0");
        }
        this.lock = new ReentrantLock();
        this.writeSignal = this.lock.newCondition();
        this.readSignal = this.lock.newCondition();
        this.buffer = new ByteRingBuffer(bufferSize);
        this.outputClosed = new AtomicBoolean(false);
        this.inputClosed = new AtomicBoolean(false);
        this.output = new BytePipeOutputStream();
        this.input = new BytePipeInputStream();
    }
    
    public OutputStream getOutputStream() {
        return this.output;
    }
    
    public InputStream getInputStream() {
        return this.input;
    }
    
    public class BytePipeOutputStream extends OutputStream {

        @Override
        public void close() throws IOException {
            lock.lock();
            try {
                // closing is like writing (since anyone reading needs to get an EOF)
                outputClosed.set(true);
                writeSignal.signal();
            } finally {
                lock.unlock();
            }
        }

        @Override
        public void flush() throws IOException {
            // hmmm... not sure this should do anything in this case
        }

        @Override
        public void write(byte[] bytes, int offset, int length) throws IOException {
            lock.lock();
            try {
                if (inputClosed.get()) {
                    throw new IOException("Pipe input is closed");
                }

                while (length > 0) {
                    // is there any room?
                    while (buffer.getFree() <= 0) {
                        // verify once again if the input was closed
                        if (inputClosed.get()) {
                            throw new IOException("Pipe input is closed");
                        }
                        
                        //log.debug("waiting for read so {} bytes can be written", length);
                        readSignal.await();
                    }

                    int putLength = Math.min(buffer.getFree(), length);
                    
                    //log.debug("trying to put {} bytes ({} free)", putLength, buffer.getFree());
                    
                    buffer.put(bytes, offset, putLength);
                    
                    //log.debug("put {} bytes into buffer ({} free)", putLength, buffer.getFree());
                    
                    offset += putLength;
                    length -= putLength;
                    
                    // signal data written
                    writeSignal.signal();
                }
            } catch (InterruptedException e) {
                throw new IOException("Interrupted while waiting for signal", e);
            } finally {
                lock.unlock();
            }
        }

        @Override
        public void write(byte[] bytes) throws IOException {
            write(bytes, 0, bytes.length);
        }

        @Override
        public void write(int b) throws IOException {
            throw new UnsupportedOperationException("Writing a single byte is massively innefficient!");
        }
        
    }
    
    public class BytePipeInputStream extends InputStream {

        @Override
        public boolean markSupported() {
            return false;
        }

        @Override
        public synchronized void reset() throws IOException {
            throw new UnsupportedOperationException();
        }

        @Override
        public synchronized void mark(int readlimit) {
            throw new UnsupportedOperationException();
        }

        @Override
        public long skip(long n) throws IOException {
            throw new UnsupportedOperationException();
        }
        
        @Override
        public int available() throws IOException {
            lock.lock();
            try {
                return buffer.getUsed();
            } finally {
                lock.unlock();
            }
        }
        
        @Override
        public void close() throws IOException {
            lock.lock();
            try {
                // closing is like reading (since anyone waiting to write needs to throw an exception)
                inputClosed.set(true);
                readSignal.signal();
            } finally {
                lock.unlock();
            }
        }

        @Override
        public int read(byte[] bytes, int offset, int length) throws IOException {
            lock.lock();
            try {
                // are there any bytes available?
                while (buffer.getUsed() <= 0) {
                    // was the output closed?
                    if (outputClosed.get()) {
                        // return EOF
                        return -1;
                    }

                    //log.debug("waiting for write so up to {} bytes can be read", length);
                    writeSignal.await();
                }
                    
                int getLength = Math.min(buffer.getUsed(), length);

                //log.debug("trying to get {} bytes ({} available)", getLength, buffer.getUsed());
                
                buffer.get(bytes, offset, getLength);

                //log.debug("get {} bytes into buffer ({} available)", getLength, buffer.getUsed());

                // signal data read
                readSignal.signal();
                
                return getLength;
            } catch (InterruptedException e) {
                throw new IOException("Interrupted while waiting for signal", e);
            } finally {
                lock.unlock();
            }
        }

        @Override
        public int read(byte[] bytes) throws IOException {
            return read(bytes, 0, bytes.length);
        }

        @Override
        public int read() throws IOException {
            throw new UnsupportedOperationException("Reading a single byte is massively innefficient!");
        }
    
    }
    
}

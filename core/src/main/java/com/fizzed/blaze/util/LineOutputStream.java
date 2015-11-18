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

import com.fizzed.blaze.util.LineOutputStream.Processor;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Deque;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.atomic.AtomicReference;

/**
 * An OutputStream that finds full lines of text (either '\r' or '\r\n') and
 * applies a function to it.  Allows processing of line-based output as its
 * written.
 * 
 * @author joelauer
 * @param <P>
 */
public class LineOutputStream<P extends Processor> extends OutputStream {
    
    private static final byte CARRIAGE_RETURN = (byte)'\r';
    private static final byte NEWLINE = (byte)'\n';
    
    static public class BufferingProcessor implements Processor {
        private final ConcurrentLinkedDeque<String> lines;

        public BufferingProcessor() {
            this.lines = new ConcurrentLinkedDeque<>();
        }
        
        public Deque<String> lines() {
            return this.lines;
        }
        
        @Override
        public void process(String line) {
            this.lines.add(line);
        }
    }
    
    static public class LastLineProcessor implements Processor {
        private String lastLine;

        public LastLineProcessor() {
            this.lastLine = null;
        }
        
        public String lastLine() {
            return this.lastLine;
        }
        
        @Override
        public void process(String line) {
            this.lastLine = line;
        }
    }
    
    static public LineOutputStream<BufferingProcessor> buffering() {
        return buffering(StandardCharsets.UTF_8);
    }
    
    static public LineOutputStream<BufferingProcessor> buffering(Charset charset) {
        return new LineOutputStream<>(new BufferingProcessor(), charset);
    }
    
    static public LineOutputStream<LastLineProcessor> lastLine() {
        return lastLine(StandardCharsets.UTF_8);
    }
    
    static public LineOutputStream<LastLineProcessor> lastLine(Charset charset) {
        return new LineOutputStream<>(new LastLineProcessor(), charset);
    }
    
    private boolean closed;
    private final ByteArray buffer;
    private final Charset charset;
    private final AtomicReference<P> processorRef;
    
    public LineOutputStream(P processor) {
        this(processor, null);
    }
    
    public LineOutputStream(P processor, Charset charset) {
        this.closed = false;
        this.buffer = new ByteArray(4096);
        this.charset = (charset != null ? charset : StandardCharsets.UTF_8);
        this.processorRef = new AtomicReference<>(processor);
    }
    
    private void verifyNotClosed() throws IOException {
        if (this.closed) {
            throw new IOException("closed");
        }
    }
    
    public P processor() {
        return this.processorRef.get();
    }
    
    public void processor(P processor) {
        this.processorRef.set(processor);
    }
    
    @Override
    public void close() throws IOException {
        this.closed = true;
    }

    @Override
    public void flush() throws IOException {
        // do nothing
    }
    
    @Override
    public void write(byte[] buffer, int offset, int length) throws IOException {
        verifyNotClosed();
        
        // scan array for lines
        int pos = offset;
        
        for (int i = offset; i < length; i++) {
            byte b = buffer[i];
            
            if (b == CARRIAGE_RETURN || b == NEWLINE) {
                String line = null;
                
                // is there any previous buffer not processed?
                if (this.buffer.length() > 0) {
                    StringBuilder sb = new StringBuilder();
                    // append current buffer + new buffer as strings
                    sb.append(this.buffer.toString(this.charset));
                    sb.append(new String(buffer, pos, i, this.charset));
                    line = sb.toString();
                    this.buffer.reset();
                } else {
                    line = new String(buffer, pos, i, this.charset);
                }
                
                // either process OR accumulate
                processorRef.get().process(line);
                
                pos = i + 1;
                
                // if this was a carriage return do we need to skip the next byte if newline?
                if (b == CARRIAGE_RETURN && (i+1) < length && buffer[i+1] == NEWLINE) {
                    // skip next byte
                    pos++;
                    i++;
                }
            }
        }
        
        // append unprocessed to internally expanding buffer
        if (pos < length) {
            this.buffer.append(buffer, pos, length - pos);
        }
    }

    @Override
    public void write(byte[] b) throws IOException {
        write(b, 0, b.length);
    }

    @Override
    public void write(int b) throws IOException {
        write(new byte[] { (byte)b }, 0, 1);
    }
    
    static public interface Processor {
        void process(String line);
    }
    
}

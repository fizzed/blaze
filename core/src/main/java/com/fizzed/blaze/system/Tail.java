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
package com.fizzed.blaze.system;

import com.fizzed.blaze.Context;
import com.fizzed.blaze.core.Action;
import com.fizzed.blaze.core.BlazeException;
import com.fizzed.blaze.util.ObjectHelper;
import java.io.InputStream;
import java.io.OutputStream;
import com.fizzed.blaze.core.PipeMixin;
import com.fizzed.blaze.core.WrappedBlazeException;
import com.fizzed.blaze.util.NamedStream;
import static com.fizzed.blaze.util.NamedStream.lineProcessor;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.stream.Stream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author joelauer
 */
public class Tail extends Action<Deque<String>> implements PipeMixin<Tail> {
    static private final Logger log = LoggerFactory.getLogger(Tail.class);

    public static final byte[] DEFAULT_NEWLINE = new byte[] { '\r', '\n' };
    
    private NamedStream<InputStream> pipeInput;
    private NamedStream<OutputStream> pipeOutput;
    private byte[] newline;
    private Charset charset;
    private int count;
    
    public Tail(Context context) {
        super(context);
        this.newline = DEFAULT_NEWLINE;
        this.charset = StandardCharsets.UTF_8;
        this.count = 10;
    }
    
    public Tail newline(byte[] newline) {
        this.newline = newline;
        return this;
    }
    
    public Tail charset(Charset charset) {
        this.charset = charset;
        return this;
    }
    
    public Tail count(int count) {
        this.count = count;
        return this;
    }
    
    @Override
    public NamedStream<InputStream> getPipeInput() {
        return this.pipeInput;
    }

    @Override
    public NamedStream<OutputStream> getPipeOutput() {
        return this.pipeOutput;
    }
    
    @Override
    public Tail pipeInput(NamedStream<InputStream> pipeInput) {
        this.pipeInput = pipeInput;
        return this;
    }
    
    @Override
    public Tail pipeOutput(NamedStream<OutputStream> pipeOutput) {
        this.pipeOutput = pipeOutput;
        return this;
    }
    
    @Override
    protected Deque<String> doRun() throws BlazeException {
        ObjectHelper.requireNonNull(pipeInput, "pipeInput is required");

        final Deque<String> lines = new ArrayDeque<>(this.count);
        
        NamedStream<OutputStream> lineOutput
            = lineProcessor((line) -> {
                log.info("line: {}", line);
                if (lines.size() >= this.count) {
                    lines.remove();
                }
                lines.add(line);
            });
        
        try {
            NamedStream.pipe(this.pipeInput, lineOutput);
            // closing is important to finish any unprocessed buffer as a line...
            lineOutput.close();
        } catch (IOException e) {
            throw new WrappedBlazeException(e);
        }
        
        log.debug("lines {}", lines);
        
        if (this.pipeOutput != null) {
            OutputStream os = this.pipeOutput.stream();
            for (String line : lines) {
                try {
                    os.write(line.getBytes(this.charset));
                    os.write(this.newline);
                } catch (IOException e) {
                    throw new WrappedBlazeException(e);
                }
            }
        }
        
        return lines;
    }
}

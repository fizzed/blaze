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
import java.io.OutputStream;
import com.fizzed.blaze.core.PipeMixin;
import com.fizzed.blaze.core.WrappedBlazeException;
import com.fizzed.blaze.util.StreamableInput;
import com.fizzed.blaze.util.StreamableOutput;
import com.fizzed.blaze.util.Streamables;
import static com.fizzed.blaze.util.Streamables.lineOutput;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayDeque;
import java.util.Deque;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

abstract public class AbstractLineAction<T extends AbstractLineAction> extends Action<Deque<String>> implements PipeMixin<T> {
    static private final Logger log = LoggerFactory.getLogger(AbstractLineAction.class);

    public static final byte[] DEFAULT_NEWLINE = new byte[] { '\r', '\n' };
    
    private StreamableInput pipeInput;
    private StreamableOutput pipeOutput;
    private byte[] newline;
    private Charset charset;
    
    public AbstractLineAction(Context context) {
        super(context);
        this.newline = DEFAULT_NEWLINE;
        this.charset = StandardCharsets.UTF_8;
    }
    
    public T newline(byte[] newline) {
        this.newline = newline;
        return (T)this;
    }
    
    public T charset(Charset charset) {
        this.charset = charset;
        return (T)this;
    }
    
    @Override
    public StreamableInput getPipeInput() {
        return this.pipeInput;
    }

    @Override
    public T pipeInput(StreamableInput pipeInput) {
        this.pipeInput = pipeInput;
        return (T)this;
    }
    
    @Override
    public StreamableOutput getPipeOutput() {
        return this.pipeOutput;
    }
    
    @Override
    public T pipeOutput(StreamableOutput pipeOutput) {
        this.pipeOutput = pipeOutput;
        return (T)this;
    }
    
    abstract protected StreamableOutput createLineOutput(final Deque<String> lines);
    
    @Override
    protected Deque<String> doRun() throws BlazeException {
        ObjectHelper.requireNonNull(pipeInput, "pipeInput is required");

        final Deque<String> lines = new ArrayDeque<>();
        
        final StreamableOutput lineOutput = createLineOutput(lines);
        
        try {
            Streamables.copy(this.pipeInput, lineOutput);
        } catch (IOException e) {
            throw new WrappedBlazeException(e);
        }
        
        Streamables.close(this.pipeInput);
        Streamables.close(lineOutput);
        
        if (this.pipeOutput != null) {
            try {
                OutputStream os = this.pipeOutput.stream();
                for (String line : lines) {
                    String s = line + "\r\n";
                    os.write(s.getBytes(this.charset));
                }
            } catch (IOException e) {
                throw new WrappedBlazeException(e);
            } finally {
                Streamables.closeQuietly(this.pipeOutput);
            }
        }
        
        return lines;
    }
}

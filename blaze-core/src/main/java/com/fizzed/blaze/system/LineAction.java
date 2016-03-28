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
import com.fizzed.blaze.core.PipeMixin;
import com.fizzed.blaze.core.WrappedBlazeException;
import com.fizzed.blaze.util.ObjectHelper;
import com.fizzed.blaze.util.StreamableInput;
import com.fizzed.blaze.util.StreamableOutput;
import com.fizzed.blaze.util.Streamables;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.ArrayDeque;
import java.util.Deque;
import org.apache.commons.io.Charsets;

abstract public class LineAction<A extends LineAction, R extends com.fizzed.blaze.core.Result<?,V,R>,V> extends Action<R,V> implements PipeMixin<A> {
    
    protected Charset charset;
    protected StreamableInput pipeInput;
    protected StreamableOutput pipeOutput;
    protected int count;
    
    public LineAction(Context context) {
        super(context);
        this.count = 10;
        this.charset = Charsets.UTF_8;
    }
    
    public A charset(Charset charset) {
        this.charset = charset;
        return (A)this;
    }
    
    @Override
    public StreamableInput getPipeInput() {
        return this.pipeInput;
    }

    @Override
    public A pipeInput(StreamableInput pipeInput) {
        this.pipeInput = pipeInput;
        return (A)this;
    }
    
    @Override
    public StreamableOutput getPipeOutput() {
        return this.pipeOutput;
    }
    
    @Override
    public A pipeOutput(StreamableOutput pipeOutput) {
        this.pipeOutput = pipeOutput;
        return (A)this;
    }
    
    public A count(int count) {
        this.count = count;
        return (A)this;
    }
    
    static public interface LineOutputSupplier {
        public StreamableOutput create(Deque<String> lines);
    }

    static public Deque<String> processLines(final Charset charset, final PipeMixin pipable, final LineOutputSupplier lineOuputSupplier) throws BlazeException {
        ObjectHelper.requireNonNull(pipable.getPipeInput(), "pipeInput is required");

        final Deque<String> lines = new ArrayDeque<>();
        
        final StreamableOutput lineOutput = lineOuputSupplier.create(lines);
        
        try {
            Streamables.copy(pipable.getPipeInput(), lineOutput);
        } catch (IOException e) {
            throw new WrappedBlazeException(e);
        }
        
        Streamables.close(pipable.getPipeInput());
        Streamables.close(lineOutput);
        
        if (pipable.getPipeOutput() != null) {
            try {
                OutputStream os = pipable.getPipeOutput().stream();
                for (String line : lines) {
                    String s = line + "\r\n";
                    os.write(s.getBytes(charset));
                }
            } catch (IOException e) {
                throw new WrappedBlazeException(e);
            } finally {
                Streamables.closeQuietly(pipable.getPipeOutput());
            }
        }
        
        return lines;
    }
    
}

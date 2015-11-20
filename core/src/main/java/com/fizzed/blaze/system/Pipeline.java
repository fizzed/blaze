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
import com.fizzed.blaze.util.BytePipe;
import com.fizzed.blaze.util.StreamableInput;
import com.fizzed.blaze.util.StreamableOutput;
import com.fizzed.blaze.util.Streamables;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Pipeline extends Action<Pipeline.Result,Void> implements PipeMixin<Pipeline> {
    static private final Logger log = LoggerFactory.getLogger(Pipeline.class);

    private StreamableInput pipeInput;
    private StreamableOutput pipeOutput;
    private final List<PipeMixin> pipables;
    
    public Pipeline(Context context) {
        super(context);
        this.pipables = new ArrayList<>();
    }
    
    @Override
    public StreamableInput getPipeInput() {
        return this.pipeInput;
    }

    @Override
    public Pipeline pipeInput(StreamableInput pipeInput) {
        this.pipeInput = pipeInput;
        return this;
    }

    @Override
    public StreamableOutput getPipeOutput() {
        return this.pipeOutput;
    }

    @Override
    public Pipeline pipeOutput(StreamableOutput pipeOutput) {
        this.pipeOutput = pipeOutput;
        return this;
    }
    
    public Pipeline add(PipeMixin pipable) {
        if (!(pipable instanceof Action)) {
            throw new IllegalArgumentException("pipable must be an instance of " + Action.class.getCanonicalName());
        }
        
        if (this.pipables.size() > 0) {
            PipeMixin lastPipable = this.pipables.get(this.pipables.size() - 1);
            
            // connect output to input
            log.debug("Connecting {} output -> {} input", lastPipable.getClass(), pipable.getClass());
                
            BytePipe pipe = new BytePipe();
            lastPipable.pipeOutput(Streamables.output(pipe.getOutputStream(), "<pipe>", true, true));
            pipable.pipeInput(Streamables.input(pipe.getInputStream(), "<pipe>", true));
        }
        
        this.pipables.add(pipable);
        
        return this;
    }

    @Override
    protected Result doRun() throws BlazeException {
        ExecutorService executor = Executors.newFixedThreadPool(this.pipables.size());
        
        // apply input to first action
        if (this.pipeInput != null) {
            PipeMixin firstPipable = this.pipables.get(0);
            firstPipable.pipeInput(this.pipeInput);
        }
        
        // apply output to last action
        if (this.pipeOutput != null) {
            PipeMixin lastPipable = this.pipables.get(this.pipables.size() - 1);
            lastPipable.pipeOutput(this.pipeOutput);
        }
        
        final List<Future> futures = new ArrayList<>();
        
        this.pipables.stream().forEach((pipable) -> {
            futures.add(executor.submit(() -> {
                Action action = (Action)pipable;
                
                log.debug("Running action {}", action.getClass());
                
                action.run();
                
                // closing input and output after action is done is critical
                // for pipeline to continue processing correctly and EOF's triggered
                Streamables.closeQuietly(pipable.getPipeInput());
                Streamables.closeQuietly(pipable.getPipeOutput());
            }));
        });
        
        // wait for all to finish
        futures.stream().forEach((Future f) -> {
            try {
                log.debug("waiting for future to finish");
                f.get();
            } catch (ExecutionException e) {
                throw new WrappedBlazeException(e.getCause());
            } catch (InterruptedException e) {
                throw new WrappedBlazeException(e);
            }
        });
        
        log.debug("waiting for executor shutdown...");
        executor.shutdown();
        
        return new Result(this, null);
    }
    
    static public class Result extends com.fizzed.blaze.core.Result<Pipeline,Void,Result> {
        
        Result(Pipeline action, Void value) {
            super(action, value);
        }
        
    }
    
}

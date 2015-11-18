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
import com.fizzed.blaze.util.NamedStream;
import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Pipeline extends Action<Void> {
    static private final Logger log = LoggerFactory.getLogger(Pipeline.class);

    private final List<PipeMixin> actions;
    
    public Pipeline(Context context) {
        super(context);
        this.actions = new ArrayList<>();
    }
    
    public Pipeline add(PipeMixin action) {
        if (!(action instanceof Action)) {
            throw new IllegalArgumentException("action must be of type " + Action.class.getCanonicalName());
        }
        
        if (this.actions.size() > 0) {
            PipeMixin lastAction = this.actions.get(this.actions.size() - 1);
            
            // connect output to input
            try {
                PipedOutputStream pos = new PipedOutputStream();
                PipedInputStream pis = new PipedInputStream(pos);
                
                lastAction.pipeOutput(NamedStream.of(pos, "<pipe>", true));
                action.pipeInput(NamedStream.of(pis, "<pipe>", true));
            } catch (IOException e) {
                throw new WrappedBlazeException(e);
            }
        }
        
        this.actions.add(action);
        
        return this;
    }

    @Override
    protected Void doRun() throws BlazeException {
        ExecutorService executor = Executors.newFixedThreadPool(this.actions.size());
        
        final List<Future> futures = new ArrayList<>();
        
        actions.stream().forEach((action) -> {
            futures.add(executor.submit(() -> {
                Action a = (Action)action;
                log.debug("running action {}", a.getClass());
                a.run();
                
                try {
                    //action.getPipeInput().close();
                    //action.getPipeOutput().
                } catch (Exception e) {
                    log.warn("Unable to close streams", e);
                }
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
        
        return null;
    }
    
}

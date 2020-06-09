/*
 * Copyright 2020 Fizzed, Inc.
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

import java.util.Objects;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Basic abstraction of a spin-lock waiting for something to be true. Ideally
 * you'd use real concurrent mechanisms, but that's not always available for
 * things you need to evaluate.
 * 
 * @author joelauer
 */
public class WaitFor {

    static public class Progress {
        
        final private int attempt;
        final private long timeout;
        final private long every;
        final private Timer timer;

        public Progress(int attempt, long timeout, long every, Timer timer) {
            this.attempt = attempt;
            this.timeout = timeout;
            this.every = every;
            this.timer = timer;
        }

        public int getAttempt() {
            return attempt;
        }

        public long getTimeout() {
            return timeout;
        }

        public long getEvery() {
            return every;
        }

        public Timer getTimer() {
            return timer;
        }

    }
    
    private final Function<Progress,Boolean> condition;
    
    public WaitFor(Supplier<Boolean> condition) {
        this.condition = c -> condition.get();
    }
    
    public WaitFor(Function<Progress,Boolean> condition) {
        this.condition = condition;
    }
    
    @SuppressWarnings("SleepWhileInLoop")
    public boolean await(
            long timeout,
            long every) throws InterruptedException {
        
        Objects.requireNonNull(timeout, "timeout was null");
        Objects.requireNonNull(every, "every was null");
        
        if (every > timeout) {
            throw new IllegalArgumentException("every must be <= timeout");
        }
        
        final Timer timer = new Timer();
        int attempt = 1;
        
        while (timer.elapsed() < timeout) {
            if (this.condition.apply(new Progress(attempt, timeout, every, timer))) {
                return true;
            }
            
            Thread.sleep(every);
            attempt++;
        }
        
        return false;
    }
    
}
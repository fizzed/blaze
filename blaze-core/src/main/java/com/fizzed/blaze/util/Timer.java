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

/**
 * Simple timing utility.
 * 
 * @author joelauer
 */
public class Timer {
    
    final private long start;
    private long stop;
    
    public Timer() {
        this.start = System.currentTimeMillis();
    }
    
    public Timer stop() {
        this.stop = System.currentTimeMillis();
        return this;
    }
    
    public long elapsed() {
        if (this.stop == 0) {
            return System.currentTimeMillis() - this.start;
        }
        return (this.stop - this.start);
    }
    
    public long millis() {
        return (this.stop - this.start);
    }

    @Override
    public String toString() {
        return DurationFormatter.formatShort(this.elapsed());
        //return this.elapsed() + " ms";
    }
    
}
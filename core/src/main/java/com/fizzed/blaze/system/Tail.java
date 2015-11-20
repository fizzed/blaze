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
import com.fizzed.blaze.core.BlazeException;
import static com.fizzed.blaze.util.Streamables.lineOutput;
import java.util.Deque;

public class Tail extends LineAction<Tail,Tail.Result,Deque<String>> {
    
    public Tail(Context context) {
        super(context);
    }
    
    @Override
    protected Result doRun() throws BlazeException {
        Deque<String> processedLines = LineAction.processLines(this.charset, this, (lines) -> {
            return lineOutput((line) -> {
                if (lines.size() >= this.count) {
                    lines.remove();
                }
                lines.add(line);
            });
        });
        return new Tail.Result(this, processedLines);
    }
    
    static public class Result extends com.fizzed.blaze.core.Result<Tail,Deque<String>,Result> {
        
        Result(Tail action, Deque<String> value) {
            super(action, value);
        }
        
    }
    
}

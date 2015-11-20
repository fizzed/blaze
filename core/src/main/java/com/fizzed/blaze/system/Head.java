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
import com.fizzed.blaze.core.PipeMixin;
import java.util.Deque;
import static com.fizzed.blaze.util.Streamables.lineOutput;

public class Head extends LineAction<Head,Head.Result,Deque<String>> implements PipeMixin<Head> {
    
    public Head(Context context) {
        super(context);
    }
    
    @Override
    protected Result doRun() throws BlazeException {
        Deque<String> processedLines = LineAction.processLines(this.charset, this, (lines) -> {
            return lineOutput((line) -> {
                if (lines.size() < this.count) {
                    lines.add(line);
                }
                // discard rest...
            });
        });
        return new Result(this, processedLines);
    }
    
    static public class Result extends com.fizzed.blaze.core.Result<Head,Deque<String>,Result> {
        
        Result(Head action, Deque<String> value) {
            super(action, value);
        }
        
    }
    
}

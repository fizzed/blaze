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
import com.fizzed.blaze.core.PipeMixin;
import com.fizzed.blaze.util.StreamableOutput;
import static com.fizzed.blaze.util.Streamables.lineOutput;
import java.util.Deque;

public class Head extends AbstractLineAction<Head> implements PipeMixin<Head> {
    
    private int count;
    
    public Head(Context context) {
        super(context);
        this.count = 10;
    }
    
    public Head count(int count) {
        this.count = count;
        return this;
    }
    
    @Override
    protected StreamableOutput createLineOutput(final Deque<String> lines) {
        return lineOutput((line) -> {
            if (lines.size() < this.count) {
                lines.add(line);
            }
            // discard rest...
        });
    }
    
}

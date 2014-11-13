/*
 * Copyright 2014 Fizzed Inc.
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

package co.fizzed.blaze.task;

import co.fizzed.blaze.action.Result;
import co.fizzed.blaze.core.Context;
import java.util.function.Function;

/**
 *
 * @author joelauer
 */
public class FunctionTask extends Task<Object> {
    
    private final Function function;
    
    public FunctionTask(Context context, Function Function) {
        super(context);
        this.function = Function;
    }

    public Function getFunction() {
        return function;
    }

    @Override
    protected Result<Object> executeTask() throws Exception {
        Object o = this.function.apply(null);
        return new Result(o);
    }
    
}

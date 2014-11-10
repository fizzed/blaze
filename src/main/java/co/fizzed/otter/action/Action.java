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
package co.fizzed.otter.action;

import co.fizzed.otter.core.Context;
import java.util.concurrent.Callable;

/**
 *
 * @author joelauer
 */
public abstract class Action<T> implements Callable<Result<T>> {
    
    protected final Context context;
    
    public Action(Context context) {
        this.context = context;
    }
    
    public T run() throws Exception {
        return this.call().getValue();
    }
    
}

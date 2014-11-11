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
package co.fizzed.blaze.action;

import co.fizzed.blaze.core.Context;
import java.util.concurrent.Callable;

/**
 *
 * @author joelauer
 */
public abstract class Action<T> implements Callable<T> {
    
    protected final Context context;
    private String name;
    
    public Action(Context context) {
        this(context, null);
    }
    
    public Action(Context context, String name) {
        this.context = context;
        this.name = (name != null ? name : this.getClass().getName());
    }

    public String getName() {
        return name;
    }

    public Action<T> name(String name) {
        this.name = name;
        return this;
    }
    
    @Override
    public T call() throws Exception {
        // call() function can seamlessly be run as a function in javascript
        // by using the normal () operator on it :-)
        Result<T> result = execute();
        return result.getValue();
    }
    
    // alias for call()
    public T run() throws Exception {
        return this.call();
    }
    
    // only other public method of running an action to get the full result
    public Result<T> runForResult() throws Exception {
        return this.execute();
    }
    
    // only method subclasses can implement
    abstract protected Result<T> execute() throws Exception;
    
}

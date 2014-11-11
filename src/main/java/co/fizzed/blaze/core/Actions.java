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
package co.fizzed.blaze.core;

import co.fizzed.blaze.action.Action;
import co.fizzed.blaze.action.CopyFilesAction;
import co.fizzed.blaze.action.ExecAction;
import co.fizzed.blaze.action.IvyAction;
import co.fizzed.blaze.action.ListFilesAction;
import co.fizzed.blaze.action.StorkLauncherGenerateAction;
import co.fizzed.blaze.action.WhichAction;
import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 *
 * @author joelauer
 */
public class Actions {
    
    private final Context context;
    public final ExecutorService executors = Executors.newFixedThreadPool(5);
    
    public Actions(Context context) {
        this.context = context;
    }
    
    public ExecAction exec(String... command) throws Exception {
        return new ExecAction(context).command(command);
    }
    
    public WhichAction which(String command) {
        return new WhichAction(context).command(command);
    }
    
    public ListFilesAction ls(String path) {
        return new ListFilesAction(context).path(path);
    }
    
    public ListFilesAction ls(File path) {
        return new ListFilesAction(context).path(path);
    }
    
    public CopyFilesAction cp(String path) {
        return new CopyFilesAction(context).source(new File(path));
    }
    
    public CopyFilesAction cp(File path) {
        return new CopyFilesAction(context).source(path);
    }
    
    public IvyAction ivy() throws Exception {
        return new IvyAction(context);
    }
    
    public StorkLauncherGenerateAction storkLauncherGenerate() {
        return new StorkLauncherGenerateAction(context);
    }
    
    public <T> void pipeline(Future<T> ... futures) throws Exception {
        for (Future<T> f : futures) {
            f.get();
        }
    }
    
    public <T> Future<T> async(Action<T> ... actions) {
        //for (Action<T> a : actions) {
        //    System.out.println("Submitted action with class " + a.getClass());
        //}
        
        if (actions.length == 1) {
            return executors.submit(actions[0]);
        } else {
            ChainedCallable<T> chained = new ChainedCallable<>(Arrays.asList(actions));
            return executors.submit(chained);
        }
    }
    
    static public class ChainedCallable<T> implements Callable<T> {

        private final List<Callable<T>> callables;
        
        public ChainedCallable(List<Callable<T>> callables) {
            this.callables = callables;
        }
        
        @Override
        public T call() throws Exception {
            for (Callable<T> c : callables) {
                //System.out.println("Calling " + c.getClass());
                try {
                    c.call();
                } catch (Exception e) {
                    e.printStackTrace(System.err);
                }
            }
            //System.out.println("Done with chained callable...");
            return null;
        }
        
    }
    
}
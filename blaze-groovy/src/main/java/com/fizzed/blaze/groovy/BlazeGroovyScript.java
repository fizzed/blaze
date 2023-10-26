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
package com.fizzed.blaze.groovy;

import com.fizzed.blaze.core.BlazeException;
import com.fizzed.blaze.core.BlazeTask;
import com.fizzed.blaze.jdk.TargetObjectScript;
import groovy.lang.Script;
import java.lang.reflect.Method;
import java.util.List;
import java.util.function.Predicate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BlazeGroovyScript extends TargetObjectScript {
    static final private Logger log = LoggerFactory.getLogger(BlazeGroovyScript.class);
    
    static final public Predicate<Method> FILTER_EXCLUDE_RUN_METHOD = (Method m) -> {
        return !m.getName().equals("run");
    };

    static final public Predicate<Method> FILTER_OTHER_GROOVY_METHODS = (Method m) -> {
        // skip ANY method declared from groovy classes
        return !m.getDeclaringClass().equals(groovy.lang.Script.class) && !m.getDeclaringClass().equals(groovy.lang.GroovyObjectSupport.class);
    };
    
    final private BlazeGroovyEngine engine;
    final private Script script;

    public BlazeGroovyScript(BlazeGroovyEngine engine, Script script) {
        super(script);
        this.engine = engine;
        this.script = script;
    }
    
    @Override
    public List<BlazeTask> tasks() throws BlazeException {
        return findTasks(FILTER_OBJECT_INSTANCE_METHOD, FILTER_PUBLIC_INSTANCE_METHOD, FILTER_EXCLUDE_RUN_METHOD, FILTER_OTHER_GROOVY_METHODS);
    }

    @Override
    public void execute(String task) throws BlazeException {
        Method method = findTaskMethod(task);
        
        try {
            script.invokeMethod(task, new Object[]{});
        } catch (Exception e) {
            logFirstScriptSource(e);
            if (e instanceof BlazeException) {
                throw (BlazeException)e;
            } else if (e instanceof RuntimeException) {
                throw (RuntimeException)e;
            } else {
                throw new BlazeException("Unable to execute task '" + task + "'", e);
            }
        }
    }
    
    public void logFirstScriptSource(Throwable t) {
        StackTraceElement[] stes = t.getStackTrace();
        if (stes != null) {
            for (StackTraceElement ste : stes) {
                if (ste.getFileName() != null && ste.getFileName().endsWith(".groovy")) {
                    log.error("Problem with script likey @ " + ste.getFileName() + ":" + ste.getLineNumber());
                    return;
                }
            }
        }
    }
    
}

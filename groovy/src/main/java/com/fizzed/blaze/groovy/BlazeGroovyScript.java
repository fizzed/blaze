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
import com.fizzed.blaze.core.Engine;
import com.fizzed.blaze.core.NoSuchTaskException;
import groovy.lang.Script;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import org.kohsuke.MetaInfServices;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@MetaInfServices(Engine.class)
public class BlazeGroovyScript implements com.fizzed.blaze.core.Script {
    static final private Logger log = LoggerFactory.getLogger(BlazeGroovyScript.class);
    
    final private BlazeGroovyEngine engine;
    final private Script script;

    public BlazeGroovyScript(BlazeGroovyEngine engine, Script script) {
        this.engine = engine;
        this.script = script;
    }
    
    @Override
    public List<String> tasks() throws BlazeException {
        List<String> tasks = new ArrayList<>();
        
        try {
            Method[] methods = this.script.getClass().getDeclaredMethods();
            
            for (Method m : methods) {
                
                //log.debug("method: {}", m);
                
                // groovy defines run() and a static main() method
                if (!Modifier.isStatic(m.getModifiers()) && Modifier.isPublic(m.getModifiers())) {
                    // groovy scripts have a run() method by default (filter it)
                    if (!m.getName().equals("run")) {
                        tasks.add(m.getName());
                    }
                }
            }
        } catch (SecurityException e) {
            throw new BlazeException("Unable to groovy script class", e);
        }
        
        //log.debug("tasks {}", tasks);
        
        return tasks;
    }

    @Override
    public void execute(String task) throws BlazeException {
        // verify the method (task) exists first
        Method method;
        try {
            method = this.script.getClass().getDeclaredMethod(task, new Class[]{});
        } catch (NoSuchMethodException e) {
            throw new NoSuchTaskException(task);
        } catch (SecurityException e) {
            throw new BlazeException("Unable to access task '" + task + "'", e);
        }
        
        try {
            script.invokeMethod(task, new Object[]{});
            //method.invoke(this.script, new Object[]{});
        } catch (Exception e) {
            //Throwable t = e.getCause();
            
            logFirstScriptSource(e);
            
            if (e instanceof BlazeException) {
                throw (BlazeException)e;
            } else if (e instanceof RuntimeException) {
                throw (RuntimeException)e;
            } else {
                throw new BlazeException("Unable to execute task '" + task + "'", e);
            }
        }
        //} catch (IllegalAccessException | IllegalArgumentException e) {
        //    throw new BlazeException("Unable to execute task '" + task + "'", e);
        //}
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

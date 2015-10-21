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

import com.fizzed.blaze.BlazeException;
import com.fizzed.blaze.Engine;
import com.fizzed.blaze.NoSuchTaskException;
import com.fizzed.blaze.Script;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import org.kohsuke.MetaInfServices;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@MetaInfServices(Engine.class)
public class BlazeGroovyScript implements Script {
    static final private Logger log = LoggerFactory.getLogger(BlazeGroovyScript.class);
    
    final private BlazeGroovyEngine engine;
    final private Object script;

    public BlazeGroovyScript(BlazeGroovyEngine engine, Object script) {
        this.engine = engine;
        this.script = script;
    }
    
    @Override
    public List<String> tasks() throws BlazeException {
        List<String> tasks = new ArrayList<>();
        
        try {
            Method[] methods = this.script.getClass().getDeclaredMethods();
            
            for (Method m : methods) {
                
                log.debug("method: {}", m);
                
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
        
        log.debug("tasks {}", tasks);
        
        return tasks;
    }

    @Override
    public void execute(String task) throws BlazeException {
        Method method;
        try {
            method = this.script.getClass().getDeclaredMethod(task, new Class[]{});
        } catch (NoSuchMethodException e) {
            throw new NoSuchTaskException(task);
        } catch (SecurityException e) {
            throw new BlazeException("Unable to access task '" + task + "'", e);
        }
        
        try {
            method.invoke(this.script, new Object[]{});
        } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
            throw new BlazeException("unable to execute task '" + task + "'", e);
        }
    }
    
}

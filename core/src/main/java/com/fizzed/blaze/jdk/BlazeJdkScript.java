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
package com.fizzed.blaze.jdk;

import com.fizzed.blaze.core.BlazeException;
import com.fizzed.blaze.core.NoSuchTaskException;
import com.fizzed.blaze.core.Script;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author joelauer
 */
public class BlazeJdkScript implements Script {
    
    final private BlazeJdkEngine engine;
    final private Object object;

    public BlazeJdkScript(BlazeJdkEngine engine, Object object) {
        this.engine = engine;
        this.object = object;
    }

    @Override
    public List<String> tasks() throws BlazeException {
        List<String> tasks = new ArrayList<>();
        
        try {
            Method[] methods = this.object.getClass().getDeclaredMethods();
            
            for (Method m : methods) {
                
                if (!Modifier.isStatic(m.getModifiers()) && Modifier.isPublic(m.getModifiers())) {
                    tasks.add(m.getName());
                }
            }
        } catch (SecurityException e) {
            throw new BlazeException("Unable to groovy script class", e);
        }
        
        return tasks;
    }

    @Override
    public void execute(String task) throws BlazeException {
        // verify the method (task) exists first
        Method method;
        try {
            method = object.getClass().getMethod(task, new Class[]{});
        } catch (NoSuchMethodException e) {
            throw new NoSuchTaskException(task);
        } catch (SecurityException e) {
            throw new BlazeException("Unable to access task '" + task + "'", e);
        }
        
        try {
            method.invoke(this.object, new Object[]{});
        } catch (InvocationTargetException e) {
            Throwable t = e.getCause();
            
            //logFirstScriptSource(e);
            
            if (t instanceof BlazeException) {
                throw (BlazeException)t;
            } else if (t instanceof RuntimeException) {
                throw (RuntimeException)t;
            } else {
                throw new BlazeException("Unable to execute task '" + task + "'", t);
            }
        } catch (IllegalAccessException | IllegalArgumentException e) {
            throw new BlazeException("Unable to execute task '" + task + "'", e);
        }
    }
    
}

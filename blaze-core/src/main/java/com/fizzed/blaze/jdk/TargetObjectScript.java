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
import com.fizzed.blaze.core.WrappedBlazeException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

/**
 * A script that uses reflection to detect and invoke tasks based on public
 * methods. Can be used by other engines that have a similar model.
 * 
 * @author joelauer
 */
public class TargetObjectScript implements Script {
    
    static final public Predicate<Method> FILTER_PUBLIC_INSTANCE_METHOD = (Method m) -> {
        return !Modifier.isStatic(m.getModifiers()) && Modifier.isPublic(m.getModifiers());
    };
    
    final protected Object targetObject;

    public TargetObjectScript(Object targetObject) {
        this.targetObject = targetObject;
    }

    public List<String> findTasks(Predicate<Method>... filters) throws BlazeException {
        List<String> tasks = new ArrayList<>();
        
        try {
            Method[] methods = targetObject.getClass().getDeclaredMethods();
            
            FIND_METHODS:
            for (Method m : methods) {
                for (Predicate<Method> filter : filters) {
                    if (!filter.test(m)) {
                        // method unacceptable on first filter failing
                        continue FIND_METHODS;
                    }
                }
                tasks.add(m.getName());
            }
        } catch (SecurityException e) {
            throw new BlazeException("Unable to detect script tasks", e);
        }
        
        return tasks;
    }
    
    public Method findTaskMethod(String task) {
        // verify the method (task) exists first
        try {
            return targetObject.getClass().getMethod(task, new Class[]{});
        } catch (NoSuchMethodException e) {
            throw new NoSuchTaskException(task);
        } catch (SecurityException e) {
            throw new BlazeException("Unable to access task '" + task + "'", e);
        }
    }
    
    public void invokeTaskMethod(String task, Method method) throws Exception {
        try {
            method.invoke(targetObject, new Object[]{});
        } catch (InvocationTargetException e) {
            Throwable t = e.getCause(); 
            if (t instanceof BlazeException) {
                throw (BlazeException)t;
            } else if (t instanceof RuntimeException) {
                throw (RuntimeException)t;
            } else if (t instanceof Exception) {
                throw (Exception)t;
            } else {
                throw new WrappedBlazeException(t);
            }
        } catch (IllegalAccessException | IllegalArgumentException e) {
            throw new WrappedBlazeException(e);
        }
    }
    
    @Override
    public List<String> tasks() throws BlazeException {
        return findTasks(FILTER_PUBLIC_INSTANCE_METHOD);
    }

    @Override
    public void execute(String task) throws Exception {
        Method method = findTaskMethod(task);
        invokeTaskMethod(task, method);
    }
    
}

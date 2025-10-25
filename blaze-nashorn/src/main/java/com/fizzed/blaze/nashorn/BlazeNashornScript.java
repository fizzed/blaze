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
package com.fizzed.blaze.nashorn;

import com.fizzed.blaze.core.*;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import javax.script.Bindings;
import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BlazeNashornScript implements Script {
    static private final Logger log = LoggerFactory.getLogger(BlazeNashornScript.class);
    
    final private BlazeNashornEngine engine;
    final private ScriptEngine scriptEngine;
    final private Invocable invocable;
    final private Bindings bindings;

    public BlazeNashornScript(BlazeNashornEngine engine, ScriptEngine scriptEngine, Bindings bindings, Invocable invocable) {
        this.engine = engine;
        this.scriptEngine = scriptEngine;
        this.bindings = bindings;
        this.invocable = invocable;
    }

    @Override
    public List<BlazeTask> tasks() throws BlazeException {
        List<String> scriptFunctions = BlazeNashornEngine
            .queryScriptFunctions(scriptEngine, bindings);
        
        // filter out standard nashorn functions
        Set<String> tasks = new HashSet<>(scriptFunctions);

        tasks.removeAll(engine.getDefaultNashornFunctions());

        return tasks.stream()
            .map((t) -> new BlazeTask(t, null))
            .collect(Collectors.toList());
    }

    @Override
    public List<BlazeTaskGroup> taskGroups() throws BlazeException {
        // these can't be supported
        return Collections.emptyList();
    }

    @Override
    public void execute(String task) throws BlazeException {
        try {
            this.invocable.invokeFunction(task);
        } catch (NoSuchMethodException e) {
            throw new NoSuchTaskException(task);
        } catch (ScriptException e) {
            throw new BlazeException("Unable to execute task in nashorn script", e);
        } 
    }
    
}

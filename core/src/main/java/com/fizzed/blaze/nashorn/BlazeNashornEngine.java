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

import com.fizzed.blaze.Context;
import com.fizzed.blaze.core.BlazeException;
import com.fizzed.blaze.util.AbstractEngine;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.Arrays;
import java.util.List;
import javax.script.Bindings;
import javax.script.Invocable;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BlazeNashornEngine extends AbstractEngine<BlazeNashornScript> {
    static private final Logger log = LoggerFactory.getLogger(BlazeNashornEngine.class);

    private ScriptEngineManager scriptEngineManager;
    private List<String> defaultNashornFunctions;

    @Override
    public String getFileExtension() {
        return ".js";
    }
    
    @Override
    public void init(Context initialContext) throws BlazeException {
        super.init(initialContext);
        
        this.scriptEngineManager = new ScriptEngineManager();
        
        ScriptEngine scriptEngine = this.scriptEngineManager.getEngineByName("nashorn");
        
        if (scriptEngine == null) {
            throw new BlazeException("Unable to get nashorn script engine. Are you running on Java 8?");
        }
        
        // query for functions available by default
        this.defaultNashornFunctions = queryScriptFunctions(scriptEngine, scriptEngine.createBindings());
        
        //log.debug("standardNashornFunctions: {}", standardNashornFunctions);
    }

    @Override
    public BlazeNashornScript compile(Context context) throws BlazeException {
        try {
            ScriptEngine scriptEngine = this.scriptEngineManager.getEngineByName("nashorn");
            
            Bindings bindings = scriptEngine.createBindings();
            
            scriptEngine.setBindings(bindings, ScriptContext.ENGINE_SCOPE);
            
            // expose functions as global variables to script
            bindings.put("context", context);
            bindings.put("log", context.logger());
            //bindings.put("console", new Console());

            scriptEngine.eval(new FileReader(context.scriptFile().toFile()), bindings);

            //log.debug("script class: {}", this.script.getClass().getCanonicalName());
     
            Invocable invocable = (Invocable)scriptEngine;
            
            return new BlazeNashornScript(this, scriptEngine, bindings, invocable);
        } catch (ScriptException | FileNotFoundException e) {
            throw new BlazeException("Unable to evaluate nashorn script", e);
        }
    }

    public List<String> getDefaultNashornFunctions() {
        return defaultNashornFunctions;
    }
    
    static public List<String> queryScriptFunctions(ScriptEngine engine, Bindings bindings) {
        Object result = null;
        
        try {
            // use js magic to find the properties of the script
            result = engine.eval("Java.to(Object.getOwnPropertyNames(this).filter(function (p) { return typeof this[p] === 'function' }), 'java.lang.String[]')", bindings);
        } catch (ScriptException e) {
            throw new BlazeException("Unable to query nashorn script for functions", e);
        }
        
        if (result == null) {
            throw new BlazeException("Unable to query for tasks in nashorn script (expected String[] but got a null)");
        }
        
        if (!(result instanceof String[])) {
            throw new BlazeException("Unable to query for tasks in nashorn script (expected String[] but got " + result.getClass() + ")");
        }
        
        return Arrays.asList((String[])result);
    }
}

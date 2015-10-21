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
import com.fizzed.blaze.Context;
import com.fizzed.blaze.Engine;
import com.fizzed.blaze.util.AbstractEngine;
import groovy.util.GroovyScriptEngine;
import groovy.util.ResourceException;
import groovy.util.ScriptException;
import java.io.IOException;
import java.net.URL;
import org.kohsuke.MetaInfServices;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@MetaInfServices(Engine.class)
public class BlazeGroovyEngine extends AbstractEngine<BlazeGroovyScript> {
    static final private Logger log = LoggerFactory.getLogger(AbstractEngine.class);
    
    private GroovyScriptEngine groovy;

    @Override
    public String getFileExtension() {
        return ".groovy";
    }
    
    @Override
    public void init(Context initialContext) throws BlazeException {
        super.init(initialContext);
        
        try {
            // initialize engine with the base directory
            URL root = initialContext.baseDir().toURI().toURL();
            this.groovy = new GroovyScriptEngine(new URL[] { root });
        } catch (IOException e) {
            throw new BlazeException("Unable to create groovy", e);
        }
    }

    @Override
    public BlazeGroovyScript compile(Context context) throws BlazeException {
        Class scriptClass;
        try {
            // must be valid url...
            String path = context.file().toURI().toURL().toString();
            scriptClass = this.groovy.loadScriptByName(path);
        } catch (ResourceException | IOException | ScriptException e) {
            throw new BlazeException("Unable to load groovy script", e);
        }
        
        try {
            Object script = scriptClass.newInstance();
            return new BlazeGroovyScript(this, script);
        } catch (InstantiationException | IllegalAccessException e) {
            throw new BlazeException("Unable to create groovy script instance", e);
        }
    }
}

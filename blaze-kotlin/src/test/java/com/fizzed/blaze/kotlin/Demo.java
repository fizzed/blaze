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
package com.fizzed.blaze.kotlin;

import com.fizzed.blaze.Context;
import com.fizzed.blaze.core.ContextHolder;
import com.fizzed.blaze.internal.ContextImpl;
import com.fizzed.blaze.util.Timer;
import java.io.File;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Joe Lauer
 */
public class Demo {
    static private final Logger log = LoggerFactory.getLogger(Demo.class);
    
    static public void main(String[] args) throws Exception {
        //File scriptFile = FileHelper.resourceAsFile("/jdk/hello.java");
        File scriptFile = new File("src/test/resources/kotlin/hello.kt");
        
        Context context = new ContextImpl(null, null, scriptFile.toPath(), null);
        ContextHolder.set(context);
                
        BlazeKotlinEngine engine = new BlazeKotlinEngine();
        
        engine.init(context);
        
        Timer timer = new Timer();
        
        BlazeKotlinScript script = engine.compile(context);
        
        log.info("Compiled in {} ms", timer.stop().millis());
        
        script.execute("main");
    }
    
}

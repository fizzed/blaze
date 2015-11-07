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

import com.fizzed.blaze.Context;
import com.fizzed.blaze.internal.ContextImpl;
import com.fizzed.blaze.internal.FileHelper;
import com.fizzed.blaze.util.Timer;
import java.io.File;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author joelauer
 */
public class TryIt {
    static private final Logger log = LoggerFactory.getLogger(TryIt.class);
    
    static public void main(String[] args) throws Exception {
        File scriptFile = FileHelper.resourceAsFile("/jdk/hello.java");
                
        Context context = new ContextImpl(null, null, scriptFile.toPath(), null);
                
        BlazeJdkEngine engine = new BlazeJdkEngine();
        
        engine.init(context);
        
        Timer timer = new Timer();
        
        BlazeJdkScript script = engine.compile(context);
        
        log.info("Compiled in {} ms", timer.stop().millis());
        
        script.execute("main");
    }
    
}

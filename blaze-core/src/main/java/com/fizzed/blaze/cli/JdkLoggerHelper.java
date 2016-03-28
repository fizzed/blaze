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
package com.fizzed.blaze.cli;

import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

/**
 *
 * @author joelauer
 */
public class JdkLoggerHelper {
    
    static public void configure() throws IOException {
        // programmatically load JDK logging properties
        final LogManager logManager = LogManager.getLogManager();
        try (final InputStream is = Bootstrap.class.getResourceAsStream("/logging.properties")) {
            logManager.readConfiguration(is);
        }
    }
    
    static public void setRootLevel(String level) {
        setLevel("", level);
    }
    
    static public void setLevel(String loggerName, String level) {
        final Logger logger = Logger.getLogger(loggerName);
        
        //for (Handler handler : logger.getHandlers()) {
            switch (level) {
                case "trace":
                    logger.setLevel(Level.FINEST);
                    //handler.setLevel(Level.FINEST);
                    break;
                case "debug":
                    logger.setLevel(Level.FINE);
                    //handler.setLevel(Level.FINE);
                    break;
                case "info":
                    logger.setLevel(Level.INFO);
                    //handler.setLevel(Level.INFO);
                    break;
                case "warn":
                    logger.setLevel(Level.WARNING);
                    //handler.setLevel(Level.WARNING);
                    break;
            }
        //}
    }
    
}

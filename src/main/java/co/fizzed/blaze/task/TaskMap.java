/*
 * Copyright 2014 Fizzed Inc.
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

package co.fizzed.blaze.task;

import java.util.LinkedHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author joelauer
 */
public class TaskMap extends LinkedHashMap<String,Task> {
    private static final Logger logger = LoggerFactory.getLogger(TaskMap.class);
    
    @Override
    public Task put(String key, Task value) {
        // tasks named "blaze" are reserved
        if (key.equalsIgnoreCase("blaze")) {
            throw new RuntimeException("Tasks cannot be named [blaze]; reserved value");
        }
        
        Task t = super.put(key, value);
        
        // TODO: allow duplicates???
        if (t != null) {
            logger.warn("Did you mean to override a previous task already named [" + key + "]");
        }
       
        // name task appropriately...
        value.name(key);
        return t;
    }
    
}

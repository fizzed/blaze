/*
 * Copyright 2016 Fizzed, Inc.
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
package com.fizzed.crux.vagrant;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.zeroturnaround.exec.ProcessResult;

/**
 * Utility methods for working with vagrant.
 */
public class VagrantUtil {
    
    static public List<String> parseLines(ProcessResult result) {
        String output = result.outputUTF8();
        String[] split = output.split("[\\r\\n]");
        List<String> lines = new ArrayList<>();
        for (String s : split) {
            lines.add(s.trim());
        }
        return lines;
    }
    
    static public Map<String,VagrantStatus> parseStatus(List<String> lines) {
        Map<String,VagrantStatus> statuses = new LinkedHashMap<>();
        
        for (String line : lines) {
            String[] values = line.split(",");
            
            if (values.length != 4) {
                // skip it
                continue;
            }
            
            String id = values[0];
            String name = values[1];
            String key = values[2];
            String value = values[3];
            
            if (Objects.equals(name, "")) {
                // skip it
                continue;
            }
            
            VagrantStatus status = statuses.get(id);
            
            if (status == null) {
                status = new VagrantStatus(id, name);
                statuses.put(id, status);
            }
            
            status.getValues().put(key, value);
        }
        
        return statuses;
    }
    
}

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
package co.fizzed.otter.core;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import jdk.nashorn.api.scripting.AbstractJSObject;

/**
 *
 * @author joelauer
 */
public class Tasks extends AbstractJSObject {
    
    private final Context context;
    private final Map<String, Object> members;
    
    public Tasks(Context context) {
        this.context = context;
        this.members = new HashMap<String, Object>();
    }

    @Override
    public Set<String> keySet() {
        return this.members.keySet();
    }
    
    @Override
    public void setMember(String name, Object value) {
        //System.out.println("Added member: " + name + " -> " + value.getClass());
        this.members.put(name, value);
    }

    @Override
    public Object getMember(String name) {
        // TODO: throw exception?
        return this.members.get(name);
    }
    
    public Map<String, Object> getMembers() {
        return this.members;
    }

}

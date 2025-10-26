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
package com.fizzed.blaze.core;

import com.fizzed.blaze.Task;

import java.util.Objects;

/**
 * Represents a task in a script.
 */
public class BlazeTask implements Comparable<BlazeTask> {

    private final String name;
    private final String description;
    private final int order;
    private final String group;

    public BlazeTask(String name) {
        this(name, null);
    }

    public BlazeTask(String name, String description) {
        this(name, description, Task.DEFAULT_ORDER, null);
    }

    public BlazeTask(String name, String description, int order, String group) {
        this.name = name;
        this.description = description;
        this.order = order;
        this.group = group;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public int getOrder() {
        return order;
    }

    public String getGroup() {
        return group;
    }

    @Override
    public String toString() {
        return name;
    }
    
    @Override
    public int hashCode() {
        int hash = 7;
        hash = 83 * hash + Objects.hashCode(this.name);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final BlazeTask other = (BlazeTask) obj;
        if (!Objects.equals(this.name, other.name)) {
            return false;
        }
        return true;
    }

    @Override
    public int compareTo(BlazeTask o) {
        // by order, then name
        int compareTo = this.order - o.order;
        
        if (compareTo == 0) {
            compareTo = this.name.compareTo(o.name);
        }
        
        return compareTo;
    }
}

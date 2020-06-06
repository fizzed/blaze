/*
 * Copyright 2020 Fizzed, Inc.
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
package com.fizzed.blaze.system;

import com.fizzed.blaze.Context;
import com.fizzed.blaze.core.Prompter;
import java.util.Arrays;
import java.util.Objects;

public class Prompt {
 
    private final Context context;
    private final Prompter prompter;
    private boolean masked;
    private boolean caseSensitive;
    private boolean trim;
    private boolean allowBlank;
    private Object defaultOption;
    private Object[] options;

    public Prompt(
            Context context,
            Prompter prompter) {
        
        this.context = context;
        this.prompter = prompter;
        this.trim = true;
        this.caseSensitive = false;
    }
    
    public Prompt masked(boolean masked) {
        this.masked = masked;
        return this;
    }
    
    public Prompt trim(boolean trim) {
        this.trim = trim;
        return this;
    }
    
    /**
     * If options are provided, should they be matched ignoring case or not.
     * The default is case insensitive.
     * @param caseSensitive
     * @return 
     */
    public Prompt caseSensitive(boolean caseSensitive) {
        this.caseSensitive = caseSensitive;
        return this;
    }
    
    /**
     * If a blank (empty) responses are valid.  Otherwise, user will be prompted
     * again, unless a defaultOption is provided.
     * @param allowBlank
     * @return 
     */
    public Prompt allowBlank(boolean allowBlank) {
        this.allowBlank = allowBlank;
        return this;
    }
    
    /**
     * In the case of an empty/blank response, this is what will be returned.
     * @param defaultOption
     * @return 
     */
    public Prompt defaultOption(Object defaultOption) {
        this.defaultOption = defaultOption;
        return this;
    }
    
    /**
     * A set of concrete acceptable options to allow.
     * @param options
     * @return 
     */
    public Prompt options(Object... options) {
        this.options = options;
        return this;
    }

    public String prompt(String message, Object... args) {
        while (true) {
            String s = null;
            
            if (this.masked) {
                char[] c = this.prompter.passwordPrompt(message ,args);
                if (c != null) {
                    s = new String(c);
                }
            }
            else {
                s = this.prompter.prompt(message ,args);
            }
            
            if (s == null) {
                // this is an EOF
                throw new IllegalStateException("Unable to prompt, input is closed (EOF!)");
            }
            
            if (this.trim) {
                s = s.trim();
            }
            
            // is it blank?
            if (s.isEmpty()) {
                if (this.defaultOption != null) {
                    return this.defaultOption.toString();
                }
                
                if (!this.allowBlank) {
                    continue;           // prompt again...
                }
            }
            
            // were there constraints?
            if (this.options != null && this.options.length > 0) {
                for (Object o : this.options) {
                    String v = Objects.toString(o, null);
                    if (this.caseSensitive) {
                        if (s.equals(v)) {
                            return v;
                        }
                    } else {
                        if (s.equalsIgnoreCase(v)) {
                            return v;
                        }
                    }
                }
                
                this.context.logger().error("Only the following values are supported: {}", Arrays.asList(this.options));
                continue;
            }
            
            return s;
        }
    }
    
}
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
package com.fizzed.blaze.internal;

import com.fizzed.blaze.core.Prompter;
import java.util.LinkedList;
import java.util.Queue;

/**
 *
 * @author joelauer
 */
public class ExpectPrompter implements Prompter {
    
    private final Queue<String> answers;

    public ExpectPrompter() {
        this.answers = new LinkedList<>();
    }
    
    public Queue<String> answers() {
        return this.answers;
    }
    
    public void add(String answer) {
        this.answers.add(answer);
    }
    
    public void verifyHasNextAnswer(String prompt) {
        if (answers.isEmpty()) {
            throw new IllegalStateException("No answer provided beforehand for " + prompt);
        }
    }
    
    @Override
    public String prompt(String prompt, Object... args) {
        verifyHasNextAnswer(prompt);
        return this.answers.remove();
    }

    @Override
    public char[] passwordPrompt(String prompt, Object... args) {
        verifyHasNextAnswer(prompt);
        return this.answers.remove().toCharArray();
    }
    
}

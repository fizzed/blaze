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
package com.fizzed.blaze.core;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 *
 * @author joelauer
 */
public class ConsolePrompter implements Prompter {

    @Override
    public String prompt(String prompt, Object... args) {
        if (System.console() != null) {
            return System.console().readLine(prompt, args);
        }
        System.out.print(String.format(prompt, args));
        BufferedReader reader = new BufferedReader(new InputStreamReader(
                System.in));
        try {
            return reader.readLine();
        } catch (IOException e) {
            throw new BlazeException(e.getMessage(), e);
        }
    }
    
    @Override
    public char[] passwordPrompt(String prompt, Object... args) {
        if (System.console() != null) {
            return System.console().readPassword(prompt, args);
        }
        return prompt(prompt, args).toCharArray();
    }
    
}

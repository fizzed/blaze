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
package com.fizzed.blaze.util;

public class TerminalLine {
 
    private int length;
    private String beforePadded;
    private String finalLine;
    
    public void update(
        String format,
        Object... args) {
        
        StringBuilder sb = new StringBuilder();
        sb.append("\r");
        
        String s = String.format(format, args);
        sb.append(s);

        this.beforePadded = sb.toString();
        
        if (sb.length() > length) {
            this.length = sb.length();
        }
        
        // pad to clear values we do not want to show
        for (int i = 0; i < (this.length - s.length()); i++) {
            sb.append(" ");
        }
        
        this.finalLine = sb.toString();
        
        System.out.print(this.finalLine);
    }
    
    public void done() {
        this.done(500L, null);
    }
    
    public void done(String message, Object... args) {
        this.done(500L, message, args);
    }
    
    public void done(long millis, String message, Object... args) {
        if (message != null) {
            String joinedMessage = this.beforePadded + message;
            this.update(joinedMessage, args);
        }
        if (millis > 0) {
            BlazeUtils.sleep(millis);
        }
        System.out.println();
        this.length = 0;
        this.beforePadded = null;
        this.finalLine = null;
    }
    
}
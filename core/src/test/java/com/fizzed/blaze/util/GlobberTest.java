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
package com.fizzed.blaze.util;

import org.junit.Test;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

/**
 *
 * @author joelauer
 */
public class GlobberTest {
    
    @Test
    public void containsUnescapedChars() {
        char[] specialChars = new char[] { '*', '{', '}' };
        
        boolean result;
        
        result = Globber.containsUnescapedChars("", specialChars);
        assertThat(result, is(false));
        
        result = Globber.containsUnescapedChars("a", specialChars);
        assertThat(result, is(false));
        
        result = Globber.containsUnescapedChars("*", specialChars);
        assertThat(result, is(true));
        
        result = Globber.containsUnescapedChars("{", specialChars);
        assertThat(result, is(true));
        
        result = Globber.containsUnescapedChars("}", specialChars);
        assertThat(result, is(true));
        
        // escaped versions
        
        result = Globber.containsUnescapedChars("\\*", specialChars);
        assertThat(result, is(false));
        
        result = Globber.containsUnescapedChars("\\*\\{.java\\}", specialChars);
        assertThat(result, is(false));
    }
   
}

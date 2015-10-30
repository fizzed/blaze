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

import com.fizzed.blaze.internal.Converter;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import org.junit.Test;

/**
 *
 * @author joelauer
 */
public class ConverterTest {
    
    @Test
    public void convert() {
        assertThat(Converter.convert("1", Integer.class), is(1));
        assertThat(Converter.convert("1", int.class), is(1));
        assertThat(Converter.convert("true", Boolean.class), is(true));
        assertThat(Converter.convert("true", boolean.class), is(true));
    }
    
}

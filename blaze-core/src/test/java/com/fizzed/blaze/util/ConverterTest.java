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

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import org.junit.jupiter.api.Test;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 *
 * @author joelauer
 */
public class ConverterTest {
    
    @Test
    public void convert() throws MalformedURLException {
        assertThat(Converter.convert("1", Byte.class), is((byte)1));
        assertThat(Converter.convert("1", byte.class), is((byte)1));

        assertThat(Converter.convert("1", Short.class), is((short)1));
        assertThat(Converter.convert("1", short.class), is((short)1));

        assertThat(Converter.convert("1", Integer.class), is(1));
        assertThat(Converter.convert("1", int.class), is(1));

        assertThat(Converter.convert("1.2", Float.class), is(1.2f));
        assertThat(Converter.convert("1.2", float.class), is(1.2f));

        assertThat(Converter.convert("1.2", Double.class), is(1.2d));
        assertThat(Converter.convert("1.2", double.class), is(1.2d));

        assertThat(Converter.convert("true", Boolean.class), is(true));
        assertThat(Converter.convert("false", boolean.class), is(false));

        assertThat(Converter.convert("http://www.fizzed.com/", URI.class), is(URI.create("http://www.fizzed.com/")));
        assertThat(Converter.convert("http://www.fizzed.com/", URL.class), is(new URL("http://www.fizzed.com/")));

        assertThat(Converter.convert("./a", Path.class), is(Paths.get("./a")));
        assertThat(Converter.convert("./a", File.class), is(Paths.get("./a").toFile()));
    }
    
}

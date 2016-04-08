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

import com.fizzed.blaze.internal.FileHelper;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.Test;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasSize;
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
    
    @Test
    public void globber() throws Exception {
        // run tests with context of the core/src/test/resources/globber directory
        Path globberDir = FileHelper.resourceAsFile("/globber/globber.txt").getParentFile().toPath();
        String globberPath = BasicPaths.toString(globberDir);
        
        // hidden directory we'll use later on
        Path hiddenDir = globberDir.resolve(".hidden");
        Files.deleteIfExists(hiddenDir);
        
        List<Path> paths;
        
        // "core/src/test/resources/globber"
        paths = Globber.globber(globberPath).scan();
        assertThat(paths, hasSize(1));
        assertThat(paths.get(0), is(globberDir));
        
        // "core/src/test/resources/globber"
        paths = Globber.globber(globberPath + "/src").scan();
        assertThat(paths, hasSize(1));
        assertThat(paths.get(0), is(globberDir.resolve("src")));
        
        // "core/src/test/resources/globber/*"
        paths = Globber.globber(globberPath + "/*").scan();
        assertThat(paths, hasSize(3));
        assertThat(paths, containsInAnyOrder(
                            globberDir.resolve("globber.html"),
                            globberDir.resolve("globber.txt"),
                            globberDir.resolve("src")));
        
        // "core/src/test/resources/globber/**"
        paths = Globber.globber(globberPath + "/**").scan();
        assertThat(paths, hasSize(5));
        assertThat(paths, containsInAnyOrder(
                            globberDir.resolve("globber.html"),
                            globberDir.resolve("globber.txt"),
                            globberDir.resolve("src"),
                            globberDir.resolve("src/java"),
                            globberDir.resolve("src/java/java.txt")));
        
        // "core/src/test/resources/globber/*.html"
        paths = Globber.globber(globberPath + "/*.html").scan();
        assertThat(paths, hasSize(1));
        assertThat(paths, containsInAnyOrder(
                            globberDir.resolve("globber.html")));
        
        // "core/src/test/resources/globber/*.{txt,html}"
        paths = Globber.globber(globberPath + "/*.{txt,html}").scan();
        assertThat(paths, hasSize(2));
        assertThat(paths, containsInAnyOrder(
                            globberDir.resolve("globber.html"),
                            globberDir.resolve("globber.txt")));
        
        // "core/src/test/resources/globber/*.{txt,html}"
        paths = Globber.globber(globberPath + "/????ber.{txt,html}").scan();
        assertThat(paths, hasSize(2));
        assertThat(paths, containsInAnyOrder(
                            globberDir.resolve("globber.html"),
                            globberDir.resolve("globber.txt")));
        
        // files only
        
        paths = Globber.globber(globberPath + "/*").filesOnly().scan();
        assertThat(paths, hasSize(2));
        assertThat(paths, containsInAnyOrder(
                            globberDir.resolve("globber.html"),
                            globberDir.resolve("globber.txt")));
        
        // dirs only
        
        paths = Globber.globber(globberPath + "/*").dirsOnly().scan();
        assertThat(paths, hasSize(1));
        assertThat(paths, containsInAnyOrder(
                            globberDir.resolve("src")));
        
        // both files and dirs only (essentially returns nothing)
        
        paths = Globber.globber(globberPath + "/*").filesOnly().dirsOnly().scan();
        assertThat(paths, hasSize(0));
        
        // ADD .hidden DIRECTORY
        
        Files.createDirectory(hiddenDir);
        
        paths = Globber.globber(globberPath + "/*").scan();
        assertThat(paths, hasSize(4));
        assertThat(paths, containsInAnyOrder(
                            hiddenDir,
                            globberDir.resolve("globber.html"),
                            globberDir.resolve("globber.txt"),
                            globberDir.resolve("src")));
        
        paths = Globber.globber(globberPath + "/*").visibleOnly().scan();
        assertThat(paths, hasSize(3));
        assertThat(paths, containsInAnyOrder(
                            globberDir.resolve("globber.html"),
                            globberDir.resolve("globber.txt"),
                            globberDir.resolve("src")));
    }
   
}

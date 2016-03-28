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
package com.fizzed.blaze;

import java.io.File;
import java.nio.file.Path;
import org.slf4j.Logger;

/**
 *
 * @author joelauer
 */
public interface Context {

    Config config();

    Logger logger();
    
    Path scriptFile();

    Path baseDir();

    Path withBaseDir(Path path);

    Path withBaseDir(File file);

    Path withBaseDir(String path);
    
    Path userDir();
    
    Path withUserDir(Path path);

    Path withUserDir(File file);

    Path withUserDir(String path);
    
    void fail(String message);
    
    String prompt(String prompt, Object... args);
    
    char[] passwordPrompt(String prompt, Object... args);
    
}

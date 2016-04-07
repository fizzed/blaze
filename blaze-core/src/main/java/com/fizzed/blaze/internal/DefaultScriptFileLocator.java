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
package com.fizzed.blaze.internal;

import static com.fizzed.blaze.core.Blaze.SEARCH_RELATIVE_DIRECTORIES;
import com.fizzed.blaze.core.BlazeException;
import com.fizzed.blaze.core.MessageOnlyException;
import com.fizzed.blaze.core.ScriptFileLocator;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DefaultScriptFileLocator implements ScriptFileLocator {
    static private final Logger log = LoggerFactory.getLogger(DefaultScriptFileLocator.class);
    
    @Override
    public Path locate(Path directory) throws BlazeException {
        if (directory == null) {
            directory = Paths.get(".");
        }
                
        List<Path> searchDirs = new ArrayList<>();

        searchDirs.add(directory);

        // add extra search directories
        for (Path d : SEARCH_RELATIVE_DIRECTORIES) {
            searchDirs.add(directory.resolve(d));
        }

        List<Path> blazeFiles = null;

        for (Path searchDir : searchDirs) {

            // skip directories that do not exist
            if (Files.notExists(searchDir) || !Files.isDirectory(searchDir)) {
                continue;
            }

            try {
                // search for file named "blaze.<ext>" (but not blaze.conf or blaze.jar)
                blazeFiles 
                    = Files.list(searchDir)
                        .filter((path) -> {
                            String name = path.getFileName().toString();
                            return name.startsWith("blaze.")
                                    && !name.endsWith(".conf")
                                    && !name.endsWith(".jar");
                        })
                        .collect(Collectors.toList());

                if (blazeFiles.size() > 0) {
                    // stop searching
                    break;
                }
            } catch (IOException e) {
                throw new BlazeException(e.getMessage(), e);
            }
        }

        if (blazeFiles == null || blazeFiles.isEmpty()) {
            throw new MessageOnlyException("Unable to find a blaze file (e.g. blaze.java). Perhaps this is not a Blaze project?");
        }

        if (blazeFiles.size() > 1) {
            throw new MessageOnlyException("More than one blaze file found. Either delete the extra files use -f parameter");
        }

        // first and only entry IS the script
        return blazeFiles.get(0);
    }
    
}

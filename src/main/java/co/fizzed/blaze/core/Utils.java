/*
 * Copyright 2014 Fizzed Inc.
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
package co.fizzed.blaze.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.script.ScriptException;
import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;

/**
 *
 * @author joelauer
 */
public class Utils {
    private static final Logger logger = LoggerFactory.getLogger(Utils.class);

    private final Context context;
    private final OS os;
    
    public Utils(Context context) {
        this.context = context;
        this.os = new OS();
    }

    public void load(String path) throws IOException, ScriptException {
        logger.info("Overridden load method... {}", path);
        String pathLowerCase = path.toLowerCase();
        if (pathLowerCase.startsWith("http://") || pathLowerCase.startsWith("https://")) {
            // do nothing, original load() function will work
        } else {
            logger.debug("Resolving load path [" + path + "] against project baseDir [" + context.getBaseDir() + "]");
            File resolvedPath = context.resolveWithBaseDir(Paths.get(path));
            path = resolvedPath.getAbsolutePath();
        }
        // delegate to original load function
        context.getEngine().eval("originalLoad('" + path + "')");
    }

    public OS os() {
        return os;
    }

    public class OS {
        private String OS = System.getProperty("os.name").toLowerCase();

        public boolean isWindows() {
            return (OS.indexOf("win") >= 0);
        }

        public boolean isMac() {
            return (OS.indexOf("mac") >= 0);
        }

        public boolean isUnix() {
            return (OS.indexOf("nix") >= 0 || OS.indexOf("nux") >= 0 || OS.indexOf("aix") > 0 );
        }

        public boolean isSolaris() {
            return (OS.indexOf("sunos") >= 0);
        }
    }
}

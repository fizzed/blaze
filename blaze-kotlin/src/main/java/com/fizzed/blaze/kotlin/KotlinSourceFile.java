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
package com.fizzed.blaze.kotlin;

import com.fizzed.blaze.internal.FileHelper;
import java.nio.file.Path;

public class KotlinSourceFile {
    
    private final String fileExtension;
    private final String className;
    private final String classNameAlt;

    public KotlinSourceFile(Path sourceFile) {
        this.fileExtension = FileHelper.fileExtension(sourceFile.toFile());
        this.className = createClassName(sourceFile.getFileName().toString(),
                                            this.fileExtension);
        this.classNameAlt = createClassNameAlt(sourceFile.getFileName().toString(),
                                            this.fileExtension);
    }

    public String getFileExtension() {
        return fileExtension;
    }

    public String getClassName() {
        return className;
    }
    
    public String getClassNameAlt() {
        return classNameAlt;
    }
    
    public boolean isScript() {
        return isScriptExt(this.fileExtension);
    }
    
    static public boolean isScriptExt(String fileExtension) {
        return ".kts".equals(fileExtension);
    }
    
    static public String createClassName(String fileName, String fileExtension) {
        // hello.kt -> hello (if class defined)
        // hello.kt -> HelloKt (if class NOT defined)
        // hello.kts -> Hello
        String nameWithoutExt = fileName.substring(0, fileName.length() - fileExtension.length());
        
        // kotlin uppercases first letter of scripts
        if (isScriptExt(fileExtension)) {
            return nameWithoutExt.substring(0, 1).toUpperCase()
                    + nameWithoutExt.substring(1);
        } else {
            return nameWithoutExt;
        }
    }
    
    static public String createClassNameAlt(String fileName, String fileExtension) {
        String nameWithoutExt = fileName.substring(0, fileName.length() - fileExtension.length());
        return nameWithoutExt.substring(0, 1).toUpperCase()
                    + nameWithoutExt.substring(1)
                    + "Kt";
    }

}

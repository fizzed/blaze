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

import com.fizzed.blaze.core.Blaze;
import com.fizzed.blaze.core.MessageOnlyException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class InstallHelper {
    
    static public List<Path> installBlazeBinaries(Path installDir) throws MessageOnlyException {
        if (Files.notExists(installDir)) {
            throw new MessageOnlyException("Install directory " + installDir + " does not exist");
        }
        
        if (!Files.isDirectory(installDir)) {
            throw new MessageOnlyException("Install directory " + installDir + " is not a directory");
        }
        
        if (!Files.isWritable(installDir)) {
            throw new MessageOnlyException("Install directory " + installDir + " is not writable (run this as an Administrator or with sudo?)");
        }
        
        List<Path> installedFiles = new ArrayList<>();
        
        if (System.getProperty("os.name").toLowerCase().contains("win")) {
            // install blaze.bat
            Path blazeBatFile = installDir.resolve("blaze.bat");
            installResource("/bin/blaze.bat", blazeBatFile);
            installedFiles.add(blazeBatFile);

            // install blaze.ps1 (powershell)
            Path blazePs1File = installDir.resolve("blaze.ps1");
            installResource("/bin/blaze.ps1", blazePs1File);
            installedFiles.add(blazePs1File);
        }
        
        // for ming32 compat also install the *nix version
        Path blazeFile = installDir.resolve("blaze");
        installResource("/bin/blaze", blazeFile);
        installedFiles.add(blazeFile);
        
        return installedFiles;
    }
    
    static private void installResource(String resourceName, Path targetFile) throws MessageOnlyException {
        String fileName = targetFile.getFileName().toString();
        InputStream is = Blaze.class.getResourceAsStream(resourceName);
        
        if (is == null) {
            throw new MessageOnlyException("Unable to find resource for " + resourceName);
        }

        if (Files.exists(targetFile)) {
            throw new MessageOnlyException("File " + targetFile + " already exists (delete first then try again)");
        }
        
        try {
            Files.copy(is, targetFile, StandardCopyOption.REPLACE_EXISTING);
            // world execute!
            targetFile.toFile().setExecutable(true, false);
        } catch (IOException e) {
            throw new MessageOnlyException("Unable to copy resource " + fileName + " to " + targetFile + " (" + e.getMessage() + ")");
        }
    }
    
}

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

import com.fizzed.blaze.Contexts;
import static com.fizzed.blaze.Contexts.withBaseDir;
import static com.fizzed.blaze.Contexts.fail;
import static com.fizzed.blaze.Systems.exec;
import com.fizzed.blaze.core.Actions;
import com.fizzed.blaze.core.Blaze;
import com.fizzed.blaze.util.Streamables;
import static com.fizzed.blaze.util.Streamables.input;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.slf4j.Logger;

public class blaze {
    static private final Logger log = Contexts.logger();
    
    public void try_all() throws Exception {
        // boom -- execute another blaze script in this jvm
        new Blaze.Builder()
            .file(withBaseDir("../examples/try_all.java"))
            .build()
            .execute();
    }
    
    public void update_version() {
        String newVersion = Contexts.prompt("New version? ");
        exec("mvn", "versions:set", "-DnewVersion=" + newVersion).run();
        exec("mvn", "versions:commit").run();
    }
    
    private String latest_tag() {
        // get latest tag from git
        return exec("git", "describe", "--abbrev=0", "--tags")
            .runCaptureOutput()
            .toString()
            .trim();
    }
    
    public void after_release() throws IOException {
        Integer exitValue
            = exec("git", "diff-files", "--quiet")
                .exitValues(0,1)
                .run();
        
        if (exitValue == 1) {
            fail("Uncommitted changes in git. Commit them first then re-run this task");
        }

        update_readme();
        
        exec("git", "commit", "-am", "Updated README with latest version").run();
        exec("git", "push", "origin").run();
    }
    
    public void update_readme() throws IOException {
        Path readmeFile = withBaseDir("../README.md");
        Path newReadmeFile = withBaseDir("../README.md.new");
        
        // find latest version via git tag
        String taggedVersion = latest_tag().substring(1);
        
        log.info("Tagged version: {}", taggedVersion);
        
        // find current version in readme using a regex to match
        // then apply a mapping function to return the first group of each match
        // then we only need to get the first matched group
        String versionRegex = ".*lite-(\\d+\\.\\d+\\.\\d+)\\.jar.*";
        String readmeVersion
            = Streamables.matchedLines(input(readmeFile), versionRegex, (m) -> m.group(1))
                .findFirst()
                .get();
        
        log.info("Readme version: {}", readmeVersion);
        
        if (readmeVersion.equals(taggedVersion)) {
            log.info("Versions match (no need to update README)");
            return;
        }
        
        // replace version in file and write a new version
        final Pattern replacePattern = Pattern.compile(readmeVersion);
        try (BufferedWriter writer = Files.newBufferedWriter(newReadmeFile)) {
            Files.lines(readmeFile)
                .forEach((l) -> {
                    Matcher matcher = replacePattern.matcher(l);
                    String newLine = matcher.replaceAll(taggedVersion);
                    try {
                        writer.append(newLine);
                        writer.append("\n");
                    } catch (IOException e) {
                        throw new UncheckedIOException(e);
                    }
                });
            writer.flush();
        }
        
        // replace readme with updated version
        Files.move(newReadmeFile, readmeFile, StandardCopyOption.REPLACE_EXISTING);
    }
}

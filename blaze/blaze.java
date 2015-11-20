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
import com.fizzed.blaze.core.Blaze;
import com.fizzed.blaze.system.ExecResult;
import com.fizzed.blaze.util.CaptureOutput;
import com.fizzed.blaze.util.Streamables;
import java.io.BufferedWriter;
import java.io.IOException;
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
        Blaze.builder()
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
        CaptureOutput capture = Streamables.captureOutput();
        exec("git", "describe", "--abbrev=0", "--tags")
            .pipeOutput(capture)
            .run();
        
        String latestTag = capture.toString().trim();
        
        // chop off leading "v"
        return latestTag.trim().substring(1);
    }
    
    public void after_release() throws IOException {
        ExecResult result = exec("git", "diff-files", "--quiet").exitValues(0,1).run();
        
        if (result.exitValue() == 1) {
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
        String latestVersion = latest_tag();
        
        log.info("Latest version in git {}", latestVersion);
        
        // find current version in readme
        final Pattern versionPattern = Pattern.compile(".*lite-(\\d+\\.\\d+\\.\\d+)\\.jar.*");
        
        String currentVersion
            = Files.lines(readmeFile)
                .map((l) -> {
                    Matcher matcher = versionPattern.matcher(l);
                    if (matcher.matches()) {
                        return matcher.group(1);
                    } else {
                        return null;
                    }
                })
                .filter((l) -> l != null)
                .findFirst()
                .get();
        
        log.info("Current version in README {}", currentVersion);
        
        if (currentVersion.equals(latestVersion)) {
            log.info("Versions match (no need to update README)");
            return;
        }
        
        final Pattern replacePattern = Pattern.compile(currentVersion);
        
        try (BufferedWriter writer = Files.newBufferedWriter(newReadmeFile)) {
            Files.lines(readmeFile)
                    .forEach((l) -> {
                        Matcher matcher = replacePattern.matcher(l);
                        String newLine = matcher.replaceAll(latestVersion);
                        try {
                            writer.append(newLine);
                            writer.append("\n");
                        } catch (IOException e) {
                            throw new RuntimeException(e.getMessage(), e);
                        }
                    });
            writer.flush();
        }
        
        // replace readme with updated version
        Files.move(newReadmeFile, readmeFile, StandardCopyOption.REPLACE_EXISTING);
    }
}

Blaze by Fizzed
=======================================

[![Maven Central](https://img.shields.io/maven-central/v/com.fizzed/blaze?color=blue&style=flat-square)](https://mvnrepository.com/artifact/com.fizzed/blaze)

[![Java 8](https://img.shields.io/github/actions/workflow/status/fizzed/blaze/linux-java8.yaml?branch=master&label=Java%208&style=flat-square)](https://github.com/fizzed/blaze/actions/workflows/linux-java8.yaml)
[![Java 11](https://img.shields.io/github/actions/workflow/status/fizzed/blaze/linux-java11.yaml?branch=master&label=Java%2011&style=flat-square)](https://github.com/fizzed/blaze/actions/workflows/linux-java11.yaml)
[![Java 17](https://img.shields.io/github/actions/workflow/status/fizzed/blaze/linux-java17.yaml?branch=master&label=Java%2017&style=flat-square)](https://github.com/fizzed/blaze/actions/workflows/linux-java17.yaml)

[![Linux](https://img.shields.io/github/actions/workflow/status/fizzed/blaze/linux-java8.yaml?branch=master&label=Linux&style=flat-square)](https://github.com/fizzed/blaze/actions/workflows/linux-java8.yaml)
[![MacOS](https://img.shields.io/github/actions/workflow/status/fizzed/blaze/macos-x64.yaml?branch=master&label=MacOS&style=flat-square)](https://github.com/fizzed/blaze/actions/workflows/macos-x64.yaml)
[![Windows](https://img.shields.io/github/actions/workflow/status/fizzed/blaze/windows-x64.yaml?branch=master&label=Windows&style=flat-square)](https://github.com/fizzed/blaze/actions/workflows/macos-x64.yaml)

[Fizzed, Inc.](http://fizzed.com) (Follow on Twitter: [@fizzed_inc](http://twitter.com/fizzed_inc))

## Sponsored by

Blaze is proudly sponsored by <a href="https://www.greenback.com">Greenback</a>.  We love the service and think you would too.

<a href="https://www.greenback.com?utm_source=github.com&utm_medium=sponsorship&utm_campaign=fizzed-blaze" title="Greenback - Expenses made simple"><img src="https://www.greenback.com/assets/images/logo-greenback.png" height="48" width="166" alt="Greenback"></a>

<a href="https://www.greenback.com?utm_source=github.com&utm_medium=sponsorship&utm_campaign=fizzed-blaze" title="Greenback - Expenses made simple">More engineering. Less paperwork. Expenses made simple.</a>

## Overview

A speedy, flexible, general purpose scripting and application launching stack for
the JVM.  Can replace shell scripts and plays nicely with other tools.  Only
requires a Java 8 runtime and adding `blaze.jar` to your project directory.  Start
writing portable and cross-platform scripts.

Blaze pulls together stable, mature libraries from the Java ecosystem into a
light-weight package that lets you focus on getting things done.  When you 
invoke blaze, it does the following:

 - Sets up console logging
 - Loads your optional configuration file(s)
 - Downloads runtime dependencies (e.g. jars from Maven central)
 - Loads and compiles your script(s)
 - Executes "tasks" (methods your script defines)

## Features

 - Write your applications (scripts) in whatever JVM language you prefer.
   Out-of-the-box support for
    - Java (.java)
    - JavaScript (.js)
    - Groovy (.groovy)
    - Kotlin (.kt, .kts)
    - Or write your own (examples [here](blaze-core/src/main/java/com/fizzed/blaze/jdk), [here](blaze-core/src/main/java/com/fizzed/blaze/nashorn), [here](blaze-kotlin/src/main/java/com/fizzed/blaze/kotlin), and [here](blaze-groovy/src/main/java/com/fizzed/blaze/groovy))
 - Zero-install required. Just drop `blaze.jar` into your project directory and
   you or others can run it with `java -jar blaze.jar`.
 - [IDE support](https://github.com/fizzed/blaze-netbeans)
 - Small size so you can commit `blaze.jar` to your repository
 - Excellent framework support for executing processes, modifying the filesystem,
   user interaction, http, and ssh.
 - Easily use any Java library as a dependency to accomplish whatever
   the framework doesn't provide.

## More documentation

 - [Why Blaze?](docs/WHY.md)
 - [Examples](docs/EXAMPLES.md)
 - [Configuration and dependency management](docs/CONFIG.md)
 - [Basic usage](docs/BASIC.md)
 - [SSH plugin](docs/SSH.md)
 - [HTTP plugin](docs/HTTP.md)

## What is a blaze script?

A Blaze script is a 100% valid JVM class with public methods that typically uses
an empty (root) package declaration.  Each public method becomes the externally
accessible task that can be called from the command-line. Since most JVM languages
support this kind of structure, Blaze can easily support a wide variety of 
JVM languages.

## Install to your project

Download `blaze.jar` to your project directory.  If you have `wget` available

    wget -O blaze.jar 'https://repo1.maven.org/maven2/com/fizzed/blaze-lite/1.0.2/blaze-lite-1.0.2.jar'

If you have `curl` available

    curl -o blaze.jar 'https://repo1.maven.org/maven2/com/fizzed/blaze-lite/1.0.2/blaze-lite-1.0.2.jar'

Or simply [download the file in your web browser](https://repo1.maven.org/maven2/com/fizzed/blaze-lite/1.0.2/blaze-lite-1.0.2.jar)
and save it to your project directory with a name of `blaze.jar`

## Write hello world blaze script in .java

Create `blaze.java` file

```java
public class blaze {
    
    public void main() {
        System.out.println("Hello World!");
    }
    
}
```

## Run blaze script

Since you named your file `blaze.java`, Blaze will find it automatically.  You
can run it like so

    java -jar blaze.jar

If no task is supplied on the command line, Blaze will attempt to run the `main`
task by default.

## Write script that executes a process

Let's do a more useful example of how we use Blaze in many cases.  Let's say
you had a Maven project and wanted to execute a class with a main method. The
syntax to do that in Maven becomes difficult to remember and communicate to
other developers.  Blaze lets you simplify the entry points to your project
by exposing everything as named tasks.

```java
import static com.fizzed.blaze.Systems.exec;

public class blaze {

    public void demo1() {
        exec(
           "mvn", "compile", "exec:java", "-Dexec.classpathScope=runtime",
           "-Dexec.mainClass=com.example.Demo1").run();
    }

    public void demo2() {
        exec(
           "mvn", "compile", "exec:java", "-Dexec.classpathScope=runtime",
           "-Dexec.mainClass=com.example.Demo2").run();
    }
}
```

You can now just run these with `java -jar blaze.jar demo1` or `java -jar blaze.jar demo2`

## But I can still do your previous example in a shell script?

Yeah, I suppose so.  But you'd probably use two shell scripts to define the
separate tasks and if you cared about platform portability, you'd be nice to
also include `.bat` scripts for Windows users.  However, when you want to do
anything else that's remotely advanced, you'll start to appreciate having a
more advanced environment.  Here's an example where we query git for the 
latest tag and use it to update a README file with it.  We use this as a way
to maintain a README file with the latest version pushed to Maven central

```java
import com.fizzed.blaze.Contexts;
import static com.fizzed.blaze.Contexts.withBaseDir;
import static com.fizzed.blaze.Contexts.fail;
import static com.fizzed.blaze.Systems.exec;
import com.fizzed.blaze.core.Actions;
import com.fizzed.blaze.core.Blaze;
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
    
    private String latest_tag() {
        // get latest tag from git
        return exec("git", "describe", "--abbrev=0", "--tags")
            .runCaptureOutput()
            .toString()
            .trim();
    }
    
    public void update_readme() throws IOException {
        Path readmeFile = withBaseDir("../README.md");
        Path newReadmeFile = withBaseDir("../README.md.new");
        
        // find latest version via git tag, trim off leading 'v'
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
```

## Optionally install blaze on your PATH

While you can invoke `blaze.jar` as an executable jar by running

    java -jar blaze.jar

Sometimes it's more convenient to have a system-wide executable installed on
your PATH to make that even shorter.  Blaze has a built-in method to copy a
wrapper script to a directory.  On Linux or Mac OSX, you can run the following

    sudo java -jar blaze.jar -i /usr/local/bin

On Windows, open up a shell as an administrator (Start Menu -> Command Prompt >
right mouse click Run as administrator), then run the following

    java -jar blaze.jar -i C:\Windows\System32

Depending on your operating system, this will install either a `blaze` shell
script or a `blaze.bat` batch script.  Assuming `/usr/local/bin` or `C:\Windows\System32`
is already on your PATH (which normally it is), then you can now just run
the following in your shell

    blaze

## IDE support

A Netbeans-certified plugin is available for Nebeans 8.1+.  You can find it
in Netbeans 8.1 -> Tools > Plugins > Available Plugins > Blaze.  The source
code and project are [here](https://github.com/fizzed/blaze-netbeans)

## Where to save your script(s)

Blaze is designed to play nicely with other popular JVM build tools such as Maven,
Gradle, Ant, SBT, etc.  Blaze is also designed to play nicely with your favorite
IDE.  If you are planning on a blaze-only project, then create a `blaze.[ext]`
file in your project directory (at the same level as your `blaze.jar`).  Your
project directory would be

    <project directory>/
        blaze.jar
        blaze.[ext]

The `[ext]` would be whatever JVM language you'd like to write your script in.
So java would be `.java`, groovy would be `.groovy`, etc.

However, if you are using another build tool such as Maven, we'd suggest creating
a sub-directory called `.blaze` or `blaze` and placing your `blaze.[ext]` script in there.
Most Java IDEs compute classpaths for auto completion based on directory paths
and placing your script in the root directory doesn't work very well with IDEs like
Netbeans.  So a maven project + blaze would be

    <project directory>/
        .blaze/
            blaze.[ext]
        blaze.jar
        pom.xml

In either setup, you'd run your script identically.  That's because blaze will
first search the current directory for `blaze.[ext]` then `.blaze/blaze.[ext]`
then `blaze/blaze.[ext]`

If you'd like to have more than one script, you can supply it on the command-line
like so

    java -jar blaze.jar path/to/script.[ext]

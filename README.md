Blaze by Fizzed
=======================================

[![Build Status](https://travis-ci.org/fizzed/blaze.svg?branch=master)](https://travis-ci.org/fizzed/blaze)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.fizzed/blaze/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.fizzed/blaze)

[Fizzed, Inc.](http://fizzed.com) (Follow on Twitter: [@fizzed_inc](http://twitter.com/fizzed_inc))


## Overview

A speedy, flexible, general purpose scripting and application launching stack for
the JVM.  Can replace shell scripts and plays nicely with other tools.  Only
requires a Java 8 runtime and adding `blaze.jar` to your project directory.  Start
writing portable and cross-platform scripts -- that don't require the user to
install anything!

Blaze pulls together stable, mature libraries from the Java ecosystem into a
light-weight package that lets you focus on getting things done.  When you 
invoke blaze, it does the following:

 - Sets up console logging
 - Loads your optional configuration file(s)
 - Downloads runtime dependencies (e.g. jars from Maven central)
 - Adds dependencies to the classpath
 - Loads and compiles your script(s)
 - Executes "tasks" (methods your script defines)

## Features

 - Write your applications (scripts) in whatever JVM language you prefer.
   Out-of-the-box support for
    - Java (.java)
    - JavaScript (.js)
    - Groovy (.groovy)
    - Kotlin (.kt, .kts)
    - Or write your own (examples [here](core/src/main/java/com/fizzed/blaze/jdk), [here](core/src/main/java/com/fizzed/blaze/nashorn), [here](kotlin/src/main/java/com/fizzed/blaze/kotlin), and [here](groovy/src/main/java/com/fizzed/blaze/groovy))
 - Zero-install required. Just drop `blaze.jar` into your project directory and
   you or others can run it with `java -jar blaze.jar`.
 - IDE support
 - Small size so you can commit `blaze.jar` to your repository
 - Careful defaults and intuitive conventions are used to minimize typing to 
   run your scripts.
 - Heavy use of statically accessible methods for simple cross-language access
   to Blaze-supplied utilities.
 - Fluent-style method calls for elegant looking scripts
 - APIs encourage use of modern Java APIs (e.g. `java.nio.file.Path` instead of `java.io.File`)
 - Excellent framework support for executing processes, modifying the filesystem,
   user interaction, http, and ssh.
 - Easily include in any Java library as a dependency to accomplish whatever
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

    wget -O blaze.jar 'http://repo1.maven.org/maven2/com/fizzed/blaze-lite/0.8.0/blaze-lite-0.8.0.jar'

If you have `curl` available

    curl -o blaze.jar 'http://repo1.maven.org/maven2/com/fizzed/blaze-lite/0.8.0/blaze-lite-0.8.0.jar'

Or simply [download the file in your web browser](http://repo1.maven.org/maven2/com/fizzed/blaze-lite/0.8.0/blaze-lite-0.8.0.jar)
and save it to your project directory with a name of `blaze.jar`

## Optionally install blaze on PATH

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

## Write your first script

### Where to create your script

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
a sub-directory called `blaze` and placing your `blaze.[ext]` script in there.
Most Java IDEs compute classpaths for auto completion based on directory paths
and placing your script in the root directory doesn't work very well with IDEs like
Netbeans.  So a maven project + blaze would be

    <project directory>/
        blaze.jar
        blaze/
            blaze.[ext]
        pom.xml

In either setup, you'd run your script identically.  That's because blaze will
first search the current directory for `blaze.[ext]` followed by `blaze/blaze.[ext]`

If you'd like to have more than one script, you can supply it on the command-line
like so

    java -jar blaze.jar path/to/script.[ext]

### Write hello world script in .java

Create `blaze.java` file

```java
public class blaze {
    
    public void main() {
        System.out.println("Hello World!");
    }
    
}
```

### Run script

Since you named your file `blaze.java`, Blaze will find it automatically.  You
can run it like so

    java -jar blaze.jar

If no task is supplied on the command line, Blaze will attempt to run the `main`
task by default.

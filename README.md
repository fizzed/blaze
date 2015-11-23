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

## Install

Download `blaze.jar` to your project directory.  If you have `wget` available

    wget -O blaze.jar 'http://repo1.maven.org/maven2/com/fizzed/blaze-lite/0.8.0/blaze-lite-0.8.0.jar'

If you have `curl` available

    curl -o blaze.jar 'http://repo1.maven.org/maven2/com/fizzed/blaze-lite/0.8.0/blaze-lite-0.8.0.jar'

Or simply [download the file in your web browser](http://repo1.maven.org/maven2/com/fizzed/blaze-lite/0.8.0/blaze-lite-0.8.0.jar)
and save it to your project directory with a name of `blaze.jar`


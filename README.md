Blaze by Fizzed
=======================================

## By

 - [Fizzed, Inc.](http://fizzed.co)
 - Joe Lauer (Twitter: [@jjlauer](http://twitter.com/jjlauer))


## Overview

Speedy make/build tool tapping into the Nashorn JavaScript engine included with
Java 8.  While most tools focus on being "expressive" -- why don't they ever
focus on speed?  And they usually eat up an enormous amount of memory.

Projects are defined in a blaze.js file and written in JavaScript (ECMA 5.0).
High performance concepts such as asynchronous tasks & actions are included as
a natural part of the build system.

## Usage

Similar to how Apache Ant and Make functions -- there are high level "tasks" that
are made up of JavaScript code, actions, and other tasks.  Here is a sample
blaze.js that can be run from the command line "blaze run" to execute the "run"
task defined below:

    var helloText = "Hello World";

    $T.hello = Task.create(function() {
        print(helloText);
    });

    $T.run = Task.create(function() {
        $T.hello();
        print("Done");
    });

The output will be:

    Hello World!
    Done

There are numerous "actions" included to perform functions like executing
an external program, copying files, compiling code, etc.

    $T.gitStatus = Task.create(function() {
        $A.exec("git", "--status").run();
    });

This sample task will run the external program "git" with a single argument of
"--status".

## Performance

Most "build times" logged by your build tool do not reflect reality. They never
include startup time, tool dependency loading, compilation of your build
script, or configuring itself.  Using "time <command>" reflects how long this
all takes and is a better way to find out how much time you could be saving.

Based on compiling a simple Java-based project into a single .jar and assembled
into a final tarball, here are the results of 3 runs (after an initial run
to factor out dependency downloading, etc.):

 - Maven v3.2.3 - 6.106 + 6.684 + 5.363 = Avg 6.051s
 - Gradle v2.1 - 6.981 + 6.572 + 6.416 = Avg 6.656s
 - SBT 0.13.5 - 3.706 + 3.770 + 3.710 = Avg 3.706s
 - Blaze (no async) - 3.101 + 3.093 + 3.055 = Avg 3.083s
 - Blaze (with example async task) - 2.746 + 2.652 + 2.725 = Avg 2.707s

Obviously, blaze is an experiment and lacks so many features it's somewhat 
useless, but what would a large multi-project save on build time? Blaze
demonstrates how quickly your build actually could be -- and how current tools
still sorta suck.  And the fact it is using JavaScript under the hood means it
could be something interesting.

 - 1.4 times faster than SBT
 - 2.2 times faster than Maven
 - 2.5 times faster than Gradle

## Asynchronous tasks

While much of building a project requires serial/ordered execution, there are
definitely parts which could happen concurrently.  Especially within tasks.
For example, if you are going to create 3 jars -- a jar of your classes, a
source jar, and a javadocs jar, you could easily do all 3 of those in parallel.
The jar is just zipping up your compiled classes, the sources jar is zipping
up your existing sources, and the javadocs jar requires compiling the javadocs
and then zipping them up.

    // wait for pipeline of async action groups to all complete
    $A.pipeline(
        $A.async($T.jar, copyProjectJar, copyJarDependencies),
        $A.async($T.storkify)
    );


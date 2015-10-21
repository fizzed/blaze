Blaze by Fizzed
=======================================

 - [Fizzed, Inc.](http://fizzed.co)
 - Joe Lauer (Twitter: [@jjlauer](http://twitter.com/jjlauer))

## Overview

Speedy, simple, and intuitive make/build tool.  Write your project files using
JavaScript (ECMA 5.0) and run them blazingly fast using Java 8 (via its Nashorn
JavaScript engine).

The aim is for your build files to look like simple code -- with some interesting
concepts thrown in like logging (e.g. log.debug("wowza")) and asynchronous
tasks (why not easily do things in parallel).

Tasks can be defined once and run once.  Rather than relying on statements like
"dependsOn" (that almost every other build tool uses), you can simply run tasks
as functions and be assured they will only run once.  Not only does this make
more intuitive sense (IMHO) -- it also means you can program whatever ordering
you need your tasks to run in.  Let's take a look at a simple example of
compiling some code:

    var targetDir = "target";

    Blaze.actions.
    Blaze.tasks.
    Blaze.createTask
    Blaze.createAction

    T
    

    T.setup = 

    $T.setup = Task.create(function() {
        // code to make target dir
    });

    $T.compile = Task.create(function() {
        $T.setup();
        // code to compile project
    });

    $T.package = Task.create(function() {
        $T.compile();
        // code to package project
    });

Tasks are composed of JavaScript code, other tasks, or actions.  Actions are
similar to tasks, but are immutable once built and can be run any number of
times.  Actions are generally provided by Blaze and perform portable functions
like making directories, executing shell commands, logging output, etc.


## Examples

Similar to how Apache Ant and Make functions -- there are high level "tasks" that
are made up of JavaScript code, actions, and other tasks.  Here is a sample
blaze.js that can be run from the command line "blaze run" to execute the "run"
task defined below:

    var helloText = "Hello World";

    $T.hello = Task.create(function() {
        // print to stdout
        print(helloText);
        // print via logging
        log.info(helloText);
    });

    $T.run = Task.create(function() {
        $T.hello();
        log.info("Done");
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


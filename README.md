Blaze by Fizzed
=======================================

 - [Fizzed, Inc.](http://fizzed.com)

## Overview

A speedy, flexible, general purpose scripting stack for the JVM.  Can replace
shell scripts and plays nicely with other tools.  Only requires a Java 8 runtime
and adding `blaze.jar` to your project directory.  Start writing portable and 
cross-platform scripts -- that don't require the user to install anything!

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

 - Write your applications (e.g. scripts) in whatever JVM language you prefer.
   Out-of-the-box support for:
    - Java (.java)
    - JavaScript (.js)
    - Groovy (.groovy)
    - Or write your own (examples [here](core/src/main/java/com/fizzed/blaze/jdk), [here](core/src/main/java/com/fizzed/blaze/nashorn), and [here](groovy/src/main/java/com/fizzed/blaze/groovy))
 - Zero-install required. Just drop `blaze.jar` into your project directory and
   you or others can run it with `java -jar blaze.jar`.
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

## Install

Download `blaze.jar` to your project directory.  If you have `wget` available

    wget -O blaze.jar 'http://repo1.maven.org/maven2/com/fizzed/blaze-lite/0.2.1/blaze-lite-0.2.1.jar'

If you have `curl` available

    curl -o blaze.jar 'http://repo1.maven.org/maven2/com/fizzed/blaze-lite/0.2.1/blaze-lite-0.2.1.jar'

Or simply download the file in your web browser and name it `blaze.jar`
    
## Example

The [examples](examples) directory is full of cross-language example scripts
to help whet your appetite.

As a simple example, let's write a script to find out if "javac" is available
and if so what version is installed. We'll use the Blaze engine for ".java"
files for this example.  Create a new `blaze.java` file

```java
import org.slf4j.Logger;
import static com.fizzed.blaze.Systems.which;
import static com.fizzed.blaze.Systems.exec;
import com.fizzed.blaze.Contexts;
import java.io.File;

public class blaze {

    public void main() {
        Logger log = Contexts.logger();
        
        log.info("Finding javac...");
        File javacFile = which("javac").run();

        log.info("Using javac {}", javacFile);
        exec("javac", "-version").run();
    };
    
}
```

With `blaze.jar` installed (see above) this example can be easily run

    java -jar blaze.jar

Blaze will output something like

```
[INFO] Resolving dependencies...
[INFO] Resolved dependencies in 434 ms
[INFO] Adding zt-exec-1.8.jar to classpath
[INFO] Adding jsch-0.1.53.jar to classpath
[INFO] Compiling script...
[INFO] No need to recompile!
[INFO] Adding /tmp/blaze/iIAEMyt1hXuT8II9VOZj9g==/classes to classpath
[INFO] Compiled script in 7 ms
[INFO] Executing examples/javac.java:main...
[INFO] Finding javac...
[INFO] Using javac /usr/java/default/bin/javac
javac 1.8.0_66
[INFO] Executed examples/javac.java:main in 222 ms
[INFO] Blazed in 710 ms
```

If you'd like to quiet Blaze down and only log what your script produces run
it again with the `-q` command-line switch

    java -jar blaze.jar -q

This will output something like

```
[INFO] Finding javac...
[INFO] Using javac /usr/java/default/bin/javac
javac 1.8.0_66
```

## Example (but now in Groovy)

Same example as above, but now we'll write it in Groovy.  Create a new file
named `blaze.groovy`

```groovy
import static com.fizzed.blaze.Systems.which
import static com.fizzed.blaze.Systems.exec
import com.fizzed.blaze.Contexts;

def main() {
    def log = Contexts.logger()
    
    log.info("Finding javac...")
    def javac = which("javac").run()

    log.info("Using javac " + javac)
    exec("javac").arg("-version").run()
}
```

If you still have the `blaze.java` file in the example above then blaze will
complain you have multiple `blaze.[ext]` files present.  You could either delete
`blaze.java` to automatically pickup `blaze.groovy` or you can tell blaze what
file to run

    java -jar blaze.jar -f blaze.groovy

This will output

```
[INFO] Resolving dependencies...
[INFO] Resolved dependencies in 492 ms
[INFO] Adding zt-exec-1.8.jar to classpath
[INFO] Adding jsch-0.1.53.jar to classpath
[INFO] Adding blaze-groovy-0.1.0-SNAPSHOT.jar to classpath
[INFO] Adding groovy-all-2.4.5-indy.jar to classpath
[INFO] Compiling script...
[INFO] Compiled script in 340 ms
[INFO] Executing examples/javac.groovy:main...
[INFO] Finding javac...
[INFO] Using javac /usr/java/default/bin/javac
javac 1.8.0_66
[INFO] Executed examples/javac.groovy:main in 280 ms
[INFO] Blazed in 1160 ms
```

## Example (but now in JavaScript)

Same example as above, but now we'll write it in JavaScript.  Create a new file
named `blaze.js`

```javascript
/* global Packages */

var sys = Packages.com.fizzed.blaze.Systems;
var log = Packages.com.fizzed.blaze.Contexts.logger();

var main = function() {
    log.info("Finding javac...");
    var javac = sys.which("javac").run();

    log.info("Using javac {}", javac);
    sys.exec("javac").arg("-version").run();
};
```
If you still have the `blaze.java` or `blaze.groovy` files in the examples above
then blaze will complain you have multiple `blaze.[ext]` files present.  You
could either delete `blaze.java` or `blaze.groovy` to automatically pickup
`blaze.js` or you can tell blaze what file to run

    java -jar blaze.jar -f blaze.js

This will output

```
[INFO] Resolving dependencies...
[INFO] Resolved dependencies in 435 ms
[INFO] Adding zt-exec-1.8.jar to classpath
[INFO] Adding jsch-0.1.53.jar to classpath
[INFO] Compiling script...
[INFO] Compiled script in 444 ms
[INFO] Executing examples/javac.js:main...
[INFO] Finding javac...
[INFO] Using javac /usr/java/default/bin/javac
javac 1.8.0_66
[INFO] Executed examples/javac.js:main in 254 ms
[INFO] Blazed in 1181 ms
```
## More advanced examples

### Try all

The [try_all](examples/try_all.java) example is a more interesting demo of why
you should start using a real programming language for your scripts.  The demo
uses Java 8 lambdas to find all the other example scripts and spawns a new 
java process to try them all out.  The script is used to verify if all the
examples work (so we can test across platforms, between versions, etc.)

### Custom build tool

The [Font Mfizz custom build script](https://github.com/fizzed/font-mfizz/blob/master/blaze.groovy)
example is a demo of compiling a custom font for use on the web. An all around
example of multiple tasks that spawn processes, copy & delete files, and use
a third party Unix4j library for cat, sed, tail, and grep support.

## Configuration and dependency management

All scripts can have an optional `[script-name].conf` configuration file placed in
the same directory as the script.  It must have a name identical to the script
with an extension of `.conf`.  So if you script is `blaze.js` then you would
have a configuration file named `blaze.conf`.

### Application config

Any values in this config file will be available to your application via the
static `com.fizzed.blaze.Contexts.config()` method.  Let's say you have the
following `blaze.conf` file

```
undertow.port = 8080
undertow.host = localhost
```

In your script (we'll use .java as an example), you'd access the config value
like so

```java
import static com.fizzed.blaze.Contexts.config

// ... other code

Integer port = config().find("undertow.port", Integer.class).get();
```

`.get()` will throw an exception if the value is missing.  A default value can
be used instead with a call to `.or()` instead

```java
import static com.fizzed.blaze.Contexts.config

// ... other code

Integer port = config().find("undertow.port", Integer.class).or(9000);
```

The `Config` object prefers System properties over config file values.  So
standard Java system properties can be supplied on the command-line to override
values in the config file.  A great way to also pass arguments to your tasks.

    java -Dundertow.port=9001 -jar blaze.jar

### Blaze config

Blaze itself uses values from `Config` to configure itself as well.

`blaze.dependencies` will let you define an array of Maven-central-like dependencies
that will be downloaded, cached, and added to your classpath before your script
is executed.  For example, to add Google Guava as a dependency:

```
blaze.dependencies = [
    "com.google.guava:guava:18.0"
]
```

Try `examples/guava.js` or `examples/guava.groovy` to see it in action!

## What's included?

The best way to discover what's available out-of-the-box is to dive into the
source code and checkout the javadocs.  Here are the key classes

### Core (com.fizzed.blaze)

 - [Contexts](core/src/main/java/com/fizzed/blaze/Contexts.java)
 - [Systems](core/src/main/java/com/fizzed/blaze/Systems.java)
 - Contexts.config() => returns a [Config](core/src/main/java/com/fizzed/blaze/Config.java)
 - Contexts.logger() => returns a org.slf4j.Logger

### Command-line

When you execute `java -jar blaze.jar` these are the command-line options

```
blaze: [options] <task> [<task> ...]
-f|--file <file>  Use this blaze file instead of default
-d|--dir <dir>    Search this dir for blaze file instead of default (-f supercedes)
-l|--list         Display list of available tasks
-q                Only log blaze warnings to stdout (script logging is still info level)
-qq               Only log warnings to stdout (including script logging)
-x[x...]          Increases verbosity of logging to stdout
-v|--version      Display version and then exit
```

### Globbing

Finding and working with files and directories is one of the most common scripting
tasks.  One nice part of shell scripts is that you can take advantage of globbing
syntax to find files or directories.  Blaze provides excellent support for globbing
with a utility wrapper around Java's own glob support.  The [Java documentation
on globbing is a good start](http://docs.oracle.com/javase/7/docs/api/java/nio/file/FileSystem.html#getPathMatcher(java.lang.String))
, but here are some examples as well.

Statically import the `Globber.globber` method

```java
import static com.fizzed.blaze.util.Globber.globber;
```

Find all paths in the current working directory with ending with `.md`

```java
List<Path> paths = globber("*.md").scan();
```

Find all paths recursively in the current working directory with ending with `.md`

```java
List<Path> paths = globber("**/*.md").scan();
```

Find all paths recursively ending with `.md` but from a different base dir 

```java
List<Path> paths = globber("../a/different/path", "**/*.md").scan();
```

### SSH

Add the following to your `blaze.conf` file to include rich support for SSH
with Blaze wrappers around the stable pure Java JSch library.
You do not want to specify a version so Blaze will resolve the identical version
to whatever `blaze-core` you're running with.

```
blaze.dependencies = [
    "com.fizzed:blaze-ssh"
]
```

A session (connection) must be established before you can execute remote commands
or transfer files.  By default, Blaze will configure the session like OpenSSH --
it will load defaults from ~/.ssh/config, ~/.ssh/known_hosts, and your ~/.ssh/id_rsa
identity.  It'll prompt you for a password or to accept an unknown host as well.

```java
import static com.fizzed.blaze.SecureShells.sshConnect;

// ... other code

try (SshSession session = sshConnect("ssh://user@host").run()) {
    // ... use session
}
```

Once a session is established you can create channels to execute commands or
transfer files via sftp.  These channels all tap into the existing session and
do not require re-establishing a connection.  Much better than using `ssh` from
the command-line where you may re-establish a connection over and over again if
you need to run multiple commands.  This is an example of running the `which` command
to see if java is available.

```java
import static com.fizzed.blaze.SecureShells.sshConnect;
import static com.fizzed.blaze.SecureShells.sshExec;

// ... other code

try (SshSession session = sshConnect("ssh://user@host").run()) {

    SshExecResult result
        = sshExec(session)
            .command("which")
            .arg("java")
            .captureOutput()
            .run();
    
    log.info("java is at {}", result.output());

}
```

Working with the remote filesystem with sftp is also supported.

```java
import static com.fizzed.blaze.SecureShells.sshConnect;
import static com.fizzed.blaze.SecureShells.sshSftp;

// ... other code

try (SshSession session = sshConnect("ssh://user@host").run()) {

    try (SshSftpSession sftp = sshSftp(session).run()) {
            
        Path pwd = sftp.pwd();

        log.info("Remote working dir is {}", pwd);

        // get file attributes for current working dir
        SshFileAttributes attrs = sftp.lstat(pwd);

        log.info("{} with permissions {}", pwd, PosixFilePermissions.toString(attrs.permissions()));

        sftp.ls(pwd)
            .stream()
                .forEach((file) -> {
                    log.info("{} {} at {}", file.attributes().lastModifiedTime(), file.path(), file.attributes().size());
                });

        sftp.put()
            .source("my/source/file.txt")
            .target("file.txt")
            .run();

        sftp.get()
            .source("file.txt")
            .target("my/target/file.txt")
            .run();

        // many more methods in sftp class...
    }
}
```

### Http

Add the following to your `blaze.conf` file to include rich support for HTTP --
much better support than the JDK HttpUrlConnection class.
You do not want to specify a version so Blaze will resolve the identical version
to whatever `blaze-core` you're running with.

```
blaze.dependencies = [
    "com.fizzed:blaze-http"
]
```

For now this is mostly a "virtual" dependency that will trigger the transitive
dependency of Apache Fluent HttpClient to be downloaded and added to the classpath.
Apache's own transitive dependencies will be correctly excluded to pickup the
right SLF4J bindings.  Down the road we may provide additional wrappers to make
working with HTTP via Apache HttpClient even easier -- although it's [own
fluent client](https://hc.apache.org/httpcomponents-client-ga/tutorial/html/fluent.html) isn't awful.

Checkout [this](examples/http.java) for an example API get request

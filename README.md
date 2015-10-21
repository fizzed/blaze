Blaze by Fizzed
=======================================

 - [Fizzed, Inc.](http://fizzed.com)

## Overview

A speedy, simple, and flexible scripting tool built on top of Java 8. Stop
using shell scripts and write portable scripts that can automatically tap into
the great world of existing Java libraries.  The goal is to let you use your
existing JVM skills to help automate tasks.

 - Pluggable scripting engine with built-in support for Javascript (Nashorn)
   and Groovy
 - Automatic dependency management at runtime (e.g. download dependencies from Maven central)
 - Zero-install - simply check-in a `blaze.jar` to your repo
 - Scripts are compiled and run on-the-fly
 - Shell-like commands are intelligent and mimic what your shell would do to
   find a command by searching your PATH as well as well-known extensions like
   .sh (on linux) or .exe, .bat, or .cmd (on windows).

## Examples

### Groovy

Define methods which will become the tasks available for a user to execute. Then
import the static functions from Blaze so they are available to your code. An
example `blaze.groovy` file that finds `javac` on your system and then executes
it to print out a version.

```groovy
import static com.fizzed.blaze.Shells.*

def main() {
    println("Finding javac...")
    def javac = which("javac").run()

    println("Using javac " + javac)
    exec("javac").arg("-version").run()
}
```

Execute the script

```shell
java -jar blaze.jar
```

### Javascript (Nashorn)

Create functions which will become the tasks available for a user to execute. Then
import the class using the Nashorn-specific `Packages` statement so they are
available in whatever variable (namespace) you used. An example `blaze.js` file
that finds `javac` on your system and then executes it to print out a version.

```javascript
var sh = Packages.com.fizzed.blaze.Shells;

var main = function() {
    print("Finding javac...");
    var javac = sh.which("javac").run();

    print("Using javac " + javac);
    sh.exec("javac").arg("-version").run();
}
```

Execute the script

```shell
java -jar blaze.jar
```
### Output

Both scripts would output the following:

```
Finding javac...
Using javac /usr/java/default/bin/javac
javac 1.8.0_51
```

## Configuration and dependency management

All scripts can have an optional `script.conf` configuration file placed in
the same directory as the script.  It must have a name identical to the script
with an extension of `.conf`.  So if you script is `blaze.js` then you would
have a configuration file named `blaze.conf`.

`blaze.dependencies` will let you define an array of Maven-central-like dependencies
that will be downloaded, cached, and added to your classpath before your script
is executed.  For example, add Google Guava as a dependency:

```
blaze.dependencies = [
  "com.google.guava:guava:18.0"
]
```

Try `examples/guava.js` or `examples/guava.groovy` to see it in action!

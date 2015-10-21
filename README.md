Blaze by Fizzed
=======================================

 - [Fizzed, Inc.](http://fizzed.co)
 - Joe Lauer (Twitter: [@jjlauer](http://twitter.com/jjlauer))

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

## Examples

### Groovy

Define methods which will become the tasks available for a user to execute. Then
import the static functions from Blaze so they are available to your code. An
example `blaze.groovy` file:

```groovy
import static com.fizzed.blaze.Shells.*

def main() {
    println("Finding mvn...")
    def mvn = which("mvn").run()

    println("Using mvn " + mvn)
    exec(mvn).arg("-v").run()
}
```

Execute the script

```shell
java -jar blaze.jar
```

### Javascript (Nashorn)

Create functions which will become the tasks available for a user to execute. Then
import the class using the Nashorn-specific `Packages` statement so they are
available in whatever variable (namespace) you used. An example `blaze.js` file:

```javascript
var sh = Packages.com.fizzed.blaze.Shells;

var main = function() {
    print("Finding mvn...");
    var mvn = sh.which("mvn").run();

    print("Using mvn " + mvn);
    sh.exec("mvn").arg("-v").run();
}
```

Execute the script

```shell
java -jar blaze.jar
```

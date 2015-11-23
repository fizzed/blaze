Blaze by Fizzed
=======================================

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
[INFO] Resolved dependencies in 429 ms
[INFO] Compiling script...
[INFO] Compiled script in 7 ms
[INFO] Executing examples/javac.java:main...
[INFO] Finding javac...
[INFO] Using javac /usr/java/default/bin/javac
javac 1.8.0_66
[INFO] Executed examples/javac.java:main in 225 ms
[INFO] Blazed in 708 ms
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
import com.fizzed.blaze.Contexts

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
[INFO] Resolved dependencies in 447 ms
[INFO] Compiling script...
[INFO] Compiled script in 346 ms
[INFO] Executing examples/javac.groovy:main...
[INFO] Finding javac...
[INFO] Using javac /usr/java/default/bin/javac
javac 1.8.0_66
[INFO] Executed examples/javac.groovy:main in 284 ms
[INFO] Blazed in 1125 ms
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
[INFO] Resolved dependencies in 429 ms
[INFO] Compiling script...
[INFO] Compiled script in 446 ms
[INFO] Executing examples/javac.js:main...
[INFO] Finding javac...
[INFO] Using javac /usr/java/default/bin/javac
javac 1.8.0_66
[INFO] Executed examples/javac.js:main in 256 ms
[INFO] Blazed in 1178 ms
```

## More advanced examples

### Try all example

The [try_all](examples/try_all.java) example is a more interesting demo of why
you should start using a real programming language for your scripts.  The demo
uses Java 8 lambdas to find all the other example scripts and spawns a new 
java process to try them all out.  The script is used to verify if all the
examples work (so we can test across platforms, between versions, etc.)

### Custom build script example

The [Font Mfizz custom build script](https://github.com/fizzed/font-mfizz/blob/master/blaze.groovy)
example is a demo of compiling a custom font for use on the web. An all around
example of multiple tasks that spawn processes, copy & delete files, and use
a third party Unix4j library for cat, sed, tail, and grep support.
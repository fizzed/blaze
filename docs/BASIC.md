Blaze by Fizzed
=======================================

## Command-line

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
-Dname=value      Sets a System property as name=value
```

## Running on a JRE

If you are using `.java` scripts then those will need to be compiled.  As long
as you're running a JDK or a Server JRE then you'll be okay.  However, if you 
do need to run on a JRE or ensure you're script always runs regardless then
simply adding the Eclipse compiler as a runtime dependency will work.  There
are two ways.  First, you can create a `blaze.conf` file in the same directory
as your `blaze.java` file and add it:

```
blaze.dependencies = [
  "org.eclipse.jdt.core.compiler:ecj:4.6.1"
]
```

Alternatively, you could supply it from the command-line:

```
java -jar blaze.jar -Dblaze.dependencies.0=org.eclipse.jdt.core.compiler:ecj:4.6.1
```

## Globbing

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

Many blaze commands accept a globber object (no need to call .scan() on it either)

```java
Systems.remove(globber("**/*.md")).run();
```


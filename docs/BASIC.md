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


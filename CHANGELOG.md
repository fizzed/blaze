Blaze by Fizzed
===============

#### 0.6.1 - 2015-11-16

 - Windows -i command-line switch installs both blaze.bat and blaze (so it's
   compatible with bash, mingw32, etc.)

#### 0.6.0 - 2015-11-16

 - Netbeans plugin support 
 - Default blaze script now searches current dir and then "blaze" subdir
 - Examples adjusted to include shebang on groovy and kotline scripts
 - Exception message for missing tasks optimized
 - Exec, which, and requireExec now support actual file and paths
 - blaze.jar supports "-i" command-line switch to install helper blaze or blaze.bat
   scripts to a target directory

#### 0.5.0 - 2015-11-10

 - Support for Kotlin and Kotlin scripts
 - Default logging statements optimized (even less by default)
 - Cached compiles now use MD5 hashes vs. timestamps

#### 0.4.0 - 2015-11-05

 - System properties with -D are now processed and passed thru to a script
   (e.g. `java -jar blaze.jar task0 task1 -Dmyarg=true`)
 - Config refactored with `value` and `valueList` instead of `find` and `findList`.
   Returned value now `get()`, `getOr(defaultValue)`, and `getOrNull()`.
 - System.exec now supports the NamedStream class for piping of input, output,
   and error.

#### 0.3.0 - 2015-11-03
 
 - Feature rich SSH client for both executing commands (exec) and file transfers
   (sftp). Very much like programmatic access to "openssh" but all with pure Java.
   See examples/ssh.java and examples/sftp.java for demos.
 - Excellent "glob" support with com.fizzed.blaze.util.Globber class
   See examples/globber.java for demo.

#### 0.2.1 - 2015-10-28

 - Initial public release

#### 0.1.0 - 2013-05-01

 - Never released

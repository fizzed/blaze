Blaze by Fizzed
==========================

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

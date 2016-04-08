Blaze by Fizzed
===============

#### x.x.x

 - Systems.exec and SecureShells.exec support a convenient `runCaptureOutput`
   method to simplify the use-case of capturing the output of a process.
 - Convenience disablePipeInput, disablePipeOutput, and disablePipeError added
   for Systems.exec and SecureShells.exec to simplify syntax of disabling any
   of those particular inputs/outputs.

#### 0.9.1 - 2016-04-08

 - zt-exec now included with blaze-lite jar by default. Allows for faster
   execution by skipping resolver.

#### 0.9.0 - 2016-04-08

 - Dependency resolver skipped if all dependencies already resolved.  Huge
   speedup in execution for scripts that don't use additional dependencies.
 - Blaze can execute objects as scripts w/o requiring an engine. Useful for
   embedding blaze in other java projects and/or unit tests.
 - Refactored how System.in, System.out, and System.err is handled by both
   Systems.exec and SecureShells.sshExec.
 - New utility classes for guarding streams against being closed and 
   non-blocking InputStreams.
 - Blaze command-line `Bootstrap` class can be easily subclassed so that most
   of its functionality can be reused in a Blaze-based script app.
 - A `DefaultContext` is now bound by default so users of Blaze as a dependency
   in other projects do not need to bind one.

#### 0.8.2 - 2016-04-04

 - blaze-ssh underlying jsch exec would never terminate threads that pumped
   the InputStream to the remote host -- implemented workaround using wrapped
   streams
 - blaze-ssh now supports readlink and realpath via sftp.
 - blaze-ssh sshExec commands correct path delimiter when running on windows
 - Bump to crux-vagrant v0.3.2 for unit testing w/ real ssh virt machines

#### 0.8.1 - 2016-03-28

 - Support for placing default script in  `.blaze` sub directory.  From your
   working directory, Blaze will now automatically search `blaze.[ext]` then 
   `blaze/blaze.[ext]` then `.blaze/blaze.[ext]`
 - Contexts withBaseDir and withUserDir return normalized paths
 - Fixed issue with file paths for scripts using `blaze-ssh` from windows to
   remote non-windows operating systems.
 - Fixed issue with null default charset on LineAction
 - Added vagrant for true integration junit testing on various operating systems.
 - Refactored project to have each module in a prefixed dir (e.g. blaze-ssh)

#### 0.8.0 - 2015-11-20

 - Refactored actions to return a concrete Result.  run() returns the primary
   result type and runResult() returns the full result.
 - Context.userDir() now checks for environment HOME variable rather than
   Java property "user.home".  Allows running under "sudo" properly.

#### 0.7.0 - 2015-11-18

 - Scripts now can throw checked exceptions.  Enables cleaner stacktraces to
   console without using wrapped runtime exceptions.
 - Refactored capturing output from actions (e.g. Exec).  A CaptureOutput needs
   to be injected into an action.
 - Added Tail feature - tailing output for N number of lines
 - Added Head feature - heading output for N number of lines
 - Added Pipeline feature - running multiple actions in sequence with streaming
   I/O between

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

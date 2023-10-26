Blaze by Fizzed
===============

#### 1.2.1 - 2023-10-26

 - Prevent maven project installs and deploys in IDE support

#### 1.2.0 - 2023-10-26

 - Add additional default ~/.ssh identities that will be loaded: id_ed25519, id_ecdsa, id_ecdsa_sk, id_ed25519_sk, id_xmss
 which are on top of existing id_rsa and id_dsa
 - New general purpose blaze command "--generate-maven-project" to generate a pom.xml file that lives in same directory as 
as the blaze script for IDE support. Will calculate dependencies specified in your [blaze script].conf file.
 - Support for maven dependency keywords LATEST and RELEASE for dependency versions. LATEST will pull in most recent
SNAPSHOT or release version, while RELEASE will only pull in latest released version.
 - Bump commons-io to v2.11.0
 - Bump ivy to v2.5.2
 - Bump jsch fork to v0.2.12
 - Bump slf4j to v2.0.7

#### 1.1.0 - 2023-01-02

 - Switch to fork of jsch (since jsch is no longer maintained). Improves support for modern ciphers, etc.
 - Automated testing using github workflows

#### 1.0.2 - 2021-10-07

 - Improved support for ssh-agents in blaze-ssh

#### 1.0.1 - 2021-10-07

 - Support for ssh-agents in blaze-ssh

#### 1.0.0 - 2020-06-09

 - New blaze-haproxy module
 - New blaze-docker module to simplify executing on docker containers
 - New blaze-mysql module
 - Ability to configure additional Maven repositories (including private)
 - Credentials are pulled from ~/.m2/settings.xml for additional repos
 - Fix bug with SSH sessions sometimes closing stdin of executing blaze app
 - Major changes to under-the-hood on ivy resolver.
 - Improved argument handling.  Allowing --arg to also be equivalent of -Darg
 - New prompt() with lots of options to help with input
 - Fix issue of ssh session close causing stdin to close
 - Bump commons-io to v2.7
 - Bump slf4j to v1.7.30
 - zt-turnaround to v1.11
 - Bump ivy to v2.5.0

#### 0.21.0 - 2020-06-01

 - Improved SFTP put/get progress meter (uses single line of text e.g. wget)

#### 0.20.0 - 2019-05-24

 - Support for Java 9, 10, 11, and up

#### 0.18.0 - 2017-12-13

 - Blaze .java engine automatically adds Eclipse compiler to dependency resolution
   if running on JRE
 - Custom zt-exec InputStreamPumper fixes unnecessary aggressive error logging
 - Eclipse compiler now omits compile warnings from stdout (matches JDK compiler)

#### 0.17.0 - 2017-12-12

 - Fix issue with Streamables.input not passing EOF thru in some cases.
 - Added Streamables.input for text

#### 0.16.0 - 2017-01-30

 - Bump jsch from v0.1.53 to v0.1.54
 - blaze-ssh: Remove debug output indicating jar was a snapshot
 - blaze-ssh: Fix random connect timeouts when via bastion/jump host

#### 0.15.1 - 2016-12-09

 - Fix issue parsing system properties w/ an equals char

#### 0.15.0 - 2016-09-27

 - Exec.workingDir no longer tries to resolve against project base dir.
 - More defensive code while searching for homedrive on windows

#### 0.14.1 - 2016-09-21

 - Improve user home dir locator for windows

#### 0.14.0 - 2016-09-16

 - New `@Task` annotation can be added to blaze methods to declare descriptions
   and ordering of tasks being listed.  Works in Java, Groovy, and Kotlin engines.
   See `examples/hello*` scripts for sample usage.
 - Moved ivy dependency resolver from `blaze-core` to `blaze-ivy` module.  Ivy
   is no longer a dependency of `blaze-core` so other projects using `blaze-core`
   don't need to specifically exclude it.
 - `blaze-ivy` no longer uses cache for depdendencies with a version ending
   with "-SNAPSHOT".
 - Blaze tasks() returns a `ScriptTask` rather than `String`

#### 0.13.0 - 2016-09-06

 - New `blaze-vagrant` module
 - Support for new `vagrant+ssh` scheme to connect via SSH to vagrant
   instances. Simply add the `blaze-vagrant` module to your config.

#### 0.12.0 - 2016-08-29

 - Support for SSH proxy/bastion/jump hosts. SSH connects will do a best
   effort at using the `ProxyCommand` value from your ssh config file.  Or you
   can set one programmatically with the `SshConnect.proxy()` method.
 - Support for password auth via keyboard-interface method.
 - Improved testing w/ real systems for SSH.  Tests are now run against
   Ubuntu 16.04, Ubuntu 14.04, Debian 8, Centos 6, Centos 7, FreeBSD 10.2,
   and OpenBSD 5.8.
 - Moved all remaining `Jsch` specific implementation code from package
   `com.fizzed.blaze.ssh`.
 - slf4j from v1.7.20 to v1.7.21
 - commons-io from v2.4 to v2.5
 - groovy from v2.4.6 to v2.4.7
 - kotlin from v1.0.1-2 to v1.0.2

#### 0.11.1 - 2016-06-24

 - Support for enabling a pty on SshExec (which helps run certain commands
   execute as you'd expect, but not always needed, so its false by default)

#### 0.11.0 - 2016-05-27

 - Bump to kotlin 1.0.2 for blaze-kotlin
 - Support for kotlin v1!

#### 0.10.0 - 2016-04-12

 - Bump to zt-exec v1.9 (for an issue we reported and fixed with a PR)
 - Bump to groovy v2.4.6
 - Bump to slf4j v1.7.20
 - Fixed bug with Systems.which() where it would locate a directory that matched an
   executable name.  Since Systems.requireExec() and Systems.exec() uses this 
   under-the-hood, this also fixes the same issue with them. 

#### 0.9.2 - 2016-04-08

 - Systems.exec and SecureShells.exec support a convenient `runCaptureOutput`
   method to simplify the use-case of capturing the output of a process.
 - Convenience disablePipeInput, disablePipeOutput, and disablePipeError added
   for Systems.exec and SecureShells.exec to simplify syntax of disabling any
   of those particular inputs/outputs.
 - Streamables now supports lines() and matchedLines() that produce a Stream
   of Strings.  Useful for grep-like replacement.

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

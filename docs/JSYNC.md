Blaze by Fizzed
=======================================

## Jsync

Add the following to your `blaze.conf` file to include extensive support for rsync-like functionality. You can synchronize
files or directories between local and remote hosts (via SSH/SFTP).  Blaze uses the excellent [Jsync](https://github.com/fizzed/jsync)
library for the actual rsync functionality.

You do not want to specify a version so Blaze will resolve the identical version to whatever `blaze-core` you're running with.

    blaze.dependencies = [
        "com.fizzed:blaze-ssh",
        "com.fizzed:blaze-jsync"
    ]

### Volumes

To abstract away the details of the remote filesystem, Blaze uses a concept of a `Volume`. A volume is a filesystem
that can be accessed via SSH/SFTP or locally. Blaze comes with a `LocalVolume` implementation that represents the local filesystem.
It also ships with an `SftpVolume` implementation that represents a remote filesystem via SFTP/SSH.

### Example

There is an example of using Jsync in the examples directory of the Blaze source code.  To run it, you can use the following command
in the root of this project directory:

    java -jar blaze.jar examples/jsync.java --from /home/jjlauer/Downloads/example.iso --to bmh-build-x64-freebsd15-1:. --mode nest

### Usage

Here is an example of syncing a directory from one local directory to another, along with asking for verbose output and progress reporting.
Instead of using the somewhat confusing "does this directory end with a slash?" syntax, you need to explicitly tell 
jsync what `mode` you want to use.  The `MERGE` mode will take source dir and merge it into the target directory, so that
target directory becomes the source directory. It's akin to adding a slash to the end of the source and target directories.
The `NEST` mode will take the source directory and copy it into the target directory, so that the target directory becomes
a subdirectory of the target directory.

```java
import static com.fizzed.blaze.jsync.Jsyncs.*;
import com.fizzed.blaze.jsync.engine.JsyncMode;

// ... other code

jsync(localVolume("example/from-dir"), localVolume("example/to-dir"), JsyncMode.MERGE)
    .verbose()
    .progress()
    .run();
```

There are also many other options available, such as specifying a filter, excluding files, etc. See the [Jsync javadocs
and class for more information](https://github.com/fizzed/blaze/blob/master/blaze-jsync/src/main/java/com/fizzed/blaze/jsync/Jsync.java).

```java
import static com.fizzed.blaze.jsync.Jsyncs.*;
import com.fizzed.blaze.jsync.engine.JsyncMode;

// ... other code

jsync(localVolume("example/from-dir"), localVolume("example/to-dir"), JsyncMode.MERGE)
    .verbose()
    .progress()
    .parents()
    .force()
    .delete()
    .skipPermissions()
    .ignoreTimes()
    .ignore(".git")    
    .run();
```

To sync from a remote host, you can use the `sftpVolume` method to create a volume that represents a remote filesystem.
You can use this volume as the source, target, or both.  Jsync supports synchronizing local-to-remote, remote-to-local,
or even remote-to-remote (where the data flows thru the host you run it on).

Jsync supports `sftpVolume` which will create the underlying SSH & SFTP session under the hood for you, or if you already
have an SSH or SFTP sessions, you can pass those in directly and they will be used instead of creating new ones. This
is significantly more efficient than creating a new SSH session for every sync, or if you are already doing other SSH
or SFTP operations outside of using Jsync.  To have Jsync create the SSH session for you, use the `sshVolume` method.

```java
import static com.fizzed.blaze.jsync.Jsyncs.*;
import com.fizzed.blaze.jsync.engine.JsyncMode;

// ... other code

jsync(localVolume("example/from-dir"), sftpVolume("target-host.example.com", "example/to-dir"), JsyncMode.MERGE)
    .verbose()
    .progress()
    .run();
```

Alternatively, here is an example where we will re-use an existing SSH session to sync from a remote host to a local directory.

```java
import static com.fizzed.blaze.SecureShells.sshConnect;

// ... other code

try (SshSession ssh = sshConnect("ssh://user@internal1").run()) {
    jsync(sftpVolume(ssh, "example/from-dir"), localVolume("example"), JsyncMode.NEST)
        .verbose()
        .progress()
        .run();
}
```

Alternatively, here is an example where we will re-use an existing SSH and SFTP session to sync from a remote host to a local directory.

```java
import static com.fizzed.blaze.SecureShells.sshConnect;

// ... other code

try (SshSession ssh = sshConnect("ssh://user@internal1").run()) {
    try (SshSftpSession sftp = sshSftp(session).run()) {
        jsync(sftpVolume(ssh, sftp, "example/from-dir"), localVolume("example"), JsyncMode.NEST)
            .verbose()
            .progress()
            .run();
    }
}
```

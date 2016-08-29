Blaze by Fizzed
=======================================

## SSH

Add the following to your `blaze.conf` file to include extensive support for SSH
with Blaze wrappers around the stable pure Java JSch library.
You do not want to specify a version so Blaze will resolve the identical version
to whatever `blaze-core` you're running with.

    blaze.dependencies = [
        "com.fizzed:blaze-ssh"
    ]

A session (connection) must be established before you can execute remote commands
or transfer files.  By default, Blaze will configure the session like OpenSSH --
it will load defaults from ~/.ssh/config, ~/.ssh/known_hosts, and your ~/.ssh/id_rsa
identity.  It'll prompt you for a password or to accept an unknown host as well.

```java
import static com.fizzed.blaze.SecureShells.sshConnect;

// ... other code

try (SshSession session = sshConnect("ssh://user@host").run()) {
    // ... use session
}
```

As of v0.12.0 support for connecting thru 1 or more bastion/jump/proxy hosts is
supported.  Proxy support can be enabled using two different methods.  First,
your ssh config file (e.g. `.ssh/config`) may contain a `ProxyCommand` value
that Blaze will honor (with Java, btw!).  Using the following `.ssh/config` file:

    Host jump1
        HostName jump1.example.com
        Port 2222
        IdentityFile ~/.ssh/my-jump-identity.pem

    Host internal1
        IdentityFile ~/.ssh/my-internal-identity.pem
        ProxyCommand ssh jump1 nc %h %p

In Blaze you will simply connect to host `internal1` and Blaze will detect
you actually need to get to it via a proxy server and will first connect to
host `jump1`, then execute the command `nc %h %p`, then connect your SSH session.

```java
import static com.fizzed.blaze.SecureShells.sshConnect;

// ... other code

try (SshSession session = sshConnect("ssh://user@internal1").run()) {
    // ... use session
}
```

Alternatively, you can programmatically define proxy hosts instead of relying
on an outside config file.

```java
import static com.fizzed.blaze.SecureShells.sshConnect;

// ... other code

try (SshSession proxy = sshConnect("ssh://user@jump1").run()) {
    try (SshSession session = sshConnect("ssh://user@internal1").proxy(proxy).run()) {
        // ... use session
    }
}
```

One advantage to using this approach is your session to the jump/bastion/proxy
host is established once and can be reused over and over again if you are 
automating working with many internal hosts.

Once a session is established you can create channels to execute commands or
transfer files via sftp.  These channels all tap into the existing session and
do not require re-establishing a connection.  Much better than using `ssh` from
the command-line where you may re-establish a connection over and over again if
you need to run multiple commands.  This is an example of running the `which` command
to see if java is available.

```java
import static com.fizzed.blaze.SecureShells.sshConnect;
import static com.fizzed.blaze.SecureShells.sshExec;

// ... other code

try (SshSession session = sshConnect("ssh://user@host").run()) {

    String result
        = sshExec(session)
            .command("which")
            .arg("java")
            .runCaptureOutput()
            .toString();
    
    log.info("java is at {}", result);

}
```

There are other ways to `run()` the SshExec command. The one above shows capturing
the `stdout` stream, but you can also use the alternative `run()` method to
get the exit value or `runResult()` to get an even more detailed result.

Working with the remote filesystem with sftp is also supported.

```java
import static com.fizzed.blaze.SecureShells.sshConnect;
import static com.fizzed.blaze.SecureShells.sshSftp;

// ... other code

try (SshSession session = sshConnect("ssh://user@host").run()) {

    try (SshSftpSession sftp = sshSftp(session).run()) {
            
        Path pwd = sftp.pwd();

        log.info("Remote working dir is {}", pwd);

        // get file attributes for current working dir
        SshFileAttributes attrs = sftp.lstat(pwd);

        log.info("{} with permissions {}", pwd, PosixFilePermissions.toString(attrs.permissions()));

        sftp.ls(pwd)
            .stream()
                .forEach((file) -> {
                    log.info("{} {} at {}", file.attributes().lastModifiedTime(), file.path(), file.attributes().size());
                });

        sftp.put()
            .source("my/source/file.txt")
            .target("file.txt")
            .run();

        sftp.get()
            .source("file.txt")
            .target("my/target/file.txt")
            .run();

        // many more methods in sftp class...
    }
}
```

### Passwordless authentication not working

Verify authentication works via openssh without prompting for password

    ssh user@host

Verify you don't have some other mechanism in place (e.g. ssh-agent).  Blaze's
SSH implementation uses JSch under-the-hood and it currently supports public key
and password authentication.

### Why doesn't setting environment variables work?

Most likley your SSHD server is filtering them out.  OpenSSH has an `AcceptEnv`
configuration file that determines which environment variables are allowed.

Blaze by Fizzed
=======================================

## SSH

Add the following to your `blaze.conf` file to include rich support for SSH
with Blaze wrappers around the stable pure Java JSch library.
You do not want to specify a version so Blaze will resolve the identical version
to whatever `blaze-core` you're running with.

```
blaze.dependencies = [
    "com.fizzed:blaze-ssh"
]
```

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

    SshExecResult result
        = sshExec(session)
            .command("which")
            .arg("java")
            .captureOutput()
            .run();
    
    log.info("java is at {}", result.output());

}
```

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

Blaze by Fizzed
=======================================

 - [Fizzed, Inc.](http://fizzed.com)

## Passwordless authentication not working

Verify authentication works via openssh without prompting for password

    ssh user@host

Verify you don't have some other mechanism in place (e.g. ssh-agent).  Blaze's
SSH implementation uses JSch under-the-hood and it currently supports public key
and password authentication.

## Why doesn't setting environment variables work?

Most likley your SSHD server is filtering them out.  OpenSSH has an `AcceptEnv`
configuration file that determines which environment variables are allowed.

Blaze by Fizzed
=======================================

## Development and testing

Standard maven goals are used.  Compiling:

    mvn compile

To run the majority of tests

    mvn test

Since blaze interacts with operating systems, Vagrant helps to test against
real systems (especially for `blaze-ssh`).  If vagrant isn't installed or any
instance running, then those unit tests are skipped.  However, to make sure
the full suite of unit tests run, its good to init Vagrant.  To bring up an
Ubuntu 14.04 instance to be used by additional unit tests.

    vagrant up
    mvn test

Ubuntu 14.04 is the default Vagrant instance, but there are numerous other
operating systems in the `Vagrantfile` to ensure compat with a wider range of
real systems.  To test everything:

    vagrant up ubuntu14 ubuntu16 debian8 centos7 centos6 freebsd102
    mvn test

Blaze by Fizzed
=======================================

## Configuration and dependency management

All scripts can have an optional `[script-name].conf` configuration file placed in
the same directory as the script.  It must have a name identical to the script
with an extension of `.conf`.  So if you script is `blaze.js` then you would
have a configuration file named `blaze.conf`.

### Application config

Any values in this config file will be available to your application via the
static `com.fizzed.blaze.Contexts.config()` method.  Let's say you have the
following `blaze.conf` file

```
undertow.port = 8080
undertow.host = localhost
```

In your script (we'll use .java as an example), you'd access the config value
like so

```java
import static com.fizzed.blaze.Contexts.config

// ... other code

Integer port = config().value("undertow.port", Integer.class).get();
```

`.get()` will throw an exception if the value is missing.  A default value can
be used instead with a call to `.getOr()` instead

```java
import static com.fizzed.blaze.Contexts.config

// ... other code

Integer port = config().value("undertow.port", Integer.class).getOr(9000);
```

The `Config` object prefers System properties over config file values.  So
standard Java system properties can be supplied on the command-line to override
values in the config file.  A great way to also pass arguments to your tasks.

    java -Dundertow.port=9001 -jar blaze.jar

### Blaze Dependencies

Blaze itself uses values from `Config` to configure itself as well.

`blaze.dependencies` will let you define an array of Maven-central-like dependencies
that will be downloaded, cached, and added to your classpath before your script
is executed.  For example, to add Google Guava as a dependency:

```
blaze.dependencies = [
    "com.google.guava:guava:18.0"
]
```

Try `examples/guava.js` or `examples/guava.groovy` to see it in action!

### Blaze Repository

If you need to change which maven repositories it will pull artifacts from, you can modify
the config `blaze.repositories`:

```
blaze.repositories = [
  "mycompany|https://repos.example.com/repository/maven-public"
]
```

### Latest and Release Versions

As of Blaze v1.2.0+, the version keywords of "latest" and "release" are supported.  These are wildcard versions that
will pull in either any recent version (including snapshots) or just the most recent released version.

```
blaze.dependencies = [
    "com.google.guava:guava:latest"
    "com.fizzed:crux-util:release"
]
```

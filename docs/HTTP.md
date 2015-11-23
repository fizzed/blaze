Blaze by Fizzed
=======================================

## HTTP

Add the following to your `blaze.conf` file to include rich support for HTTP --
much better support than the JDK HttpUrlConnection class.
You do not want to specify a version so Blaze will resolve the identical version
to whatever `blaze-core` you're running with.

```
blaze.dependencies = [
    "com.fizzed:blaze-http"
]
```

For now this is mostly a "virtual" dependency that will trigger the transitive
dependency of Apache Fluent HttpClient to be downloaded and added to the classpath.
Apache's own transitive dependencies will be correctly excluded to pickup the
right SLF4J bindings.  Down the road we may provide additional wrappers to make
working with HTTP via Apache HttpClient even easier -- although it's [own
fluent client](https://hc.apache.org/httpcomponents-client-ga/tutorial/html/fluent.html) isn't awful.

Checkout [this](../examples/http.java) for an example API get request

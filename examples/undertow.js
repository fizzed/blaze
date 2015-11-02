/* global java, Packages, Contexts, Undertow, Handlers, undertow, int */

// nashorn recommended method of importing classes
var Imports = new JavaImporter(
    java.nio.file.Path,
    java.lang.Integer,
    java.lang.Boolean,
    java.lang.Thread,
    Packages.com.fizzed.blaze.Contexts,
    Packages.io.undertow.Undertow,
    Packages.io.undertow.server.HttpHandler,
    Packages.io.undertow.server.HttpServerExchange,
    Packages.io.undertow.util.Headers,
    Packages.io.undertow.server.handlers.resource.PathResourceManager,
    Packages.io.undertow.Handlers);
    
with (Imports) {

    var main = function() {
        var dir = Contexts.baseDir();
        var log = Contexts.logger();
        var config = Contexts.config();

        var host = config.find("undertow.host").get();
        var port = config.find("undertow.port", Integer.class).get();
        var in_try_all_example = config.find("examples.try_all").or("false");
        
        var undertow = Undertow.builder()
            .addHttpListener(port, host)
            .setHandler(Handlers.resource(new PathResourceManager(dir, 100)).setDirectoryListingEnabled(true))
            .build();

        undertow.start();

        log.info("Open browser to http://{}:{}", host, port);
        
        if (in_try_all_example.equals("true")) {
            // simply for stopping server if we're in try_all example
            undertow.stop();
        } else {
            // hack to wait
            Thread.sleep(10000000);
        }
    };
    
}
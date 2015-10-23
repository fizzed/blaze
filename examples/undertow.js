// nashorn recommended method of importing classes
var Imports = new JavaImporter(
    java.nio.file.Paths,
    Packages.com.fizzed.blaze.Contexts,
    Packages.io.undertow.Undertow,
    Packages.io.undertow.server.HttpHandler,
    Packages.io.undertow.server.HttpServerExchange,
    Packages.io.undertow.util.Headers,
    Packages.io.undertow.server.handlers.resource.PathResourceManager,
    Packages.io.undertow.Handlers);
    
with (Imports) {

    var main = function() {
        //def dir = Paths.get(System.getProperty("user.home"))
        var dir = Contexts.baseDir().toPath();

        var undertow = Undertow.builder()
            .addHttpListener(8080, "localhost")
            .setHandler(Handlers.resource(new PathResourceManager(dir, 100))
                .setDirectoryListingEnabled(true))
            .build();

        undertow.start();

        log.info("Open browser to http://localhost:8080");
    }
    
}
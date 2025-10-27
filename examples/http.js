/* global Packages, Contexts, MutableUri, Request */

// nashorn recommended method of importing classes
var Imports = new JavaImporter(
    Packages.org.slf4j.Logger,
    Packages.com.fizzed.blaze.Contexts,
    Packages.com.fizzed.blaze.util.MutableUri,
    Packages.com.fizzed.blaze.Https);
    
with (Imports) {
    
    var log = Contexts.logger();

    var main = function() {
        var uri = MutableUri.of("http://jsonplaceholder.typicode.com/comments")
            .query("postId", 1)
            .toURI();

        var output = Https.httpGet(uri.toString())
            .addHeader("Accept", "application/json")
            .runCaptureOutput()
            .toString();

        log.info("Quote of the day JSON is {}", output);
    };
    
}
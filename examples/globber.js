/* global Packages, Contexts, Globber */

// nashorn recommended method of importing classes
var Imports = new JavaImporter(
    Packages.org.slf4j.Logger,
    Packages.com.fizzed.blaze.Contexts,
    Packages.com.fizzed.blaze.util.Globber);
    
with (Imports) {
    
    var log = Contexts.logger();
    var globber = Globber.globber;

    var main = function() {
        globber(Contexts.baseDir(), "*.{java,js,groovy,kt,kts}")
            .filesOnly()
            .visibleOnly()
            .scan().stream().forEach(function(p) {
                log.info("{}", p);
            });
    };
    
}
#!/usr/bin/env blaze

import com.fizzed.blaze.Contexts
import io.undertow.Undertow
import io.undertow.server.HttpHandler
import io.undertow.server.HttpServerExchange
import io.undertow.util.Headers
import io.undertow.server.handlers.resource.PathResourceManager
import java.nio.file.Paths
import static io.undertow.Handlers.resource

def main() {
    def dir = Contexts.baseDir()
    def log = Contexts.logger()
    def config = Contexts.config()
    
    def host = config.value("undertow.host").get()
    def port = config.value("undertow.port", Integer.class).get()
    def in_try_all_example = config.value("examples.try_all", Boolean.class).getOr(false)
    
    def undertow = Undertow.builder()
        .addHttpListener(port, host)
        .setHandler(resource(new PathResourceManager(dir, 100)).setDirectoryListingEnabled(true))
        .build()
       
    undertow.start()
    
    log.info("Open browser to http://{}:{}", host, port)
    
    if (in_try_all_example) {
        // simply for stopping server if we're in try_all example
        undertow.stop();
    } else {
        synchronized (undertow) {
            undertow.wait();
        }
    }
}
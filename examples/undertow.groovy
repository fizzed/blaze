import static com.fizzed.blaze.Contexts.*
import io.undertow.Undertow
import io.undertow.server.HttpHandler
import io.undertow.server.HttpServerExchange
import io.undertow.util.Headers
import io.undertow.server.handlers.resource.PathResourceManager
import java.nio.file.Paths
import static io.undertow.Handlers.resource

def main() {
    //def dir = Paths.get(System.getProperty("user.home"))
    def dir = baseDir().toPath()
    
    def undertow = Undertow.builder()
        .addHttpListener(8080, "localhost")
        .setHandler(resource(new PathResourceManager(dir, 100))
            .setDirectoryListingEnabled(true))
        .build()
       
    undertow.start()
    
    log.info("Open browser to http://localhost:8080")
    
    synchronized (undertow) {
        undertow.wait();
    }
}
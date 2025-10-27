import com.fizzed.blaze.Config;
import com.fizzed.blaze.Contexts;
import io.undertow.Undertow;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.Headers;
import io.undertow.server.handlers.resource.PathResourceManager;
import static io.undertow.Handlers.resource;
import java.nio.file.Path;
import org.slf4j.Logger;

public class undertow {
    final private Logger log = Contexts.logger();
    final private Config config = Contexts.config();

    public void main() throws Exception {
        // simple for skipping this example in try_all.java
        boolean in_try_all_example = config.flag("examples-try-all").getOr(false);

        Path dir = Contexts.baseDir();
        String host = config.value("undertow.host").get();
        int port = config.value("undertow.port", int.class).get();

        Undertow undertow = Undertow.builder()
            .addHttpListener(port, host)
            .setHandler(resource(new PathResourceManager(dir, 100)).setDirectoryListingEnabled(true))
            .build();

        undertow.start();

        log.info("Open browser to http://{}:{}", host, port);

        if (in_try_all_example) {
            // simply for stopping server if we're in try_all example
            undertow.stop();
        } else {
            synchronized (undertow) {
                undertow.wait();
            }
        }
    }
    
}
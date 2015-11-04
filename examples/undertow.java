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

    public void main() throws Exception {
        Path dir = Contexts.baseDir();
        Logger log = Contexts.logger();
        Config config = Contexts.config();

        String host = config.value("undertow.host").get();
        int port = config.value("undertow.port", int.class).get();
        boolean in_try_all_example = config.value("examples.try_all", Boolean.class).getOr(false);

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
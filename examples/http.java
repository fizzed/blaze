import org.slf4j.Logger;
import com.fizzed.blaze.Contexts;
import com.fizzed.blaze.util.MutableUri;
import java.net.URI;
import static com.fizzed.blaze.Https.*;

public class http {
    static final private Logger log = Contexts.logger();

    public void main() throws Exception {
        URI uri = MutableUri.of("http://jsonplaceholder.typicode.com/comments")
            .query("postId", 1)
            .toURI();
        
        String output = httpGet(uri.toString())
            .addHeader("Accept", "application/json")
            .runCaptureOutput()
            .toString();
        
        log.info("Quote of the day JSON is {}", output);
    }
    
}
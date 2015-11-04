import org.slf4j.Logger;
import com.fizzed.blaze.Contexts;
import com.fizzed.blaze.util.MutableUri;
import java.net.URI;
import org.apache.http.client.fluent.Request;

public class http {
    static final private Logger log = Contexts.logger();

    public void main() throws Exception {
        URI uri = MutableUri.of("http://jsonplaceholder.typicode.com/comments")
            .query("postId", 1)
            .toURI();
        
        String output = 
            Request.Get(uri)
                .addHeader("Accept", "application/json")
                .execute()
                .returnContent()
                .toString();
        
        log.info("Quote of the day JSON is {}", output);
    }
    
}
Blaze by Fizzed
=======================================

## HTTP

Add the following to your `blaze.conf` file to include rich support for HTTP get/post/delete/etc. Significantly
enhanced over any built-in JDK http support.  You do not want to specify a version so Blaze will resolve the identical
version to whatever `blaze.jar` you're running with.

```
blaze.dependencies = [
    "com.fizzed:blaze-http"
]
```

Here is an example of getting a URL, capturing all of its output to a String, and then printing it out.

```java
import org.slf4j.Logger;
import com.fizzed.blaze.Contexts;
import com.fizzed.blaze.util.MutableUri;
import java.net.URI;
import static com.fizzed.blaze.Https.*;

public class http {
    static final private Logger log = Contexts.logger();

    public void main() throws Exception {
        String url = MutableUri.of("http://jsonplaceholder.typicode.com/comments")
            .query("postId", 1)
            .toString();
        
        String output = httpGet(url)
            .verbose()
            .progress()
            .addHeader("Accept", "application/json")
            .runCaptureOutput()
            .toString();
        
        log.info("Quote of the day JSON is {}", output);
    }
    
}
```
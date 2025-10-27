Blaze by Fizzed
=======================================

## Archive

Add the following to your `blaze.conf` file to include rich support for working with archives such as .zip, .bz2, .xz, etc.
Significantly enhanced over any built-in JDK archive support.  You do not want to specify a version so Blaze will resolve the identical
version to whatever `blaze.jar` you're running with.

```
blaze.dependencies = [
    "com.fizzed:blaze-archive"
]
```

Here is an example of getting a URL, capturing all of its output to a String, and then printing it out.

```java
import org.slf4j.Logger;
import com.fizzed.blaze.Contexts;
import java.nio.file.Path;
import java.nio.file.Paths;
import static com.fizzed.blaze.Archives.*;

public class http {
    static final private Logger log = Contexts.logger();

    public void main() throws Exception {
        Path archiveFile = Paths.get("archive.zip");
        Path targetDir = Paths.get("target");
        
        unarchive(archiveFile)
            .verbose()
            .progress()
            .useTemporaryFiles()
            .target(targetDir)
            .force()
            .run();
    }
    
}
```
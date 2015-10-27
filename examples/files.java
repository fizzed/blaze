import java.nio.Files;
import java.nio.file.Paths;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class files {
    static final private Logger log = LoggerFactory.getLogger("script");

    public void ls() throws Exception {
        Files.walkFileTree(Paths.get("."), (v) -> {
            v.
        });
    }
    
}

import org.slf4j.Logger;
import static com.fizzed.blaze.Systems.which;
import static com.fizzed.blaze.Systems.exec;
import com.fizzed.blaze.Contexts;
import java.nio.file.Path;

public class javac {
    static final private Logger log = Contexts.logger();

    public void main() {
        log.info("Finding javac...");
        Path javacFile = which("javac").run();

        log.info("Using javac {}", javacFile);
        exec("javac", "-version").run();
    }
    
}
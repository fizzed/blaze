import org.slf4j.Logger;
import com.fizzed.blaze.Contexts;
import static com.fizzed.blaze.util.Globber.globber;

public class globber {
    static private final Logger log = Contexts.logger();

    public void main() throws Exception {
        globber(Contexts.baseDir(), "*.{java,js,groovy,kt,kts}")
            .filesOnly()
            .visibleOnly()
            .scan().stream().forEach((p) -> {
                log.info("{}", p);
            });
    }
    
}
import com.fizzed.blaze.Contexts;
import org.unix4j.Unix4j;
import org.slf4j.Logger;
import org.unix4j.unix.Ls;

public class grep {
    static final private Logger log = Contexts.logger();

    public void main() throws Exception {
        log.info("Demo of Unix4j tool for \"grep\" support");

        Unix4j.cat(Contexts.withBaseDir("../README.md").toFile()).grep("blaze-lite").toLineList().forEach(s -> {
            log.info("found: {}", s.toString().trim());
        });
    }
    
}

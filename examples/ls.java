import com.fizzed.blaze.Contexts;
import org.slf4j.Logger;
import org.unix4j.Unix4j;
import org.unix4j.unix.Ls;

public class ls {
    static final private Logger log = Contexts.logger();

    public void main() throws Exception {
        log.info("Demo of Unix4j tool for \"ls\" support");

        Unix4j.ls(Ls.Options.l.a, Contexts.withBaseDir("../").toAbsolutePath().toFile()).toLineList().forEach(s -> {
            log.info("found: {}", s.toString().trim());
        });
    }
    
}

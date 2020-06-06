
import com.fizzed.blaze.Config;
import com.fizzed.blaze.Contexts;
import org.slf4j.Logger;

public class prompt {
    final private Logger log = Contexts.logger();
    final private Config config = Contexts.config();
    
    public void main() throws Exception {
        
        String s = null;
            
        s = Contexts.prompt()
            .options("yes", "no")
            .prompt("No default prompt [yes|no]: ");
        
        log.info("Answer: {}", s);
        
        s = Contexts.prompt()
            .options("yes", "no")
            .defaultOption("no")
            .prompt("Answer or default no [yes|no]: ");
        
        log.info("Answer: {}", s);
        
        s = Contexts.prompt()
            .masked(true)
            .prompt("Enter a fake password: ");
        
        log.info("Answer: {}", s);
    }
    
}

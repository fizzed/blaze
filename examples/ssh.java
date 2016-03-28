
import com.fizzed.blaze.Config;
import com.fizzed.blaze.Contexts;
import static com.fizzed.blaze.Contexts.prompt;
import com.fizzed.blaze.util.MutableUri;
import com.fizzed.blaze.ssh.SshSession;
import org.slf4j.Logger;
import static com.fizzed.blaze.SecureShells.sshConnect;
import static com.fizzed.blaze.SecureShells.sshExec;
import com.fizzed.blaze.util.CaptureOutput;
import com.fizzed.blaze.util.Streamables;

public class ssh {
    final private Logger log = Contexts.logger();
    final private Config config = Contexts.config();
    
    public void main() throws Exception {
        // simple for skipping this example in try_all.java
        boolean in_try_all_example = config.value("examples.try_all", Boolean.class).getOr(false);
        
        if (in_try_all_example) {
            return;
        }
        
        // get or prompt for uri to ssh to
        MutableUri uri = config.value("ssh.uri", MutableUri.class).getOrNull();
        
        if (uri == null) {
            String s = prompt("Enter ssh uri (e.g. ssh://user@host)> ");
            uri = MutableUri.of(s);
        }
        
        try (SshSession session = sshConnect(uri).run()) {
            // remote working directory
            CaptureOutput capture = Streamables.captureOutput();
            
            sshExec(session)
                .command("pwd")
                .pipeOutput(capture)
                .run();
            log.info("Remote working dir is {}", capture.toString().trim());
            
            // who are we logged in as?
            capture = Streamables.captureOutput();
            
            sshExec(session)
                .command("whoami")
                .pipeOutput(capture)
                .run();
            
            log.info("Logged in as {}", capture.toString().trim());
            
            log.info("Listing current directory...");
            
            // list directory to stdout
            Integer exitValue
                = sshExec(session)
                    .command("ls")
                    .arg("-la")
                    .run();
            
            log.info("Last exec exited with {}", exitValue);
        }
    }
    
}

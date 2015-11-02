
import com.fizzed.blaze.Config;
import com.fizzed.blaze.Contexts;
import static com.fizzed.blaze.Contexts.prompt;
import com.fizzed.blaze.util.MutableUri;
import com.fizzed.blaze.ssh.SshExecResult;
import com.fizzed.blaze.ssh.SshSession;
import org.slf4j.Logger;
import static com.fizzed.blaze.SecureShells.sshConnect;
import static com.fizzed.blaze.SecureShells.sshExec;

public class ssh {
    final private Logger log = Contexts.logger();
    final private Config config = Contexts.config();
    
    public void main() throws Exception {
        // simple for skipping this example in try_all.java
        boolean in_try_all_example = config.find("examples.try_all", Boolean.class).or(false);
        
        if (in_try_all_example) {
            return;
        }
        
        // get or prompt for uri to ssh to
        MutableUri uri = config.find("ssh.uri", MutableUri.class).or(null);
        
        if (uri == null) {
            String s = prompt("Enter ssh uri (e.g. ssh://user@host)> ");
            uri = MutableUri.of(s);
        }
        
        try (SshSession session = sshConnect(uri).run()) {
            // remote working directory
            SshExecResult result0
                = sshExec(session)
                    .command("pwd")
                    .captureOutput()
                    .run();
            
            log.info("Remote working dir is {}", result0.output().trim());
            
            // who are we logged in as?
            SshExecResult result1
                = sshExec(session)
                    .command("whoami")
                    .captureOutput()
                    .run();
            
            log.info("Logged in as {}", result1.output().trim());
            
            log.info("Listing current directory...");
            
            // list directory to stdout
            SshExecResult result2
                = sshExec(session)
                    .command("ls")
                    .arg("-la")
                    .run();
            
            log.info("Last exec exited with {}", result2.exitValue());
        }
    }
    
}

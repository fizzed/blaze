
import com.fizzed.blaze.Config;
import com.fizzed.blaze.Contexts;
import static com.fizzed.blaze.Contexts.prompt;
import com.fizzed.blaze.util.MutableUri;
import com.fizzed.blaze.ssh.SshSession;
import org.slf4j.Logger;
import static com.fizzed.blaze.SecureShells.sshConnect;
import static com.fizzed.blaze.SecureShells.sshSftp;
import com.fizzed.blaze.ssh.SshFileAttributes;
import com.fizzed.blaze.ssh.SshSftpSession;
import java.nio.file.Path;
import java.nio.file.attribute.PosixFilePermissions;

public class sftp {
    static final private Logger log = Contexts.logger();
    static final private Config config = Contexts.config();

    public void main() throws Exception {
        // simple for skipping this example in try_all.java
        boolean in_try_all_example = config.flag("examples-try-all").getOr(false);
        if (in_try_all_example) {
            log.info("Skipping example in try_all.java");
            return;
        }

        // get or prompt for uri to sftp to
        MutableUri uri = config.value("ssh.uri", MutableUri.class).getOrNull();

        if (uri == null) {
            String s = prompt("Enter ssh uri (e.g. ssh://user@host)> ");
            uri = MutableUri.of(s);
        }

        try (SshSession session = sshConnect(uri).run()) {

            try (SshSftpSession sftp = sshSftp(session).run()) {

                Path pwd = sftp.pwd();

                log.info("Remote working dir is {}", pwd);

                // get file attributes for current working dir
                SshFileAttributes attrs = sftp.lstat(pwd);

                log.info("{} with permissions {}", pwd, PosixFilePermissions.toString(attrs.permissions()));

                sftp.ls(pwd)
                        .stream()
                        .forEach((file) -> {
                            log.info("{} {} at {}", file.attributes().lastModifiedTime(), file.path(), file.attributes().size());
                        });

                /**
                 * sftp.put()
                 *  .source("my/source/file.txt")
                 *  .target("file.txt")
                 *  .run();
                 */
                /**
                 * sftp.get()
                 *  .source("file.txt")
                 *  .target("my/target/file.txt")
                 *  .run();
                 */
                
                //sftp.symlink("blaze.jar", "blaze.jar.lnk");
                
                // many more methods in sftp class...
            }
        }
    }
}

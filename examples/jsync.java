import com.fizzed.blaze.Config;
import com.fizzed.blaze.Contexts;
import com.fizzed.blaze.core.Verbosity;
import com.fizzed.jsync.engine.JsyncMode;
import com.fizzed.jsync.vfs.VirtualVolume;
import org.slf4j.Logger;

import java.nio.file.Paths;
import java.util.NoSuchElementException;

import static com.fizzed.blaze.jsync.Jsyncs.*;

public class jsync {
    final private Logger log = Contexts.logger();
    final private Config config = Contexts.config();

    public void main() throws Exception {
        // simple for skipping this example in try_all.java
        boolean in_try_all_example = config.flag("examples-try-all").getOr(false);
        if (in_try_all_example) {
            log.info("Skipping example in try_all.java");
            return;
        }

        try {
            final String from = this.config.value("from").get();
            final String to = this.config.value("to").get();
            final String modeStr = this.config.value("mode").get();
            final JsyncMode mode = JsyncMode.valueOf(modeStr.toUpperCase());
            final boolean verbose = this.config.flag("no-verbose").orElse(true);
            final boolean progress = this.config.flag("no-progress").orElse(true);
            final boolean parents = this.config.flag("parents").orElse(false);
            final boolean force = this.config.flag("force").orElse(false);
            final boolean delete = this.config.flag("delete").orElse(false);

            final VirtualVolume fromVolume = this.detectVolume(from);
            final VirtualVolume toVolume = this.detectVolume(to);

            jsync(fromVolume, toVolume, mode)
                .verbosity(verbose ? Verbosity.VERBOSE : Verbosity.DEFAULT)
                .progress(progress)
                .parents(parents)
                .force(force)
                .delete(delete)
                .run();
        } catch (NoSuchElementException e) {
            this.printUsage();
            System.exit(1);
        }
    }

    public void printUsage() {
        System.out.println("Usage: blaze examples/jsync.java --from <from> --to <to> [--mode <mode>] [--no-verbose] [--no-progress] [--parents] [--force] [--delete]");
        System.out.println();
        System.out.println("For SSH/SFTP volumes, use the format host:path OR user@host:path - path can be an empty string which will be the home directory of the user");
        System.out.println();
        System.out.println("Options:");
        System.out.println("--from <from>                local directory or SSH volume (user@host:path)");
        System.out.println("--to <to>                    local directory or SSH volume (user@host:path)");
        System.out.println("--mode <mode>                nest or merge");
        System.out.println("--no-verbose                 disable verbose output");
        System.out.println("--no-progress                disable progress output");
        System.out.println("--parents                    enable creating parent directories on target if they don't exist");
        System.out.println("--force                      enable updating target file/dir type mismatches if they exist");
        System.out.println("--delete                     delete dirs/files on target if they don't exist on source");
        System.out.println();
        System.out.println("Examples:");
        System.out.println("blaze examples/jsync.java --from /tmp/from --to /tmp/to");
        System.out.println("blaze examples/jsync.java --from user@host:/tmp/from --to /tmp/to");
    }

    public VirtualVolume detectVolume(String volume) {
        int colonPos = volume.indexOf(":");

        if (colonPos >= 0) {
            // this may be SSH, although it could also be a windows path
            String host = volume.substring(0, colonPos);
            if (host.length() > 2) {
                // assume this is an SSH volume
                final String pathStr = volume.substring(colonPos+1);
                final String path = pathStr.isEmpty() ? "." : pathStr;
                return sftpVolume(host, path);
            }
        }

        // otherwise, assume it's local
        return localVolume(Paths.get(volume));
    }
    
}

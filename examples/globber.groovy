import org.slf4j.Logger
import com.fizzed.blaze.Contexts
import static com.fizzed.blaze.util.Globber.globber

log = Contexts.logger()

def main() {
    globber(Contexts.baseDir(), "*.{java,js,groovy,kt,kts}")
        .filesOnly()
        .visibleOnly()
        .scan().stream().forEach { p ->
            log.info("{}", p)
        }
}
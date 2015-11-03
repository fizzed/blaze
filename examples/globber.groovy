import org.slf4j.Logger
import com.fizzed.blaze.Contexts
import static com.fizzed.blaze.util.Globber.globber

log = Contexts.logger()

def main() {
    globber("*")
        .filesOnly()
        .visibleOnly()
        .scan().stream().forEach { p ->
            log.info("{}", p)
        }
}
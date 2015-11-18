import org.slf4j.Logger;
import com.fizzed.blaze.Contexts;
import static com.fizzed.blaze.Systems.pipeline;
import static com.fizzed.blaze.Systems.tail;
import com.fizzed.blaze.util.NamedStream;
import java.nio.file.Paths;

public class pipeline {
    static final private Logger log = Contexts.logger();

    public void main() {
        pipeline()
            //.add(exec("java", "-version").pipeErrorToOutput())
            .add(tail(3)
                    .pipeInput(Paths.get("/etc/passwd"))
                    .pipeOutput(NamedStream.standardOutput()))
            .run();
    }
    
}
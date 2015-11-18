import org.slf4j.Logger;
import com.fizzed.blaze.Contexts;
import static com.fizzed.blaze.Contexts.withBaseDir;
import static com.fizzed.blaze.Systems.exec;
import static com.fizzed.blaze.Systems.pipeline;
import static com.fizzed.blaze.Systems.tail;
import com.fizzed.blaze.util.NamedStream;
import java.nio.file.Paths;

public class pipeline {
    static final private Logger log = Contexts.logger();

    public void main() {
        pipeline()
            .add(exec("java", "-version")
                .pipeOutput(NamedStream.nullOutput())
                .pipeErrorToOutput()
            )
            .add(tail(1)
                .pipeInput(withBaseDir("pipeline.txt"))
                .pipeOutput(NamedStream.standardOutput())
            )
            .run();
    }
    
}
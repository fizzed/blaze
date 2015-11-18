import org.slf4j.Logger;
import com.fizzed.blaze.Contexts;
import static com.fizzed.blaze.Contexts.withBaseDir;
import static com.fizzed.blaze.Systems.head;
import static com.fizzed.blaze.Systems.pipeline;
import static com.fizzed.blaze.Systems.tail;
import com.fizzed.blaze.util.CaptureOutput;
import com.fizzed.blaze.util.Streamables;

public class pipeline {
    static final private Logger log = Contexts.logger();

    public void main() {
        CaptureOutput capture = Streamables.captureOutput();
        
        pipeline()
            .pipeInput(withBaseDir("pipeline.txt"))
            .add(head(3))
            .add(tail(2))
            .pipeOutput(capture)
            .run();
        
        log.info("{}", capture);
    }
    
}
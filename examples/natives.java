import com.fizzed.blaze.Contexts;
import com.fizzed.jne.NativeTarget;
import org.slf4j.Logger;

public class natives {
    static final private Logger log = Contexts.logger();

    public void main() throws Exception {
        final NativeTarget nativeTarget = NativeTarget.detect();

        log.info("Detected native target:");
        log.info(" os: {}", nativeTarget.getOperatingSystem());
        log.info(" arch: {}", nativeTarget.getHardwareArchitecture());
        log.info(" abi: {}", nativeTarget.getAbi());
        log.info(" jneTarget: {}", nativeTarget.toJneTarget());
        log.info(" rustTarget: {}", nativeTarget.toRustTarget());
    }
    
}

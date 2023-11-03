import com.fizzed.blaze.Contexts;
import org.slf4j.Logger;
import com.fizzed.jne.*;

import java.util.List;

public class find_java {

    final private Logger log = Contexts.logger();

    public void main() throws Exception {
        log.info("Finding all JVMs on your system...");

        final List<JavaHome> javaHomes = JavaHomes.detect();

        for (JavaHome javaHome : javaHomes) {
            log.info("{}", javaHome);
            log.info("  javaExe: {}", javaHome.getJavaExe());
            log.info("  javacExe: {}", javaHome.getJavacExe());
            log.info("  nativeImageExe: {}", javaHome.getNativeImageExe());
            log.info("  imageType: {}", javaHome.getImageType());
            log.info("  version: {}", javaHome.getVersion());
            log.info("       major: {}", javaHome.getVersion().getMajor());
            log.info("       minor: {}", javaHome.getVersion().getMinor());
            log.info("    security: {}", javaHome.getVersion().getSecurity());
            log.info("  os: {}", javaHome.getOperatingSystem());
            log.info("  arch: {}", javaHome.getHardwareArchitecture());
            log.info("  distro: {}", javaHome.getDistribution());
            log.info("  vendor: {}", javaHome.getVendor());
        }

        // now, let's use the JavaHomeFinder to narrow our match to something awesome
        final JavaHome jdk = new JavaHomeFinder()
            .jdk()
            .maxVersion(21)
            .minVersion(8)
            .preferredDistributions()
            .sorted()
            .find();

        log.info("Found JVM {}", jdk);
    }
    
}

import com.fizzed.blaze.Contexts;
import static com.fizzed.blaze.Contexts.baseDir;
import static com.fizzed.blaze.Systems.exec;
import static com.fizzed.blaze.util.Globber.globber;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.atomic.AtomicInteger;
import org.slf4j.Logger;

public class try_all {
    private final Logger log = Contexts.logger();

    public void main() throws Exception {
        // find blaze jar to use (same one that is on our classpath)
        final Path blazeJarFile = findBlazeJarOnClassPath();
        
        if (blazeJarFile == null) {
            Contexts.fail("Unable to find blaze.jar on classpath");
        }
        
        final AtomicInteger count = new AtomicInteger();
        
        // use globber to find files to run
        globber(baseDir(), "*.{java,groovy,js,kt,kts}")
            .exclude("try_all.java")
            .stream()
            .sorted()
            .forEach((p) -> {
                log.info("Trying {}", p);
                exec("java", "-Dexamples.try_all=true", "-jar", blazeJarFile, "-f", p).run();
                count.incrementAndGet();
            });
        
        log.info("Ran {} examples!", count);
    }
    
    private Path findBlazeJarOnClassPath() throws URISyntaxException {
        // does the jar already exist on claspath?
        URLClassLoader urlClassLoader = (URLClassLoader)Thread.currentThread().getContextClassLoader();

        for (URL u : urlClassLoader.getURLs()) {
            Path loadedJarFile = Paths.get(u.toURI());
            String loadedJarName = loadedJarFile.getFileName().toString();
            if (loadedJarName.startsWith("blaze") && loadedJarName.endsWith(".jar")) {
                log.info("Using blaze jar {}", loadedJarFile);
                return loadedJarFile;
            }
        }
        
        return null;
    }
    
}
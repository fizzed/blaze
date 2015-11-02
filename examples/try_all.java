import com.fizzed.blaze.Contexts;
import com.fizzed.blaze.Systems;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.slf4j.Logger;

public class try_all {
    private final Logger log = Contexts.logger();

    public void main() throws Exception {
        final Path examplesDir = Contexts.baseDir();

        // find blaze jar to use (same one that is on our classpath)
        final Path blazeJarFile = findBlazeJarOnClassPath();
        
        if (blazeJarFile == null) {
            Contexts.fail("Unable to find blaze.jar on classpath");
        }
        
        // find all non .conf files and try 'em out
        Files.list(examplesDir)
            .filter((f) -> !f.getFileName().toString().equals("try_all.java"))
            .filter((f) -> !f.getFileName().toString().endsWith(".conf"))
            .sorted()
            .forEach((f) -> {
                log.info("Trying {}", f);
                Systems.exec("java", "-Dexamples.try_all=true", "-jar", blazeJarFile, "-f", f).run();
            });
    }
    
    private Path findBlazeJarOnClassPath() throws URISyntaxException {
        // does the jar already exist on claspath?
        URLClassLoader urlClassLoader = (URLClassLoader)Thread.currentThread().getContextClassLoader();

        for (URL u : urlClassLoader.getURLs()) {
            Path loadedJarFile = Paths.get(u.toURI());
            String loadedJarName = loadedJarFile.getFileName().toString();
            if (loadedJarName.startsWith("blaze-") && loadedJarName.endsWith(".jar")) {
                log.info("Using blaze jar {}", loadedJarFile);
                return loadedJarFile;
            }
        }
        
        return null;
    }
    
}
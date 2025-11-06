package com.fizzed.blaze.maven;

import java.io.IOException;
import java.nio.file.Paths;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.fizzed.blaze.Systems.exec;
import static com.fizzed.blaze.maven.MavenProjects.mavenClasspath;

public class Demo {
    static private final Logger log = LoggerFactory.getLogger(Demo.class);
    
    static public void main(String[] args) throws InterruptedException, IOException {
        final MavenProject mavenProject = MavenProjects.mavenProject(Paths.get("pom.xml"))
            .run();

        final MavenClasspath classpath = mavenClasspath(mavenProject, "test", "test-compile", "blaze-maven")
            .run();

        exec("java", "-cp", classpath, "com.fizzed.blaze.maven.HelloDemo")
            .verbose()
            .run();
    }
    
}

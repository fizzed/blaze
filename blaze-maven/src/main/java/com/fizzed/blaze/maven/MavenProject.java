package com.fizzed.blaze.maven;

import java.nio.file.Path;

public class MavenProject {

    private final Path pomFile;

    public MavenProject(Path pomFile) {
        this.pomFile = pomFile;
    }

    public Path getPomFile() {
        return pomFile;
    }

}
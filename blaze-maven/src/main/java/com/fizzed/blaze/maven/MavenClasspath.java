package com.fizzed.blaze.maven;

import java.io.File;
import java.nio.file.Path;
import java.util.List;

import static java.util.stream.Collectors.joining;

public class MavenClasspath {

    private final List<Path> paths;

    public MavenClasspath(List<Path> paths) {
        this.paths = paths;
    }

    public List<Path> getPaths() {
        return paths;
    }

    public String toString() {
        return this.paths.stream().map(Path::toString).collect(joining(File.pathSeparator));
    }

}
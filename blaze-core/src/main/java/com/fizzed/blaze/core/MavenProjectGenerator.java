package com.fizzed.blaze.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;

public class MavenProjectGenerator {
    static private final Logger log = LoggerFactory.getLogger(MavenProjectGenerator.class);

    private Blaze blaze;
    private Path pomFile;

    public Blaze getBlaze() {
        return blaze;
    }

    public MavenProjectGenerator setBlaze(Blaze blaze) {
        this.blaze = blaze;
        return this;
    }

    public Path getPomFile() {
        return pomFile;
    }

    public MavenProjectGenerator setPomFile(Path pomFile) {
        this.pomFile = pomFile;
        return this;
    }

    static public String fromIvyToMavenVersion(String ivyVersion) {
        if (ivyVersion.equalsIgnoreCase("latest.integration")) {
            return "LATEST";
        }
        if (ivyVersion.equalsIgnoreCase("latest.release")) {
            return "RELEASE";
        }
        return ivyVersion;
    }

    static public List<Path> detectProjectToScriptPaths(Path scriptFile) throws IOException {
        final List<Path> paths = new ArrayList<>();

        // figure out the project directory we are running in
        // we will iterate thru the "parent" of the script to search for well known conventions
        int i = 0;
        Path dir = scriptFile.toAbsolutePath().getParent();
        while (i < 5 && dir != null) {
            // always push the current dir on front
            paths.add(0, dir);

            // is this directory named ".blaze" or "blaze"
            String dirName = dir.getFileName().toString();
            if (dirName.equals(".blaze") || dirName.equals("blaze")) {
                // project dir will be just 1 level up, and we're done
                paths.add(0, dir.getParent());
                break;
            }

            // does a "blaze.jar" exist in it?
            if (Files.exists(dir.resolve("blaze.jar"))) {
                // current dir IS the project dir (and it was already added above)
                break;
            }

            // does a ".git" exist in it?
            if (Files.exists(dir.resolve(".git"))) {
                // current dir IS the project dir (and it was already added above)
                break;
            }

            i++;
            dir = dir.getParent();
        }

        return paths;
    }

    public void generate() throws IOException {
        final Path scriptFile = this.blaze.context().scriptFile().toAbsolutePath();

        // control the source and target we put into the maven project
        final String sourceVersion = this.blaze.context().config().value("maven.project.source.version").orElse("8");
        final String targetVersion = this.blaze.context().config().value("maven.project.target.version").orElse("8");

        // is the pomFile provided or should we calculate it?
        if (this.pomFile == null) {
            this.pomFile = scriptFile.getParent().resolve("pom.xml");
        }

        final List<Dependency> dependencies = this.blaze.dependencies();

        log.info("Generating maven project {} file...", this.pomFile);

        // figure out the project directory hierarchy the script is in
        log.info("Detecting the \"project\" this script is a part of (using conventions of where blaze scripts typically reside)...");
        final List<Path> projectToScriptPaths = detectProjectToScriptPaths(scriptFile);
        for (Path dir : projectToScriptPaths) {
            log.info("  {}", dir.toAbsolutePath());
        }
        log.info("  {}", scriptFile);

        // name of the "blaze" project is a combo of the directory hierarchy
        // e.g. if this is projectA/.blaze/blaze.java then the name would be projectA-dotblaze
        final StringBuilder projectNameBuilder = new StringBuilder();
        for (Path dir : projectToScriptPaths) {
            if (projectNameBuilder.length() > 0) {
                projectNameBuilder.append("-");
            }
            // not safe to have periods in the name
            projectNameBuilder.append(dir.getFileName().toString().replace(".", "").replace(" ", "_"));
        }

        // if there was only 1 directory, this must be a blaze.java in the main directory, we'll add a "blaze" on it
        if (projectToScriptPaths.size() == 1) {
            projectNameBuilder.append("-blaze");
        }

        final String projectName = projectNameBuilder.toString();

        log.info("Calculated project name to \"{}\"", projectName);

        final StringBuilder sb = new StringBuilder();
        sb.append("<project xmlns=\"http://maven.apache.org/POM/4.0.0\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:schemaLocation=\"http://maven.apache.org/POM/4.0.0  http://maven.apache.org/xsd/maven-4.0.0.xsd\">\n");
        sb.append("  <modelVersion>4.0.0</modelVersion>\n");
        sb.append("  <groupId>blaze</groupId>\n");
        sb.append("  <artifactId>"+projectName+"</artifactId>\n");
        sb.append("  <version>0.0.1</version>\n");
        sb.append("\n");
        sb.append("  <!--\n");
        sb.append("  THIS FILE IS AUTOMATICALLY GENERATED. DO NOT EDIT BY HAND!\n");
        sb.append("\n");
        sb.append("  Edit or create a <blaze-script>.conf file, and re-run the generate-maven-project command.\n");
        sb.append("  -->\n");
        sb.append("\n");
        sb.append("  <properties>\n");
        sb.append("    <maven.compiler.source>" + sourceVersion + "</maven.compiler.source>\n");
        sb.append("    <maven.compiler.target>" + targetVersion + "</maven.compiler.target>\n");
        sb.append("    <maven.install.skip>true</maven.install.skip>\n");
        sb.append("    <maven.deploy.skip>true</maven.deploy.skip>\n");
        sb.append("  </properties>\n");
        sb.append("  <build>\n");
        sb.append("    <sourceDirectory>${project.basedir}</sourceDirectory>\n");
        sb.append("  </build>\n");
        sb.append("  <dependencies>\n");
        for (Dependency dependency : dependencies) {
            sb.append("    <dependency>\n");
            sb.append("      <groupId>" + dependency.getGroupId() + "</groupId>\n");
            sb.append("      <artifactId>" + dependency.getArtifactId() + "</artifactId>\n");
            sb.append("      <version>" + fromIvyToMavenVersion(dependency.getVersion()) + "</version>\n");
            sb.append("    </dependency>\n");
        }
        sb.append("  </dependencies>\n");
        sb.append("</project>");

        try (BufferedWriter output = Files.newBufferedWriter(this.pomFile, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)) {
            output.write(sb.toString());
            output.flush();
        }

        log.info("Done. Wrote maven project file {}", this.pomFile);
    }

}
package com.fizzed.blaze.maven;

import com.fizzed.blaze.Context;
import com.fizzed.blaze.core.Action;
import com.fizzed.blaze.core.BlazeException;
import com.fizzed.blaze.core.VerbosityMixin;
import com.fizzed.blaze.system.Exec;
import com.fizzed.blaze.util.VerboseLogger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static com.fizzed.blaze.Systems.exec;
import static java.util.Arrays.asList;

public class MavenClasspathResolver extends Action<MavenClasspathResolver.Result,MavenClasspath> implements VerbosityMixin<MavenClasspathResolver> {

    static public class Result extends com.fizzed.blaze.core.Result<MavenClasspathResolver,MavenClasspath, Result> {

        public Result(MavenClasspathResolver action, MavenClasspath cp) {
            super(action, cp);
        }

    }

    static public final String HOOKS_MAVEN_PLUGIN = "com.fizzed:hooks-maven-plugin:2.0.0";

    protected  final MavenProject mavenProject;
    protected final VerboseLogger log;
    protected String scope;                     // e.g. compile, runtime, test
    protected Collection<String> phases;         // e.g. compile, test-compile, package, etc.
    protected String module;                        // target module (if in a multi-module project)
    protected boolean alsoMakeDependants;           // if in multi-module project, also include dependants too
    protected List<String> arguments;

    public MavenClasspathResolver(Context context, MavenProject mavenProject) {
        super(context);
        this.mavenProject = mavenProject;
        this.log = new VerboseLogger(this);
        this.phases = Collections.singletonList("compile");
        this.scope = "runtime";
        this.module = null;
        this.alsoMakeDependants = true;
    }

    @Override
    public VerboseLogger getVerboseLogger() {
        return this.log;
    }

    /**
     * Sets the scope for the MavenClasspath action. The scope determines
     * the classpath scope, such as "compile", "runtime", or "test",
     * which is used during execution.
     *
     * @param scope the scope to set, e.g., "compile", "runtime", or "test"
     * @return the current instance of MavenJavaExec for method chaining
     */
    public MavenClasspathResolver scope(String scope) {
        this.scope = scope;
        return this;
    }

    /**
     * Configures the Maven phases to be executed by this action. These phases are
     * part of the Maven lifecycle, such as "compile", "test-compile", or "package".
     *
     * @param phases the phases to set, specified as varargs (e.g., "compile", "test-compile", "package")
     * @return the current instance of MavenJavaExec for method chaining
     */
    public MavenClasspathResolver phases(String... phases) {
        this.phases = asList(phases);
        return this;
    }

    /**
     * Sets the Maven phases to be executed in this action. Phases are part of the Maven
     * build lifecycle, such as "compile", "test-compile", "package", etc.
     *
     * @param phases the collection of Maven phases to set
     * @return the current instance of MavenJavaExec for method chaining
     */
    public MavenClasspathResolver phases(Collection<String> phases) {
        this.phases = phases;
        return this;
    }

    /**
     * Specifies the module name to be used in the MavenClasspath configuration.
     * The module name typically corresponds to a specific Maven project module.
     *
     * @param module the name of the module to set
     * @return the current instance of MavenClasspath for method chaining
     */
    public MavenClasspathResolver module(String module) {
        this.module = module;
        return this;
    }

    /**
     * Configures whether the dependent Maven modules should also be built
     * when executing this MavenClasspath action.
     *
     * @param alsoMakeDependants a boolean flag indicating whether dependent Maven modules
     *                           should also be built (true) or not (false)
     * @return the current instance of MavenClasspath for method chaining
     */
    public MavenClasspathResolver alsoMakeDependants(boolean alsoMakeDependants) {
        this.alsoMakeDependants = alsoMakeDependants;
        return this;
    }

    public MavenClasspathResolver args(String... arguments) {
        return this.args(asList(arguments));
    }

    public MavenClasspathResolver args(List<String> arguments) {
        if (this.arguments == null) {
            this.arguments = new ArrayList<>();
        }
        this.arguments.addAll(arguments);
        return this;
    }

    @Override
    protected Result doRun() throws BlazeException {
        // we need to leverage the "com.fizzed:hooks-maven-plugin" with the "classpath" goal, to figure out the classpath
        // for the various maven goals/phases that the user is requesting
        // mvn compile -Dclasspath.scope=runtime com.fizzed:hooks-maven-plugin:2.0.0:classpath
        final Exec exec = exec("mvn");

        // the working directory MUST be the pom.xml we are basing this on
        exec.workingDir(this.mavenProject.getPomFile().toAbsolutePath().getParent());

        // is there a target module?
        if (this.module != null) {
            if (this.alsoMakeDependants) {
                exec.arg("-am");
            }
            exec.arg("-pl").arg(this.module);
        }

        // add the phases
        for (String phase : this.phases) {
            exec.arg(phase);
        }

        // set the scope we want
        exec.arg("-Dclasspath.scope=" + this.scope);

        // the plugin we need to call
        exec.arg(HOOKS_MAVEN_PLUGIN+":classpath");

        // any additional arguments
        if (this.arguments != null) {
            for (String arg : this.arguments) {
                exec.arg(arg);
            }
        }

        // run it!
        exec
            .verbosity(this.getVerboseLogger().getLevel())      // passthrough verbosity
            .run();

        // location of "classpath.txt" file will be the target or module/target relative to the pomFile
        final Path classpathTxtFile;
        if (this.module == null) {
            classpathTxtFile = this.mavenProject.getPomFile().resolveSibling("target/classpath.txt");
        } else {
            classpathTxtFile = this.mavenProject.getPomFile().resolveSibling(this.module).resolve("target/classpath.txt");
        }

        if (!Files.exists(classpathTxtFile)) {
            throw new BlazeException("Unable to locate classpath.txt file at " + classpathTxtFile);
        }

        // read in the entire classpath.txt file, and each line represents a Path
        final List<String> lines;
        try {
            lines = Files.readAllLines(classpathTxtFile);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        List<Path> paths = new ArrayList<>();
        for (String line : lines) {
            paths.add(Paths.get(line));
        }

        return new Result(this, new MavenClasspath(paths));
    }

}
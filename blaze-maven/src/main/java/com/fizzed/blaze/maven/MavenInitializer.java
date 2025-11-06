package com.fizzed.blaze.maven;

import com.fizzed.blaze.Context;
import com.fizzed.blaze.core.Action;
import com.fizzed.blaze.core.BlazeException;
import com.fizzed.blaze.core.VerbosityMixin;
import com.fizzed.blaze.util.VerboseLogger;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class MavenInitializer extends Action<MavenInitializer.Result, MavenProject> implements VerbosityMixin<MavenInitializer> {

    static public class Result extends com.fizzed.blaze.core.Result<MavenInitializer, MavenProject,Result> {
        public Result(MavenInitializer action, MavenProject value) {
            super(action, value);
        }
    }

    protected final VerboseLogger log;
    protected Path pomFile;

    public MavenInitializer(Context context) {
        super(context);
        this.log = new VerboseLogger(this);
    }

    @Override
    public VerboseLogger getVerboseLogger() {
        return this.log;
    }

    public MavenInitializer pomFile(Path pomFile) {
        this.pomFile = pomFile;
        return this;
    }

    @Override
    protected Result doRun() throws BlazeException {
        // try to find the pom.xml file if not set
        if (this.pomFile == null) {
            // we will only assume the current directory
            log.verbose("Trying to detect pom.xml in the current directory...");
            this.pomFile = Paths.get("pom.xml");
        }

        // pom file needs to exist
        if (!Files.exists(this.pomFile)) {
            throw new IllegalArgumentException("The file " + this.pomFile + " does not exist");
        }

        return new Result(this, new MavenProject(this.pomFile));
    }

}
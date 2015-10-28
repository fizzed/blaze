/* global Packages, Contexts */

var Imports = JavaImporter(
    Packages.com.fizzed.blaze.Contexts,
    Packages.org.eclipse.jgit.lib.Repository,
    Packages.org.eclipse.jgit.storage.file.FileRepositoryBuilder,
    Packages.org.eclipse.jgit.api.Git,
    Packages.org.eclipse.jgit.api.LogCommand,
    Packages.org.eclipse.jgit.api.errors.GitAPIException,
    Packages.org.eclipse.jgit.lib.Ref,
    Packages.org.eclipse.jgit.revwalk.RevCommit);

with (Imports) {

    var log = Contexts.logger();

    var status = function() {
        repo = new FileRepositoryBuilder()
            .readEnvironment()  // scan environment GIT_* variables
            .findGitDir()       // scan up the file system tree
            .build();

        g = new Git(repo);

        status = g.status().call();
        log.info("Added: {}", status.getAdded());
        log.info("Changed: {}", status.getChanged());
        log.info("Conflicting: {}", status.getConflicting());
        log.info("ConflictingStageState: {}", status.getConflictingStageState());
        log.info("IgnoredNotInIndex: {}", status.getIgnoredNotInIndex());
        log.info("Missing: {}", status.getMissing());
        log.info("Modified: {}", status.getModified());
        log.info("Removed: {}", status.getRemoved());
        log.info("Untracked: {}", status.getUntracked());
        log.info("UntrackedFolders: {}", status.getUntrackedFolders());
    };
    
}

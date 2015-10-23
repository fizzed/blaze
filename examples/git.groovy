import org.eclipse.jgit.lib.Repository
import org.eclipse.jgit.storage.file.FileRepositoryBuilder
import org.eclipse.jgit.api.Git
import org.eclipse.jgit.api.LogCommand
import org.eclipse.jgit.api.errors.GitAPIException
import org.eclipse.jgit.lib.Ref
import org.eclipse.jgit.revwalk.RevCommit

def status() {
    repo = new FileRepositoryBuilder()
        .readEnvironment()  // scan environment GIT_* variables
        .findGitDir()       // scan up the file system tree
        .build();
      
    def g = Git.wrap(repo)
    try {
        status = g.status().call();
        log.info("Added: " + status.getAdded());
        log.info("Changed: " + status.getChanged());
        log.info("Conflicting: " + status.getConflicting());
        log.info("ConflictingStageState: " + status.getConflictingStageState());
        log.info("IgnoredNotInIndex: " + status.getIgnoredNotInIndex());
        log.info("Missing: " + status.getMissing());
        log.info("Modified: " + status.getModified());
        log.info("Removed: " + status.getRemoved());
        log.info("Untracked: " + status.getUntracked());
        log.info("UntrackedFolders: " + status.getUntrackedFolders())  
    } finally {
        g.close()
    }

}


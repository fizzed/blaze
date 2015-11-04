import com.fizzed.blaze.Contexts;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.Status;
import org.slf4j.Logger;

public class git {
    static final private Logger log = Contexts.logger();

    public void status() throws Exception {
        Repository repo = new FileRepositoryBuilder()
            .readEnvironment()  // scan environment GIT_* variables
            .findGitDir()       // scan up the file system tree
            .build();
        
        try (Git git = new Git(repo)) {
            Status status = git.status().call();
            log.info("Added: " + status.getAdded());
            log.info("Changed: " + status.getChanged());
            log.info("Conflicting: " + status.getConflicting());
            log.info("ConflictingStageState: " + status.getConflictingStageState());
            log.info("IgnoredNotInIndex: " + status.getIgnoredNotInIndex());
            log.info("Missing: " + status.getMissing());
            log.info("Modified: " + status.getModified());
            log.info("Removed: " + status.getRemoved());
            log.info("Untracked: " + status.getUntracked());
            log.info("UntrackedFolders: " + status.getUntrackedFolders());
        }
    }
    
}

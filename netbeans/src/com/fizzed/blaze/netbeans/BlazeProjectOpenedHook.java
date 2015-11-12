package com.fizzed.blaze.netbeans;

import com.fizzed.blaze.core.BlazeProjects;
import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.project.Project;
import org.netbeans.spi.project.ProjectServiceProvider;
import org.netbeans.spi.project.ui.ProjectOpenedHook;

@ProjectServiceProvider(
    service = ProjectOpenedHook.class,
    projectType = {
        "org-netbeans-modules-ant-freeform",
        "org-netbeans-modules-java-j2seproject",
        "org-netbeans-modules-maven",
        "com-fizzed-blaze-netbeans"
    }
)
public class BlazeProjectOpenedHook extends ProjectOpenedHook {
    private static final Logger LOG = Logger.getLogger(BlazeProjectOpenedHook.class.getCanonicalName());

    private final Project project;
    private final Map<String, ClassPath[]> classPaths;

    public BlazeProjectOpenedHook(Project project) {
        this.project = project;
        this.classPaths = new HashMap<>();
    }

    public Project getProject() {
        return project;
    }
    
    public Map<String, ClassPath[]> getClassPaths() {
        return classPaths;
    }
    
    @Override
    protected void projectOpened() {
        /**
        Logger rootLogger = Logger.getLogger("org.netbeans.modules.java");
        
        for(Handler handler : rootLogger.getHandlers()) {
          // Change log level of default handler(s) of root logger
          // The paranoid would check that this is the ConsoleHandler ;)
          handler.setLevel(Level.FINEST);
        }
        
        // Set root logger level
        rootLogger.setLevel(Level.FINEST);
        */
        
        
        LOG.log(Level.INFO, "Opened project {0}", project);
        
        File projectDir = BlazeNetbeansProjects.getProjectDirectory(project);
        
        if (!BlazeProjects.isBlazed(projectDir)) {
            return;
        }
        
        // register this project as "blazed" (which enables classpath lookups)
        BlazeNetbeansProject blazeProject = new BlazeNetbeansProject(project);
        
        BlazeNetbeansProjects.register(project, blazeProject);
        
        blazeProject.onOpen();
    }
    
    @Override
    protected void projectClosed() {
        LOG.log(Level.INFO, "Closed project {0}", project);
        
        BlazeNetbeansProject blazeProject = new BlazeNetbeansProject(project);
        
        blazeProject.onClose();
        
        BlazeNetbeansProjects.unregister(project);
    }
}
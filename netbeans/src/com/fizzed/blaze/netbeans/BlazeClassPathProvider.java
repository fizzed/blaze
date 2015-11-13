package com.fizzed.blaze.netbeans;

import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.java.classpath.*;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.spi.java.classpath.ClassPathProvider;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.lookup.ServiceProvider;

@ServiceProvider(
    service=ClassPathProvider.class,
    position=99
)
public class BlazeClassPathProvider implements ClassPathProvider {
    private static final Logger LOG = Logger.getLogger(BlazeClassPathProvider.class.getCanonicalName());
    
    public BlazeClassPathProvider() {
        // do nothing
    }

    @Override
    public ClassPath findClassPath(FileObject fileObject, String type) {
        Project project = FileOwnerQuery.getOwner(fileObject);
        
        // this may not be part of our project
        if (project == null) {
            return null;
        }
        
        LOG.log(Level.INFO, "findClassPath({0}, {1}) on project {2}", new Object[] { fileObject, type, project} );
        
        BlazeNetbeansProject blazeProject = BlazeNetbeansProjects.find(project);
        
        if (blazeProject == null) {
            LOG.log(Level.WARNING, "Unabe to find associated blaze project for {0}", fileObject);
            return null;
        }
        
        File file = FileUtil.toFile(fileObject);
        
        if (file == null) {
            LOG.log(Level.WARNING, "Unabe to convert file object to file {0}", fileObject);
            return null;
        }
        
        return internalFindClassPath(blazeProject, type, file);
    }
    
    public ClassPath internalFindClassPath(BlazeNetbeansProject blazeProject, String type, File file) {
        ClassPath cp = null;
        
        switch (type) {
            case ClassPath.COMPILE:
            case ClassPath.EXECUTE:
                cp = blazeProject.findCompileClassPath(file);
                break;
            case ClassPath.SOURCE:
                cp = blazeProject.findSourceClassPath(file);
                break;
            case ClassPath.BOOT:
                cp = blazeProject.findBootClassPath(file);
                break;
            default:
                LOG.log(Level.WARNING, "Not handling {0} for {1}", new Object[] { type, file });
                break;
        
        }
        
        LOG.log(Level.INFO, "Returning {0} classpath {1} for {2}", new Object[] { type, cp, file });
        
        return cp;
    }
}
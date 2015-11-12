
package com.fizzed.blaze.netbeans;

import com.fizzed.blaze.core.BlazeProjects;
import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.ImageIcon;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.spi.project.ProjectFactory;
import org.netbeans.spi.project.ProjectFactory2;
import org.netbeans.spi.project.ProjectState;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.ImageUtilities;
import org.openide.util.lookup.ServiceProvider;

@ServiceProvider(service=ProjectFactory.class)
public class BlazeProjectFactory implements ProjectFactory2 {
    static private final Logger LOG = Logger.getLogger(BlazeProjectFactory.class.getCanonicalName());

    @Override
    public boolean isProject(FileObject fo) {
        LOG.log(Level.INFO, "isProject for {0}", fo);
        
        File dir = FileUtil.toFile(fo);
        return BlazeProjects.isOnlyBlazed(dir);
    }
    
    @Override
    public ProjectManager.Result isProject2(FileObject fo) {
        LOG.log(Level.INFO, "isProject2 for {0}", fo);
        
        File dir = FileUtil.toFile(fo);
        
        if (!BlazeProjects.isOnlyBlazed(dir)) {
            return null;
        }
        
        return new ProjectManager.Result(
                new ImageIcon(ImageUtilities.loadImage(
                    "com/fizzed/blaze/netbeans/icon1.png")));
    }

    @Override
    public Project loadProject(FileObject fo, ProjectState ps) throws IOException {
        //File dir = FileUtil.toFile(fo);
        Project project = new BlazeProject(fo);
        
        return project;
    }

    @Override
    public void saveProject(Project prjct) throws IOException, ClassCastException {
        // do nothing
    }
    
}

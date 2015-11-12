
package com.fizzed.blaze.netbeans;

import java.beans.PropertyChangeListener;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectInformation;
import org.openide.filesystems.FileObject;
import org.openide.util.ImageUtilities;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;

public class BlazeProject implements Project {

    private final FileObject projectDirectory;
    private volatile Lookup lookup;
    
    public BlazeProject(FileObject projectDirectory) {
        this.projectDirectory = projectDirectory;
    }
    
    @Override
    public Lookup getLookup() {
        if (lookup == null) {
            lookup = Lookups.fixed(new Object[] {
                new Info(),
                new BlazeProjectOpenedHook(this),
                new BlazeProjectLogicalView(this),
                new BlazeNodeFactory()
            });
        }
        return lookup;
    }

    @Override
    public FileObject getProjectDirectory() {
        return this.projectDirectory;
    }
    
    private final class Info implements ProjectInformation {

        @Override
        public String getName() {
            return BlazeProject.this.getProjectDirectory().getName();
        }

        @Override
        public String getDisplayName() {
            return getName();
        }

        @Override
        public Icon getIcon() {
            return new ImageIcon(ImageUtilities.loadImage(
                    "com/fizzed/blaze/netbeans/icon1.png"));
        }

        @Override
        public void addPropertyChangeListener(PropertyChangeListener pcl) {
        }

        @Override
        public void removePropertyChangeListener(PropertyChangeListener pcl) {
        }

        @Override
        public Project getProject() {
            return BlazeProject.this;
        }
    }
    
}

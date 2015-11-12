
package com.fizzed.blaze.netbeans;

import java.awt.Image;
import javax.swing.Action;
import org.netbeans.api.annotations.common.StaticResource;
import org.netbeans.spi.project.ui.LogicalViewProvider;
import org.netbeans.spi.project.ui.support.CommonProjectActions;
import org.netbeans.spi.project.ui.support.NodeFactorySupport;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.FilterNode;
import org.openide.nodes.Node;
import org.openide.util.Exceptions;
import org.openide.util.ImageUtilities;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;
import org.openide.util.lookup.ProxyLookup;

public class BlazeProjectLogicalView implements LogicalViewProvider {
    
    @StaticResource()
    public static final String PROJECT_ICON = "com/fizzed/blaze/netbeans/icon1.png";

    private final BlazeProject project;

    public BlazeProjectLogicalView(BlazeProject project) {
        this.project = project;
    }
    
    @Override
    public Node createLogicalView() {
        try {
            //Obtain the project directory's node:
            FileObject projectDirectory = project.getProjectDirectory();
            DataFolder projectFolder = DataFolder.findFolder(projectDirectory);
            Node nodeOfProjectFolder = projectFolder.getNodeDelegate();
            return new ProjectNode(nodeOfProjectFolder, project);
        } catch (DataObjectNotFoundException donfe) {
            Exceptions.printStackTrace(donfe);
            return new AbstractNode(Children.LEAF);
        }
    }

    @Override
    public Node findPath(Node node, Object o) {
        // do not implement yet
        return null;
    }
    
    private final class ProjectNode extends FilterNode {

        final BlazeProject project;

        public ProjectNode(Node node, BlazeProject project) throws DataObjectNotFoundException {
            super(node,
                    NodeFactorySupport.createCompositeChildren(
                        project, "Projects/com-fizzed-blaze-netbeans/Nodes"),
                    //new FilterNode.Children(node),
                    new ProxyLookup(new Lookup[] { Lookups.singleton(project), node.getLookup()}));
            this.project = project;
        }
        
        @Override
        public Image getIcon(int type) {
            return ImageUtilities.loadImage(PROJECT_ICON);
        }

        @Override
        public Image getOpenedIcon(int type) {
            return getIcon(type);
        }
    
        /**
        @Override
        public Action[] getActions(boolean arg0) {
            return new Action[] {
                CommonProjectActions.
                CommonProjectActions.closeProjectAction()
            };
        }
        */
        
    }
    
}

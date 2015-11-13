package com.fizzed.blaze.netbeans;

import org.netbeans.api.project.Project;
import org.netbeans.spi.project.ui.support.NodeFactory;
import org.netbeans.spi.project.ui.support.NodeFactorySupport;
import org.netbeans.spi.project.ui.support.NodeList;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.nodes.FilterNode;
import org.openide.nodes.Node;
import org.openide.util.*;

@NodeFactory.Registration(
    projectType = {
        "org-netbeans-modules-maven",
        "org-netbeans-modules-ant-freeform",
        "org-netbeans-modules-java-j2seproject",
        "com-fizzed-blaze-netbeans"
    },
    position = 199
)
public class BlazeNodeFactory implements NodeFactory {

    @Override
    public NodeList<?> createNodes(Project project) {
        /**
        File projectDir = BlazeNetbeansProjects.getProjectDirectory(project);
        
        // verify this project is blaze-enabled
        if (!BlazeProjects.isBlazed(projectDir)) {
            // empty list
            return NodeFactorySupport.fixedNodeList();
        }
        */
        
        BlazeNetbeansProject blazeProject = BlazeNetbeansProjects.find(project);
        
        if (blazeProject == null) {
            // empty list
            return NodeFactorySupport.fixedNodeList();
        }
        
        FilterNode projectNode;
        try {
            // find path we need
            FileObject p = project.getProjectDirectory();
            
            if (!blazeProject.getScriptRoots().contains(blazeProject.getProjectDir())) {
                // lookup sub-dir
                p = p.getFileObject("blaze");
            }
            
            if (p != null) {
                Node node = DataObject.find(p).getNodeDelegate();

                if (node != null) {
                    projectNode = new BlazeNode(node, new BlazeNodeChildren(blazeProject, node));
                    return NodeFactorySupport.fixedNodeList(projectNode);
                }
            }
        } catch (DataObjectNotFoundException ex) {
            Exceptions.printStackTrace(ex);
        }
        
        return NodeFactorySupport.fixedNodeList();
    }
    
}

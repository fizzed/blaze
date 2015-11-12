
package com.fizzed.blaze.netbeans;

import com.fizzed.blaze.core.BlazeProjects;
import java.util.ArrayList;
import java.util.List;
import org.openide.nodes.FilterNode;
import org.openide.nodes.Node;

/**
 *
 * @author joelauer
 */
public class BlazeNodeChildren extends FilterNode.Children {

    private final BlazeNetbeansProject blazeProject;
    
    public BlazeNodeChildren(BlazeNetbeansProject blazeProject, Node original) {
        super(original);
        this.blazeProject = blazeProject;
    }

    @Override
    protected Node[] createNodes(Node object) {
        List<Node> result = new ArrayList<>();
        for (Node node : super.createNodes(object)) {
            if (accept(node)) {
                result.add(node);
            }
        }
        return result.toArray(new Node[0]);
    }

    private boolean accept(Node node) {
        String fileName = node.getDisplayName();
        
        return fileName.endsWith(".conf")
                || BlazeProjects.isBlazeScript(fileName);
    }
    
}

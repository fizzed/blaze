/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.fizzed.blaze.netbeans;

import org.openide.nodes.FilterNode;
import org.openide.nodes.Node;

/**
 *
 * @author Joe Lauer
 */
public class BlazeNode extends FilterNode {
    
    public BlazeNode(Node node, Children children) {
        super(node, children);
    }

    @Override
    public String getDisplayName() {
        return "Blaze Files";
    }
    
}

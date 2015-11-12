/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.fizzed.blaze.core;

import java.io.File;
import java.util.HashSet;
import java.util.Set;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import org.junit.Test;

/**
 *
 * @author joelauer
 */
public class BlazeProjectsTest {
    
    @Test
    public void isBlazeScript() {
        File scriptRoot = new File("test");
        
        Set<File> scriptRoots = new HashSet<>();
        scriptRoots.add(scriptRoot);
        
        File testScriptFile = null;
        boolean result;
        
        testScriptFile = new File(scriptRoot, "blaze.java");
        result = BlazeProjects.isBlazeScript(scriptRoots, testScriptFile);
        
        assertThat(result, is(true));
        
        testScriptFile = new File(scriptRoot, "blaze.conf");
        result = BlazeProjects.isBlazeScript(scriptRoots, testScriptFile);
        
        assertThat(result, is(false));
        
        testScriptFile = new File(scriptRoot, "blaze.jar");
        result = BlazeProjects.isBlazeScript(scriptRoots, testScriptFile);
        
        assertThat(result, is(false));
    }
    
}

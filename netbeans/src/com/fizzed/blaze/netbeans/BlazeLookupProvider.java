/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.fizzed.blaze.netbeans;

import java.util.logging.Logger;
import org.netbeans.api.project.Project;
import org.netbeans.spi.project.LookupProvider;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;

/**
@LookupProvider.Registration(projectType = {
    "org-netbeans-modules-ant-freeform",
    "org-netbeans-modules-java-j2seproject",
    "org-netbeans-modules-maven"
})
*/
//public class BlazeLookupProvider implements LookupProvider {
public class BlazeLookupProvider {
    private static final Logger LOG = Logger.getLogger(BlazeLookupProvider.class.getCanonicalName());
    
    /**
    @Override
    public Lookup createAdditionalLookup(final Lookup lookup) {
        LOG.info("Creating blaze project hook...");
        
        Project project = lookup.lookup(Project.class);
        
        // registers the project hook with this project
        return Lookups.fixed(new BlazeProjectOpenedHook(project));
    }
    */

}
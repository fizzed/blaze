/*
 * Copyright 2014 Fizzed Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package co.fizzed.otter.cli;

import java.io.File;
import java.util.List;
import org.apache.ivy.Ivy;
import org.apache.ivy.core.event.EventManager;
import org.apache.ivy.core.module.descriptor.Artifact;
import org.apache.ivy.core.module.descriptor.DefaultDependencyDescriptor;
import org.apache.ivy.core.module.descriptor.DefaultModuleDescriptor;
import org.apache.ivy.core.module.descriptor.ModuleDescriptor;
import org.apache.ivy.core.module.id.ModuleRevisionId;
import org.apache.ivy.core.report.ResolveReport;
import org.apache.ivy.core.resolve.ResolveData;
import org.apache.ivy.core.resolve.ResolveEngine;
import org.apache.ivy.core.resolve.ResolveOptions;
import org.apache.ivy.core.retrieve.RetrieveOptions;
import org.apache.ivy.core.settings.IvySettings;
import org.apache.ivy.core.sort.SortEngine;
import org.apache.ivy.plugins.resolver.IBiblioResolver;

/**
 *
 * @author joelauer
 */
public class Ivy2Main {
    
    public static void main(String[] args) throws Exception {
        
        //String groupId = "org.springframework";
        //String artifactId = "spring-context-support";
        //String version = "4.0.2.RELEASE";
        
        String groupId = "ch.qos.logback";
        String artifactId = "logback-classic";
        String version = "1.1.2";
        
        File out = new File("out");

        // create an ivy instance
        IvySettings ivySettings = new IvySettings();
        ivySettings.setDefaultCache(new File("ivy/cache"));
        
        ResolveEngine engine = new ResolveEngine(ivySettings, new EventManager(),new SortEngine(ivySettings));
        ResolveData d = new ResolveData(engine, new ResolveOptions());
        
        /**
        settings=new IvySettings();
        
  cache=new File("build/cache");
  data=new ResolveData(engine,new ResolveOptions());
  cache.mkdirs();
  settings.setDefaultCache(cache);
  */
        
        
        // use the biblio resolver, if you consider resolving POM declared dependencies
        IBiblioResolver br = new IBiblioResolver();
        br.setM2compatible(true);
        br.setUsepoms(true);
        br.setName("central");

        ivySettings.addResolver(br);
        ivySettings.setDefaultResolver(br.getName());

        Ivy ivy = Ivy.newInstance(ivySettings);

        // Step 1: you always need to resolve before you can retrieve
        ResolveOptions ro = new ResolveOptions();
        // this seems to have no impact, if you resolve by module descriptor (in contrast to resolve by ModuleRevisionId)
        ro.setTransitive(true);
        // if set to false, nothing will be downloaded
        ro.setDownload(true);

        // 1st create an ivy module (this always(!) has a "default" configuration already)
        DefaultModuleDescriptor md = DefaultModuleDescriptor.newDefaultInstance(
            // give it some related name (so it can be cached)
            ModuleRevisionId.newInstance(
                "co.fizzed", 
                "test-project", 
                "1.0-SNAPSHOT"
            )
        );

        // add dependencies for what we are really looking for
        ModuleRevisionId ri = ModuleRevisionId.newInstance(groupId, artifactId, version);
        
        ResolveReport resolve = engine.resolve(ri, ro, true);
        
        for (Object a : resolve.getArtifacts()) {
            System.out.println(a);
        }
        
        
        
        /**
        
        // don't go transitive here, if you want the single artifact
        DefaultDependencyDescriptor dd = new DefaultDependencyDescriptor(md, ri, true, true, true);

        // map to master to just get the code jar. See generated ivy module xmls from maven repo
        // on how configurations are mapped into ivy. Or check 
        // e.g. http://lightguard-jp.blogspot.de/2009/04/ivy-configurations-when-pulling-from.html
        dd.addDependencyConfiguration("default", "master");
        md.addDependency(dd);

        // now resolve
        ResolveReport rr = ivy.resolve(md, ro);
        if (rr.hasError()) {
            throw new RuntimeException(rr.getAllProblemMessages().toString());
        }
        
        List depends = rr.getArtifacts();
        for (Object d : depends) {
            System.out.println(d);
        }
        
        // Step 2: retrieve
        ModuleDescriptor m = rr.getModuleDescriptor();

        ivy.retrieve(
            m.getModuleRevisionId(),
            out.getAbsolutePath()+"/[organisation].[artifact](-[classifier]).[ext]",
            new RetrieveOptions()
                // this is from the envelop module
                .setConfs(new String[]{"default"})
        );
        */
    }
    
}

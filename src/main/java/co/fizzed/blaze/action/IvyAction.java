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
package co.fizzed.blaze.action;

import co.fizzed.blaze.core.Context;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import org.apache.commons.io.FileUtils;
import org.apache.ivy.Ivy;
import org.apache.ivy.core.module.descriptor.DefaultDependencyDescriptor;
import org.apache.ivy.core.module.descriptor.DefaultModuleDescriptor;
import org.apache.ivy.core.module.descriptor.ModuleDescriptor;
import org.apache.ivy.core.module.id.ModuleRevisionId;
import org.apache.ivy.core.report.ArtifactDownloadReport;
import org.apache.ivy.core.report.ResolveReport;
import org.apache.ivy.core.resolve.ResolveOptions;
import org.apache.ivy.core.retrieve.RetrieveOptions;
import org.apache.ivy.core.retrieve.RetrieveReport;
import org.apache.ivy.core.settings.IvySettings;
import org.apache.ivy.plugins.resolver.ChainResolver;
import org.apache.ivy.plugins.resolver.DependencyResolver;
import org.apache.ivy.plugins.resolver.FileSystemResolver;

/**
 *
 * @author joelauer
 */
public class IvyAction extends Action<Boolean> {

    private String projectGroup;
    private String projectName;
    private String projectVersion;
    private String resolveScope;
    private File outputDir;
    private final Dependencies dependencies;
    private List<File> jars;
    
    private Ivy ivy;
    private IvySettings settings;
    private ChainResolver chainedResolver;
    private final Resolvers resolvers;
    private DependencyResolver ivyLocal;
    private DependencyResolver mavenCentral;
    private FileSystemResolver mavenLocal;
    
    public IvyAction(Context context) throws Exception {
        super(context);
        createIvy();
        this.dependencies = new Dependencies();
        this.resolvers = new Resolvers();
    }

    private void createIvy() throws Exception {
        this.ivy = Ivy.newInstance();
        this.settings = ivy.getSettings();
        settings.setVariable("ivy.default.configuration.m2compatible", "true");
        ivy.configureDefault();
        
        // setup better maven repos (extract out of defaults...)
        this.ivyLocal = settings.getResolver("local");
        ivyLocal.setName("ivyLocal");
        
        this.mavenCentral = settings.getResolver("public");
        mavenCentral.setName("mavenCentral");

        // create local .m2 repo
        this.mavenLocal = new FileSystemResolver();
        mavenLocal.setName("mavenLocal");
        mavenLocal.setLocal(true);
        File userHomeDir = new File(System.getProperty("user.home"));
        mavenLocal.addArtifactPattern(userHomeDir.getAbsolutePath() + "/.m2/repository/[organisation]/[module]/[revision]/[module]-[revision](-[classifier]).[ext]");
        mavenLocal.addIvyPattern(userHomeDir.getAbsolutePath() + "/.m2/repository/[organisation]/[module]/[revision]/[module]-[revision].pom");
        mavenLocal.setM2compatible(true);

        // chained resolver important for resolving
        this.chainedResolver = new ChainResolver();
        chainedResolver.setName("default");
        settings.getResolvers().clear();
    }
    
    public String getProjectGroup() {
        return projectGroup;
    }

    public IvyAction projectGroup(String projectGroup) {
        this.projectGroup = projectGroup;
        return this;
    }

    public String getProjectName() {
        return projectName;
    }

    public IvyAction projectName(String projectName) {
        this.projectName = projectName;
        return this;
    }

    public String getProjectVersion() {
        return projectVersion;
    }

    public IvyAction projectVersion(String projectVersion) {
        this.projectVersion = projectVersion;
        return this;
    }

    public String getResolveScope() {
        return resolveScope;
    }

    public IvyAction resolveScope(String resolveScope) {
        this.resolveScope = resolveScope;
        return this;
    }

    public File getOutputDir() {
        return outputDir;
    }

    public IvyAction outputDir(File outputDir) {
        this.outputDir = outputDir;
        return this;
    }
    
    public IvyAction outputDir(String outputDir) {
        this.outputDir = new File(outputDir);
        return this;
    }

    public List<File> getJars() {
        return jars;
    }
    
    public List<File> jars() {
        return this.jars;
    }

    public void setJars(List<File> jars) {
        this.jars = jars;
    }
    
    public Resolvers getResolvers() {
        return this.resolvers;
    }
    
    public Dependencies getDependencies() {
        return this.dependencies;
    }
    
    public String classpath() {
        return this.jars.stream().map(f -> f.getAbsolutePath()).collect(Collectors.joining(":"));
    }
    
    @Override
    protected Result<Boolean> execute() throws Exception {
        // make and/or clean dependency lib
        File scopeOutputDir = new File(outputDir, resolveScope);
        scopeOutputDir.mkdirs();
        if (scopeOutputDir.exists()) {
            FileUtils.cleanDirectory(scopeOutputDir);
        }
        
        /**
        Ivy ivy = Ivy.newInstance();
        IvySettings settings = ivy.getSettings();
        settings.setVariable("ivy.default.configuration.m2compatible", "true");
        ivy.configureDefault();
        
        // setup better maven repos
        DependencyResolver ivyLocal = settings.getResolver("local");
        ivyLocal.setName("ivyLocal");
        
        DependencyResolver mavenCentral = settings.getResolver("public");
        mavenCentral.setName("mavenCentral");

        // create local .m2 repo
        FileSystemResolver mavenLocal = new FileSystemResolver();
        //IBiblioResolver mavenLocal = new IBiblioResolver();
        mavenLocal.setName("mavenLocal");
        mavenLocal.setLocal(true);
        File userHomeDir = new File(System.getProperty("user.home"));
        mavenLocal.addArtifactPattern(userHomeDir.getAbsolutePath() + "/.m2/repository/[organisation]/[module]/[revision]/[module]-[revision](-[classifier]).[ext]");
        mavenLocal.addIvyPattern(userHomeDir.getAbsolutePath() + "/.m2/repository/[organisation]/[module]/[revision]/[module]-[revision].pom");
        mavenLocal.setM2compatible(true);

        // chained resolver important for resolving
        ChainResolver chainedResolver = new ChainResolver();
        //chainedResolver.setDual(true);
        chainedResolver.setName("default");
        chainedResolver.add(ivyLocal);
        chainedResolver.add(mavenLocal);
        chainedResolver.add(mavenCentral);
        settings.getResolvers().clear();
        settings.addResolver(chainedResolver);
        */
        
        settings.addResolver(chainedResolver);
        
        // ivy cache makes stuff waaaaay faster
        File cache = new File(settings.getDefaultCache().getAbsolutePath());
        settings.setDefaultCache(cache);

        if (!cache.exists()) {
            cache.mkdirs();
        } else if (!cache.isDirectory()) {
            throw new Exception(cache + " is not a directory");
        }

        // default is compile scope...
        String[] confs = new String[] { "default", "master", "compile", "runtime" };
 
        DefaultModuleDescriptor dmd;
        if (true) {
            // this is the project artifact we are working on
            dmd = DefaultModuleDescriptor.newDefaultInstance(ModuleRevisionId.newInstance(projectGroup, projectName, projectVersion));
            
            for (Dependency artifact : this.dependencies) {
                ModuleRevisionId mrid = ModuleRevisionId.newInstance(artifact.group, artifact.name, artifact.version);
                DefaultDependencyDescriptor dd = new DefaultDependencyDescriptor(dmd, mrid, false, false, true);
                
                for (int i = 0; i < confs.length; i++) {
                    dd.addDependencyConfiguration("default", confs[i]);
                }
                
                dmd.addDependency(dd);
            }

            // this would now save the ivy.xml if we wanted...
            // XmlModuleDescriptorWriter.write(dmd, ivyfile);
            confs = new String[] {"default"};
        }

        /**
        for (Object r : settings.getResolvers()) {
            System.out.println("Resolver: " + r + " -> " + r.getClass());
        }
        */
            
        ResolveOptions resolveOptions = new ResolveOptions()
                .setConfs(confs)
                //.setValidate(validate)
                //.setResolveMode(line.getOptionValue("mode"))
                //.setArtifactFilter(
                //    FilterHelper.getArtifactTypeFilter(line.getOptionValues("types")));
                .setTransitive(true)
                .setCheckIfChanged(false)
                .setRefresh(true);
            
        ResolveReport resolveReport = ivy.resolve(dmd, resolveOptions);
        if (resolveReport.hasError()) {
            throw new Exception("Unable to cleanly resolve ivy dependencies");
        }
        
        ModuleDescriptor md = resolveReport.getModuleDescriptor();
        
        /**
        for (Object o : resolveReport.getArtifacts()) {
            if (o instanceof MDArtifact) {
                MDArtifact artifact = (MDArtifact)o;
                System.out.println("Artifact: " + artifact);
            } else {
                System.out.println("Artifact class unknown: " + o + " -> " + o.getClass());
            }
        }
        */
        
        this.jars = new ArrayList<>();
        for (ArtifactDownloadReport adr : resolveReport.getAllArtifactsReports()) {
            System.out.println(adr.getName() + " -> " + adr.getLocalFile());
            if (adr.getLocalFile() != null) {
                this.jars.add(adr.getLocalFile());
            }
        }
        
        
        if (confs.length == 1 && "*".equals(confs[0])) {
            confs = md.getConfigurationsNames();
        }
        
        //String retrievePattern = scopeOutputDir.getAbsolutePath()+"/[conf]/[organization].[artifact]-[revision](-[classifier]).[ext]";
        String retrievePattern = scopeOutputDir.getAbsolutePath()+"/[organization].[artifact]-[revision](-[classifier]).[ext]";
        RetrieveReport retrieveReport = ivy.retrieve(
            md.getModuleRevisionId(),
            new RetrieveOptions()
                .setConfs(confs)
                .setDestArtifactPattern(retrievePattern)
        );
        
        return new Result(Boolean.TRUE);
    }
    
    public class Resolvers {
        
        public Resolvers addIvyLocal() {
            chainedResolver.add(ivyLocal);
            return this;
        }
        
        public Resolvers addMavenLocal() {
            chainedResolver.add(mavenLocal);
            return this;
        }
        
        public Resolvers addMavenCentral() {
            chainedResolver.add(mavenCentral);
            return this;
        }
        
    }

    public class Dependencies extends ArrayList<Dependency> {
        
        public Dependencies add(String group, String name, String version) {
            Dependency d = new Dependency().group(group).name(name).version(version);
            this.add(d);
            return this;
        }
        
    }
    
    static public class Dependency {
        
        private String group;
        private String name;
        private String version;
        private String scope;

        public String getGroup() {
            return group;
        }

        public Dependency group(String group) {
            this.group = group;
            return this;
        }

        public String getName() {
            return name;
        }

        public Dependency name(String name) {
            this.name = name;
            return this;
        }

        public String getVersion() {
            return version;
        }

        public Dependency version(String version) {
            this.version = version;
            return this;
        }

        public String getScope() {
            return scope;
        }

        public Dependency scope(String scope) {
            this.scope = scope;
            return this;
        }
        
    }
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
}

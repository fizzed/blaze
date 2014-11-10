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
import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.io.FileUtils;
import org.apache.ivy.Ivy;
import org.apache.ivy.core.module.descriptor.DefaultDependencyDescriptor;
import org.apache.ivy.core.module.descriptor.DefaultModuleDescriptor;
import org.apache.ivy.core.module.descriptor.MDArtifact;
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
public class IvyMain {
    
    public static void main(String[] args) throws Exception {
        new IvyMain().run(args);
    }
    
    public void run(String[] args) throws Exception {
        String groupId = "co.fizzed";
        String artifactId = "fizzed-otter";
        String version = "1.0.0-SNAPSHOT";
        
        List<ModuleRevisionId> artifacts = new ArrayList<>();
        artifacts.add(ModuleRevisionId.newInstance("org.zeroturnaround", "zt-exec", "1.7"));
        artifacts.add(ModuleRevisionId.newInstance("org.apache.ivy", "ivy", "2.4.0-rc1"));
        //artifacts.add(ModuleRevisionId.newInstance("ch.qos.logback", "logback-core", "1.1.1"));
        artifacts.add(ModuleRevisionId.newInstance("ch.qos.logback", "logback-classic", "1.1.2"));
        artifacts.add(ModuleRevisionId.newInstance("co.fizzed", "fizzed-stork-launcher", "1.2.0-SNAPSHOT"));
        
        
        File outputDir = new File("out");
        if (outputDir.exists()) {
            FileUtils.cleanDirectory(outputDir);
        }
        
        Ivy ivy = Ivy.newInstance();
        //initMessage(line, ivy);
        IvySettings settings = initSettings(ivy);
        
        
        //ivy.getLoggerEngine().pushLogger(new DefaultMessageLogger(Message.MSG_DEBUG));
        
        
        // setup better maven repos...
        
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

        
        ChainResolver chainedResolver = new ChainResolver();
        //chainedResolver.setDual(true);
        chainedResolver.setName("default");
        chainedResolver.add(ivyLocal);
        chainedResolver.add(mavenLocal);
        chainedResolver.add(mavenCentral);
        settings.getResolvers().clear();
        settings.addResolver(chainedResolver);
        //*/
        
        
        
        
        
        ivy.pushContext();

        //File cache = new File(settings.substitute(line.getOptionValue("cache", settings.getDefaultCache().getAbsolutePath())));
        File cache = new File(settings.getDefaultCache().getAbsolutePath());

        //if (line.hasOption("cache")) {
            // override default cache path with user supplied cache path
            settings.setDefaultCache(cache);
        //}

        if (!cache.exists()) {
            cache.mkdirs();
        } else if (!cache.isDirectory()) {
            throw new Exception(cache + " is not a directory");
        }

        String[] confs;
        //if (line.hasOption("confs")) {
        //    confs = line.getOptionValues("confs");
        //} else {
         //confs = new String[] {"*"};
        //confs = new String[] { "default" };
        confs = new String[] { "default", "compile", "runtime" };
            //confs = new String[] {"default","compile","runtime","provided"};
        //}
 
        File ivyfile;
        DefaultModuleDescriptor dmd;
        if (true) {
        //if (line.hasOption("dependency")) {
            //String[] dep = line.getOptionValues("dependency");
            //String[] dep = new String[] { groupId, artifactId, version };
            //ivyfile = File.createTempFile("ivy", ".xml");
//            ivyfile = new File("ivy.xml");
//            ivyfile.deleteOnExit();
            
            // this is the project artifact we are working on
            dmd = DefaultModuleDescriptor.newDefaultInstance(ModuleRevisionId.newInstance(groupId, artifactId, version));
            
            for (ModuleRevisionId artifact : artifacts) {
                DefaultDependencyDescriptor dd = new DefaultDependencyDescriptor(dmd, artifact, false, false, true);
                
                //dd.addDependencyConfiguration("default", "compile");
                
                for (int i = 0; i < confs.length; i++) {
                    dd.addDependencyConfiguration("default", confs[i]);
                }
                
                dmd.addDependency(dd);
            }
            
            //DefaultDependencyDescriptor dd = new DefaultDependencyDescriptor(dmd, ModuleRevisionId.newInstance(dep[0], dep[1], dep[2]), false, false, true);
            //dmd.addDependency(dd);
 
//            XmlModuleDescriptorWriter.write(dmd, ivyfile);
            confs = new String[] {"default"};
        }

        
        for (Object r : settings.getResolvers()) {
            System.out.println("Resolver: " + r + " -> " + r.getClass());
        }
        
        
        /**
        } else {
            ivyfile = new File(settings.substitute(line.getOptionValue("ivy", "ivy.xml")));
            if (!ivyfile.exists()) {
                error("ivy file not found: " + ivyfile);
            } else if (ivyfile.isDirectory()) {
                error("ivy file is not a file: " + ivyfile);
            }
        }
        */


        //if (line.hasOption("useOrigin")) {
        //    ivy.getSettings().useDeprecatedUseOrigin();
        //}
            
        ResolveOptions resolveOptions = new ResolveOptions()
                .setConfs(confs);
                //.setValidate(validate)
                //.setResolveMode(line.getOptionValue("mode"))
                //.setArtifactFilter(
                //    FilterHelper.getArtifactTypeFilter(line.getOptionValues("types")));
        //if (line.hasOption("notransitive")) {
            resolveOptions.setTransitive(true);
        //}
        //if (line.hasOption("refresh")) {
            resolveOptions.setRefresh(true);
        //}
            
        //ResolveReport report = ivy.resolve(ivyfile.toURI().toURL(), resolveOptions);
        ResolveReport resolveReport = ivy.resolve(dmd, resolveOptions);
        
        //if (resolveReport.hasError()) {
        //    System.exit(1);
        //}
        
        ModuleDescriptor md = resolveReport.getModuleDescriptor();
        
        for (Object o : resolveReport.getArtifacts()) {
            if (o instanceof MDArtifact) {
                MDArtifact artifact = (MDArtifact)o;
                System.out.println("Artifact: " + artifact);
            } else {
                System.out.println("Artifact class unknown: " + o + " -> " + o.getClass());
            }
        }
        
        for (ArtifactDownloadReport adr : resolveReport.getAllArtifactsReports()) {
            System.out.println(adr.getName() + " -> " + adr.getLocalFile());
        }
        
        
        //Map artifactsToCopy = determineArtifactsToCopy(mrid, destFilePattern, options);
        
        
        

        if (confs.length == 1 && "*".equals(confs[0])) {
            confs = md.getConfigurationsNames();
        }
        
       // if (line.hasOption("retrieve")) {
            //String retrievePattern = settings.substitute(line.getOptionValue("retrieve"));
            //if (retrievePattern.indexOf("[") == -1) {
        
            
        //confs = new String[] { "default", "compile", "runtime" };
        
            String retrievePattern = new File("out").getAbsolutePath()+"/lib/[conf]/[organization].[artifact]-[revision](-[classifier]).[ext]";
            //}
            //String ivyPattern = settings.substitute(line.getOptionValue("ivypattern"));
            RetrieveReport retrieveReport = ivy.retrieve(
                md.getModuleRevisionId(),
                //retrievePattern,
                new RetrieveOptions()
                        .setConfs(confs)
                        .setDestArtifactPattern(retrievePattern)
                        //.setSync(line.hasOption("sync"))
                        //.setUseOrigin(line.hasOption("useOrigin"))
                        //.setDestIvyPattern(ivyPattern)
                        //.setArtifactFilter(FilterHelper.getArtifactTypeFilter(line.getOptionValues("types")))
                        //.setMakeSymlinks(line.hasOption("symlink"))
                        //.setMakeSymlinksInMass(line.hasOption("symlinkmass")));
            );
        //}
            
            
        //retrieveReport.
            
            
        
            
        //if (line.hasOption("cachepath")) {
//           outputCachePath(ivy, cache, md, confs,
//                line.getOptionValue("cachepath", "ivycachepath.txt"));
        //}

/**            
        if (line.hasOption("revision")) {
            ivy.deliver(
                md.getResolvedModuleRevisionId(),
                settings.substitute(line.getOptionValue("revision")),
                settings.substitute(line.getOptionValue("deliverto", "ivy-[revision].xml")),
                DeliverOptions.newInstance(settings)
                        .setStatus(settings.substitute(line.getOptionValue("status", "release")))
                        .setValidate(validate));
            if (line.hasOption("publish")) {
                ivy.publish(
                    md.getResolvedModuleRevisionId(),
                    Collections.singleton(settings.substitute(line.getOptionValue("publishpattern",
                        "distrib/[type]s/[artifact]-[revision].[ext]"))),
                    line.getOptionValue("publish"),
                    new PublishOptions()
                            .setPubrevision(settings.substitute(line.getOptionValue("revision")))
                            .setValidate(validate)
                            .setSrcIvyPattern(
                                settings.substitute(line.getOptionValue("deliverto",
                                    "ivy-[revision].xml")))
                            .setOverwrite(line.hasOption("overwrite")));
            }
        }
*/
        
        /**
        if (line.hasOption("main")) {
            // check if the option cp has been set
            List fileList = getExtraClasspathFileList(line);

            // merge -args and left over args
            String[] fargs = line.getOptionValues("args");
            if (fargs == null) {
                fargs = new String[0];
            }
            String[] extra = line.getLeftOverArgs();
            if (extra == null) {
                extra = new String[0];
            }
            String[] params = new String[fargs.length + extra.length];
            System.arraycopy(fargs, 0, params, 0, fargs.length);
            System.arraycopy(extra, 0, params, fargs.length, extra.length);
            // invoke with given main class and merged params
            invoke(ivy, cache, md, confs, fileList, line.getOptionValue("main"), params);
        }
        */
            
        ivy.getLoggerEngine().popLogger();
        ivy.popContext();
        
        
        System.out.println("Done");
    }
    
    
    /**
    private static List<File> getExtraClasspathFileList() {
        List<File> fileList = null;
        /if (line.hasOption("cp")) {
            fileList = new ArrayList<File>();
            String[] cpArray = line.getOptionValues("cp");
            for (int index = 0; index < cpArray.length; index++) {
                StringTokenizer tokenizer = new StringTokenizer(cpArray[index],
                        System.getProperty("path.separator"));
                while (tokenizer.hasMoreTokens()) {
                    String token = tokenizer.nextToken();
                    File file = new File(token);
                    if (file.exists()) {
                        fileList.add(file);
                    } else {
                        Message.warn("Skipping extra classpath '" + file
                                + "' as it does not exist.");
                    }
                }
            }
        //}
        return fileList;
    }
    */
    
    
    private static IvySettings initSettings(Ivy ivy) throws java.text.ParseException, IOException, ParseException {
        IvySettings settings = ivy.getSettings();
        //settings.addAllVariables(System.getProperties());
        //if (line.hasOption("m2compatible")) {
            settings.setVariable("ivy.default.configuration.m2compatible", "true");
        //}

        //configureURLHandler(line.getOptionValue("realm", null), line.getOptionValue("host", null),
        //    line.getOptionValue("username", null), line.getOptionValue("passwd", null));

        /**
        String settingsPath = line.getOptionValue("settings", "");
        if ("".equals(settingsPath)) {
            settingsPath = line.getOptionValue("conf", "");
            if (!"".equals(settingsPath)) {
                Message.deprecated("-conf is deprecated, use -settings instead");
            }
        }
        */
            
        //if ("".equals(settingsPath)) {
            ivy.configureDefault();
        /**
        } else {
            File conffile = new File(settingsPath);
            if (!conffile.exists()) {
                error("ivy configuration file not found: " + conffile);
            } else if (conffile.isDirectory()) {
                error("ivy configuration file is not a file: " + conffile);
            }
            ivy.configure(conffile);
        }
        */
        return settings;
    }
    
}

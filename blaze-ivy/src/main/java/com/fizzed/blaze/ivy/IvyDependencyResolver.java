/*
 * Copyright 2015 Fizzed, Inc.
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
package com.fizzed.blaze.ivy;

import com.fizzed.blaze.core.Dependency;
import com.fizzed.blaze.core.DependencyResolveException;
import com.fizzed.blaze.core.DependencyResolver;
import com.fizzed.blaze.Config;
import com.fizzed.blaze.Context;
import com.fizzed.blaze.internal.ConfigHelper;
import com.fizzed.blaze.internal.DependencyHelper;
import java.io.File;
import java.io.IOException;
import java.net.Authenticator;
import java.net.InetAddress;
import java.net.PasswordAuthentication;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import static java.util.Optional.ofNullable;
import java.util.Set;
import org.apache.ivy.Ivy;
import org.apache.ivy.core.cache.ArtifactOrigin;
import org.apache.ivy.core.cache.CacheDownloadOptions;
import org.apache.ivy.core.cache.CacheMetadataOptions;
import org.apache.ivy.core.cache.CacheResourceOptions;
import org.apache.ivy.core.cache.DefaultRepositoryCacheManager;
import org.apache.ivy.core.cache.ModuleDescriptorWriter;
import org.apache.ivy.core.cache.RepositoryCacheManager;
import org.apache.ivy.core.module.descriptor.Artifact;
import org.apache.ivy.core.module.descriptor.DefaultDependencyDescriptor;
import org.apache.ivy.core.module.descriptor.DefaultModuleDescriptor;
import org.apache.ivy.core.module.descriptor.DependencyDescriptor;
import org.apache.ivy.core.module.descriptor.ModuleDescriptor;
import org.apache.ivy.core.module.id.ModuleRevisionId;
import org.apache.ivy.core.report.ArtifactDownloadReport;
import org.apache.ivy.core.report.ResolveReport;
import org.apache.ivy.core.resolve.ResolveOptions;
import org.apache.ivy.core.resolve.ResolvedModuleRevision;
import org.apache.ivy.core.settings.IvySettings;
import org.apache.ivy.plugins.matcher.PatternMatcher;
import org.apache.ivy.plugins.repository.ArtifactResourceResolver;
import org.apache.ivy.plugins.repository.Repository;
import org.apache.ivy.plugins.repository.Resource;
import org.apache.ivy.plugins.repository.ResourceDownloader;
import org.apache.ivy.plugins.resolver.ChainResolver;
import org.apache.ivy.plugins.resolver.FileSystemResolver;
import org.apache.ivy.plugins.resolver.IBiblioResolver;
import org.apache.ivy.plugins.resolver.util.ResolvedResource;
import org.apache.ivy.util.DefaultMessageLogger;
import org.apache.ivy.util.Message;
import org.apache.ivy.util.url.CredentialsStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Dependency resolver implemented with Ivy.
 */
public class IvyDependencyResolver implements DependencyResolver {
    static private final Logger log = LoggerFactory.getLogger(IvyDependencyResolver.class);
    
    @Override
    public List<File> resolve(
            Context context,
            List<Dependency> resolvedDependencies,
            List<Dependency> dependencies) throws DependencyResolveException, ParseException, IOException {
        
        Objects.requireNonNull(context, "context may not be null");
        Objects.requireNonNull(resolvedDependencies, "resolvedDependencies may not be null");
        Objects.requireNonNull(dependencies, "dependencies may not be null");
        
        log.trace("Already resolved dependencies {}", resolvedDependencies);
        log.trace("Dependencies to resolve {}", dependencies);
        
        // customize logging (ivy uses a terrible approach)
        Message.setDefaultLogger(new FilteringIvyLogger());

        
        //
        // maven settings file?
        //
        
        MavenSettings mavenSettings = null;
        Path mavenSettingsFile = context.userDir().resolve(".m2/settings.xml");
        if (Files.exists(mavenSettingsFile)) {
            try {
                mavenSettings = MavenSettings.parse(mavenSettingsFile);
                log.debug("Using maven settings {}", mavenSettingsFile);
            }
            catch (Exception e) {
                log.error("Unable to cleanly parse {}", mavenSettingsFile, e);
            }
        }
        
        //
        // load up any credentials?
        //
        
        final IvyAuthenticator authenticator = new IvyAuthenticator();
        
        Authenticator.setDefault(authenticator);
        
        
        // creates an Ivy instance with settings
        Ivy ivy = Ivy.newInstance();
        IvySettings ivySettings = ivy.getSettings();
        
        // TODO: ivy truly is a piece of junk - unable to figure out how to NOT
        // cache a SNAPSHOT version so this is the workaround for now - allowing you
        // to delete the entire cache
        if (context.config().value(Config.KEY_DEPENDENCY_CLEAN, Boolean.class).getOr(Config.DEFAULT_DEPENDENCY_CLEAN)) {
            log.info("Cleaning dependency cache...");
            ivy.getResolutionCacheManager().clean();
        }
        
        // remove any SNAPSHOT artifacts from cache
        dependencies.stream().forEach((d) -> {
            // cache pattern ~/.ivy2/cache/com.fizzed/blaze-ssh/jars/blaze-ssh-0.13.1-SNAPSHOT.jar
            if (d.getVersion().endsWith("-SNAPSHOT")) {
                Path cachedFile = context.userDir().resolve(".ivy2/cache/" + d.getGroupId() + "/" + d.getArtifactId() + "/jars/" + d.getArtifactId() + "-" + d.getVersion() + ".jar");
                if (Files.exists(cachedFile)) {
                    log.trace("Deleting cached snapshot dependency {}", cachedFile);
                    try {
                        Files.delete(cachedFile);
                    } catch (IOException e) {
                        log.trace("Unable to delete", e.getMessage());
                    }
                }
            }
        });
        
        /**
        DefaultRepositoryCacheManager cacheManager = new DefaultRepositoryCacheManager();
        cacheManager.setChangingPattern(".*-SNAPSHOT");
        cacheManager.setChangingMatcher(PatternMatcher.EXACT_OR_REGEXP);
        cacheManager.setCheckmodified(true);
        
        ivySettings.addRepositoryCacheManager(cacheManager);
        */
//        DefaultRepositoryCacheManager cacheManager
//            = (DefaultRepositoryCacheManager)ivySettings.getRepositoryCacheManagers()[0];
//        cacheManager.clean();
        
        // maven central resolver
        IBiblioResolver mavenCentralResolver = new IBiblioResolver();
        mavenCentralResolver.setM2compatible(true);
        mavenCentralResolver.setName("mavenCentral");
        mavenCentralResolver.setUseMavenMetadata(true);
        
        // any additional upstream repositories?
        final List<IBiblioResolver> additionalResolvers = new ArrayList<>();
        
        final List<String> repositoryUrls = context.config().valueList(Config.KEY_REPOSITORIES).orNull();
        
        if (repositoryUrls != null) {
            for (String repositoryUrl : repositoryUrls) {
                MavenRepositoryUrl mru = MavenRepositoryUrl.parse(repositoryUrl);
                
                // are there credentials for this too?
                MavenServer mavenServer = ofNullable(mavenSettings)
                    .map(v -> v.findServerById(mru.getId()))
                    .orElse(null);
                
                log.debug("Adding extra maven repo: {}->{}", mru.getId(), mru.getUrl());
                
                if (mavenServer != null) {
                    String host = mru.getUrl().getHost();
                    log.debug("Using maven settings credentials for id={}, host={}, username={}",
                        mru.getId(), host, mavenServer.getUsername());
                    
                    authenticator.addCredentials(host, mavenServer.getUsername(), mavenServer.getPassword());
                }
                
                IBiblioResolver additionalResolver = new IBiblioResolver();
                additionalResolver.setM2compatible(true);
                additionalResolver.setName(mru.getId());
                additionalResolver.setUseMavenMetadata(true);
                additionalResolver.setRoot(mru.getUrl().toString());
                additionalResolvers.add(additionalResolver);
            }
        }
        
        // maven local resolver
        FileSystemResolver mavenLocalResolver = new FileSystemResolver();
        mavenLocalResolver.setName("mavenLocal");
        mavenLocalResolver.setLocal(true);
        
        // it'd be sweet to use the context here, eh?
        //File userHomeDir = new File(System.getProperty("user.home"));
        File userHomeDir = context.userDir().toFile();
        
        mavenLocalResolver.addArtifactPattern(userHomeDir.getAbsolutePath() + "/.m2/repository/[organisation]/[module]/[revision]/[module]-[revision](-[classifier]).[ext]");
        mavenLocalResolver.addIvyPattern(userHomeDir.getAbsolutePath() + "/.m2/repository/[organisation]/[module]/[revision]/[module]-[revision].pom");
        mavenLocalResolver.setM2compatible(true);
        //mavenLocalResolver.setForce(true);
        mavenLocalResolver.setChangingMatcher(PatternMatcher.REGEXP);
        mavenLocalResolver.setChangingPattern(".*-SNAPSHOT");
        mavenLocalResolver.setCheckmodified(true);
        
        // chain resolvers together
        ChainResolver chainResolver = new ChainResolver();
        chainResolver.setName("default");
        chainResolver.add(mavenLocalResolver);
        chainResolver.add(mavenCentralResolver);
        chainResolver.setDual(true);
        additionalResolvers.forEach(v -> {
            chainResolver.add(v);
        });
        
        //chainResolver.setChangingMatcher(PatternMatcher.REGEXP);
        //chainResolver.setChangingPattern(".*-SNAPSHOT");
        //chainResolver.setCheckmodified(true);
        
        ivySettings.addResolver(chainResolver);
        ivy.getSettings().setDefaultResolver(chainResolver.getName());
        
        // fake uber module (this project)
        DefaultModuleDescriptor md =
                DefaultModuleDescriptor.newDefaultInstance(
                        ModuleRevisionId.newInstance("blaze", "blaze", "resolver"));

        // build list of transitive dependencies to resolve
        dependencies.stream()
            .map((d) -> {
                boolean isChanging = d.getVersion().endsWith("-SNAPSHOT");
                return new DefaultDependencyDescriptor(md,
                    ModuleRevisionId.newInstance(d.getGroupId(), d.getArtifactId(), d.getVersion()), false, isChanging, true);
            })
            .forEach((dd) -> {
                dd.addDependencyConfiguration("default", "default");
                md.addDependency(dd);
            });

        String[] confs = new String[] { "default" };

        ResolveOptions resolveOptions = new ResolveOptions().setConfs(confs);

        resolveOptions.setRefresh(true);
        
        //resolveOptions.setValidate(true);
        //resolveOptions.setCheckIfChanged(true);
        //resolveOptions.setRefresh(true);
        //resolveOptions.setValidate(true);
        //resolveOptions.setTransitive(true);
        
        ResolveReport report = ivy.resolve(md, resolveOptions);
        
        if (report.hasError()) {
            // grab first message
            String firstMessage = (String)report.getAllProblemMessages().get(0);
            throw new DependencyResolveException(firstMessage);
        }        

        
        // filter out artifacts that were already resolved and added to classpath
        final Set<String> alreadyResolved = DependencyHelper.toGroupArtifactSet(resolvedDependencies);
        
        // ivy triggers duplicate exclusion calls, this will make sure we only display it once
        final Set<String> alreadyExcluded = new HashSet<>();
        
        // filter and build list of local jar files to use in classpath
        List<File> jarFiles = new ArrayList<>();
        for (ArtifactDownloadReport adr : report.getAllArtifactsReports()) {
            Artifact artifact = adr.getArtifact();
            
            String key = artifact.getModuleRevisionId().getOrganisation()
                            + ":" + artifact.getModuleRevisionId().getName();
            
            log.trace("Potentially filtering {} with key {}", artifact, key);
                
            if (alreadyResolved.contains(key)) {
                log.debug("Excluding {} (already added to classpath)", artifact);
            } else if (adr.getLocalFile() != null) {
                jarFiles.add(adr.getLocalFile());
            }
        }

        return jarFiles;
    }
    
    public class FilteringIvyLogger extends DefaultMessageLogger {
        
        public FilteringIvyLogger() {
            super(Message.MSG_VERBOSE);
        }
        
        @Override
        public void log(String msg, int level) {
            // lots of smart filtering for only the useful messages from ivy
            String trimmedMessage = msg.trim();
            
            if (level < Message.MSG_INFO) {
                log.error(trimmedMessage);
            } else if (trimmedMessage.startsWith("downloading ")) {
                // uppercase the d to match our other logging
                log.info("D{}", trimmedMessage.substring(1));
            } else {
                if (ConfigHelper.isSuperDebugEnabled()) {
                    log.trace(trimmedMessage);
                }
            }
        }

        @Override
        public void rawlog(String msg, int level) {
            log(msg, level);
        }

        @Override
        public void doProgress() {
            //System.out.print(".");
        }

        @Override
        public void doEndProgress(String msg) {
            //System.out.println(msg);
        }
    }
    
    public class DebugIvyLogger extends DefaultMessageLogger {
        
        public DebugIvyLogger() {
            super(Message.MSG_VERBOSE);
        }
        
        @Override
        public void log(String msg, int level) {
            log.debug(msg);
        }

        @Override
        public void rawlog(String msg, int level) {
            log(msg, level);
        }

        @Override
        public void doProgress() {
            // do nothing
        }

        @Override
        public void doEndProgress(String msg) {
            // do nothing
        }
    }
}

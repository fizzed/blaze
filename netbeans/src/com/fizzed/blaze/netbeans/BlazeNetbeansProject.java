
package com.fizzed.blaze.netbeans;

import com.fizzed.blaze.core.BlazeProjects;
import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.platform.JavaPlatform;
import org.netbeans.api.java.platform.JavaPlatformManager;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.netbeans.api.project.Project;
import org.openide.filesystems.FileAttributeEvent;
import org.openide.filesystems.FileChangeListener;
import org.openide.filesystems.FileEvent;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileRenameEvent;
import org.openide.filesystems.FileUtil;

public class BlazeNetbeansProject {
    private static final Logger LOG = Logger.getLogger(BlazeNetbeansProject.class.getCanonicalName());
    
    private final File projectDir;
    private final File blazeJarFile;
    private final Set<File> scriptRoots;
    private final Set<BlazeFileChangeListener> scriptRootListeners;
    private BlazeClassPath sourceClassPath;
    private BlazeClassPath bootClassPath;
    private AggregateBlazeClassPath scriptRootsClassPath;
    private final Map<File,BlazeClassPath> scriptClassPaths;
    // way of preventing duplicate resolving tasks
    private final Map<File,Long> scriptResolvingFutures;
    private final ExecutorService scriptResolvingExecutor;
    
    public BlazeNetbeansProject(Project project) {
        this.projectDir = BlazeNetbeansProjects.getProjectDirectory(project);
        this.blazeJarFile = BlazeProjects.findBlazeJar(projectDir);
        this.scriptRoots = new LinkedHashSet<>();
        this.scriptRootListeners = new LinkedHashSet<>();
        this.scriptClassPaths = new ConcurrentHashMap<>();
        this.scriptResolvingFutures = new ConcurrentHashMap<>();
        this.scriptResolvingExecutor = Executors.newFixedThreadPool(2);
    }

    public File getProjectDir() {
        return projectDir;
    }

    public Set<File> getScriptRoots() {
        return scriptRoots;
    }
    
    public void onOpen() {
        // root project direcotyr OR blaze directory?
        // when within maven/ant project any sources at root project dir really screws things up
        // placing those in a separate directory solves the many problems it created
        if (BlazeProjects.isOnlyBlazed(projectDir)) {
            this.scriptRoots.add(projectDir);
        } else {
            this.scriptRoots.add(new File(projectDir, "blaze"));
        }
        
        // build source classpath for script roots
        List<URL> scriptRootUrls = new LinkedList<>();
        for (File f : this.scriptRoots) {
            scriptRootUrls.add(FileUtil.urlForArchiveOrDir(f));
        }
        
        this.sourceClassPath = new BlazeClassPath(ClassPath.SOURCE, scriptRootUrls, true);
        
        // build bootstrap classpath (jdk libraries)
        JavaPlatform platform = JavaPlatformManager.getDefault().getDefaultPlatform();
        List<URL> platformUrls = new LinkedList<>();
        for (ClassPath.Entry entry: platform.getBootstrapLibraries().entries()) {
            platformUrls.add(entry.getURL());
        }
        
        this.bootClassPath = new BlazeClassPath(ClassPath.BOOT, platformUrls, true);
        
        // build aggregate classpath of all script classpaths
        this.scriptRootsClassPath = new AggregateBlazeClassPath(ClassPath.COMPILE, this.scriptClassPaths.values(), false);
        
        // TODO: find all possible scripts we should pre-kickoff resolving for???
        
        // watch for file change events in all source roots
        for (File scriptRoot : this.scriptRoots) {
            BlazeFileChangeListener listener = new BlazeFileChangeListener();
            this.scriptRootListeners.add(listener);
            FileUtil.addFileChangeListener(listener, scriptRoot);
        }
    }
    
    public void onClose() {
        // watch for file change events in all source roots
        for (BlazeFileChangeListener listener : this.scriptRootListeners) {
            FileUtil.removeFileChangeListener(listener);
        }
        
        /**
        // unregister script classpaths
        for (Map.Entry<File, ClassPath[]> entry : this.scriptClassPaths.entrySet()) {
            try {
                GlobalPathRegistry.getDefault().unregister(ClassPath.EXECUTE, entry.getValue());
                GlobalPathRegistry.getDefault().unregister(ClassPath.COMPILE, entry.getValue());
            } catch (Exception e) {
                LOG.log(Level.WARNING, "{0}", e);
            }
        }
        */
    }
    
    public void asyncResolveScriptDependencies(final List<File> scriptFiles) {
        for (final File scriptFile : scriptFiles) {
            
            // prevent duplicate script resolving submissions
            if (this.scriptResolvingFutures.putIfAbsent(scriptFile, System.currentTimeMillis()) != null) {
                LOG.log(Level.INFO, "Preventing duplicate run of resolving dependencies for {0}", scriptFile);
                continue;
            }

            final ProgressHandle handle = ProgressHandleFactory.createSystemHandle("Blaze Dependencies");
            final Future<?> submit = this.scriptResolvingExecutor.submit(new Runnable() {
                @Override
                public void run() {
                    handle.start();
                    try {
                        LOG.log(Level.INFO,"Resolving dependencies for script {0}", scriptFile);

                        List<File> dependencies = BlazeProjects.resolveScriptDependencies(scriptFile);

                        for (File d : dependencies) {
                            LOG.log(Level.INFO,"Resolved blaze dependency {0}", d);
                        }

                        dependencies.add(0, blazeJarFile);

                        createOrUpdateScriptClassPath(scriptFile, dependencies);
                        
                        /**
                        IndexingManager.getDefault().refreshIndex(
                            Utilities.toURI(projectDir).toURL(),
                            Arrays.asList(Utilities.toURI(scriptFile).toURL()), true);
                        */
                        
                        //IndexingManager.getDefault().refreshAllIndices(FileUtil.toFileObject(scriptFile));
                        
                    } catch (Exception e) {
                        LOG.log(Level.WARNING, "{0}", e);
                    } finally {
                        handle.finish();
                        BlazeNetbeansProject.this.scriptResolvingFutures.remove(scriptFile);
                    }
                }
            });
        }
    }
    
    public ClassPath findBootClassPath(File file) {
        if (this.bootClassPath == null) {
            LOG.log(Level.INFO, "Boot classpath not initialized yet for file {0}", file);
            return null;
        }
        
        // is the query on a script root dir?
        if (file.isDirectory()) {
            if (!this.scriptRoots.contains(file)) {
                LOG.log(Level.INFO, "Not handling boot classpath query (not a script root dir) for {0}", file);
                return null;
            }
        } else if (!BlazeProjects.isBlazeScript(this.scriptRoots, file)) {
            LOG.log(Level.INFO, "Not handling boot classpath query (not a blaze script) for {0}", file);
            return null;
        }
        
        return this.bootClassPath.getUnderlyingClassPath();
    }
    
    public ClassPath findSourceClassPath(File file) {
        if (this.sourceClassPath == null) {
            LOG.log(Level.INFO, "Source classpath not initialized yet for file {0}", file);
            return null;
        }
        
        // is the query on a script root dir?
        if (file.isDirectory()) {
            if (!this.scriptRoots.contains(file)) {
                LOG.log(Level.INFO, "Not handling source classpath query (not a script root dir) for {0}", file);
                return null;
            }
        } else if (!BlazeProjects.isBlazeScript(this.scriptRoots, file)) {
            LOG.log(Level.INFO, "Not handling source classpath query (not a blaze script) for {0}", file);
            return null;
        }
        
        return this.sourceClassPath.getUnderlyingClassPath();
    }
    
    public ClassPath findCompileClassPath(File file) {
        BlazeClassPath cp = null;
        
        // is the file really one of the script root directories?
        if (this.scriptRoots.contains(file)) {
            // netbeans uses the "source root" directory to determine if badges
            // need to be displayed for files in the packages windows -- the problem
            // is that blaze permits per-file classpaths -- one idea is to simply
            // provide a merged view of all possible jars of any scripts -- or just
            // provide the first...
            LOG.log(Level.INFO, "Handling compile classpath query for script root dir {0}", file);
            
            return this.scriptRootsClassPath.getUnderlyingClassPath();
        }
        
        // is the query on a script root dir?
        if (file.isDirectory()) {
            if (!this.scriptRoots.contains(file)) {
                LOG.log(Level.INFO, "Not handling compile classpath query (not a script root dir) for {0}", file);
                return null;
            }
        } else if (!BlazeProjects.isBlazeScript(this.scriptRoots, file)) {
            LOG.log(Level.INFO, "Not handling compile classpath query (not a blaze script) for {0}", file);
            return null;
        }

        cp = this.scriptClassPaths.get(file);
        
        if (cp == null) {
            LOG.log(Level.INFO, "Classpath missing for {0}... creating initial classpath with blaze.jar", file);
            
            // classpath not create yet, kickoff a task to resolve dependencies
            // but return a mutable classpath in the meantime which consists of the blaze.jar
            cp = createOrUpdateScriptClassPath(file, Arrays.asList(this.blazeJarFile));
            
            // fire task to try to actually resolve the dependencies
            asyncResolveScriptDependencies(Arrays.asList(file));
        }
        
        return cp.getUnderlyingClassPath();
    }
    
    public BlazeClassPath createOrUpdateScriptClassPath(File scriptFile, List<File> jarFiles) {
        List<URL> urls = new ArrayList<>();
        
        for (File jarFile : jarFiles) {
            URL url = FileUtil.urlForArchiveOrDir(jarFile);
            urls.add(url);
        }
        
        // create or update underlying impl
        BlazeClassPath cp = this.scriptClassPaths.get(scriptFile);
        
        if (cp == null) {
            // registering the classpath globally enables auto package import hint
            // e.g. click on left-hand item to auto pick the package import to add
            cp = new BlazeClassPath(ClassPath.COMPILE, urls, true);
            if (this.scriptClassPaths.putIfAbsent(scriptFile, cp) != null) {
                // some other thread happened to create the classpath before us
                cp = this.scriptClassPaths.get(scriptFile);
            } else {
                // add classpath to aggregate view
                this.scriptRootsClassPath.addClassPath(cp);
            }
        } else {
            cp.setJars(urls);
        }
        
        return cp;
    }
    
    private class BlazeFileChangeListener implements FileChangeListener {
        @Override
        public void fileFolderCreated(FileEvent fe) {
            // hmm...
        }

        @Override
        public void fileDataCreated(FileEvent fe) {
            // hmm...
        }

        @Override
        public void fileChanged(FileEvent fe) {
            FileObject fo = fe.getFile();
            File file = FileUtil.toFile(fo);
            if (file != null) {
                LOG.log(Level.INFO, "File {0} changed", file);
                
                // only changes to ".conf" files could possibly trigger changes
                // to the associated script and its dependencies
                String name = file.getName();
                if (name.endsWith(".conf")) {
                    String nameWithoutExt = name.substring(0, name.length() - 5);
                    for (File f : file.getParentFile().listFiles()) {
                        if (f.getName().startsWith(nameWithoutExt)) {
                            if (BlazeProjects.isBlazeScript(scriptRoots, f)) {
                                asyncResolveScriptDependencies(Arrays.asList(f));
                            }
                        }
                    }
                }
            }
        }

        @Override
        public void fileDeleted(FileEvent fe) {
            // hmm...
        }

        @Override
        public void fileRenamed(FileRenameEvent fre) {
            // hmm...
        }

        @Override
        public void fileAttributeChanged(FileAttributeEvent fae) {
            // do nothing
        }
    }
}

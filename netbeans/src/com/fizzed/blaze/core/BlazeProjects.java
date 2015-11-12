package com.fizzed.blaze.core;

import com.fizzed.blaze.internal.FileHelper;
import java.io.File;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author joelauer
 */
public class BlazeProjects {
    private static final Logger LOG = Logger.getLogger(BlazeProjects.class.getCanonicalName());
    
    static public boolean fileExists(File dir, String name) {
        File f = new File(dir, name);
        return f.exists();
    }
    
    /**
     * Tests if a directory is "blaze" enabled by checking if the directory contains
     * a blaze.jar file.  Please note that other build tools may be in the same
     * directory.
     * @param dir The directory to test
     * @return True if blazed or false otherwise
     */
    static public boolean isBlazed(File dir) {
        File blazeJarFile = findBlazeJar(dir);
        return blazeJarFile != null;
    }
    
    /**
     * Tests if a directory is "blaze" enabled and no other well-known build
     * tools are present in the same directory. 
     * @param dir The directory to test
     * @return True if blazed or false otherwise
     */
    static public boolean isOnlyBlazed(File dir) {
        return isBlazed(dir)
            && !fileExists(dir, "pom.xml")
            && !fileExists(dir, "build.xml")
            && !fileExists(dir, "build.gradle");
    }
    
    /**
     * Finds the blaze.jar for a given directory or null if it doesn't exist.
     * @param dir The directory to search
     * @return The blaze.jar file or null if not found
     */
    static public File findBlazeJar(File dir) {
        if (dir == null) {
            return null;
        }
        
        File blazeJarFile = new File(dir, "blaze.jar"); 
        
        if (!blazeJarFile.exists()) {
            return null;
        }
        
        return blazeJarFile;
    }
    
    /**
     * Resolves the dependencies for a script file.  This method is blocking
     * and may take awhile to complete.
     * @param scriptFile The script to resolve dependencies for
     * @return A list of jar files that represent the complete list of dependencies
     */
    static public List<File> resolveScriptDependencies(File scriptFile) {
        Blaze.Builder builder = Blaze.builder()
            .file(scriptFile);
        
        builder.resolveDependencies();
        
        // TODO: javadocs would be great to include as well...
        
        return builder.getDependencyJarFiles();
    }
    
    static public final Set<String> SCRIPT_EXCLUDE_FILE_EXTS = new HashSet<>();
    static {
        SCRIPT_EXCLUDE_FILE_EXTS.add(".jar");
        SCRIPT_EXCLUDE_FILE_EXTS.add(".conf");
    }
    
    static public final Set<String> SCRIPT_INCLUDE_FILE_EXTS = new HashSet<>();
    static {
        SCRIPT_INCLUDE_FILE_EXTS.add(".java");
        SCRIPT_INCLUDE_FILE_EXTS.add(".groovy");
        SCRIPT_INCLUDE_FILE_EXTS.add(".js");
        SCRIPT_INCLUDE_FILE_EXTS.add(".kt");
        SCRIPT_INCLUDE_FILE_EXTS.add(".kts");
    }
    
    /**
     * Assuming filename is already in a script root, is it a blaze script?
     * 
     * @param fileName
     * @return 
     */
    static public boolean isBlazeScript(String fileName) {
        int pos = fileName.lastIndexOf(".");
        if (pos < 0) {
            return false;
        }
        
        String fileExtension = fileName.substring(pos);
        
        if (SCRIPT_EXCLUDE_FILE_EXTS.contains(fileExtension)) {
            return false;
        }
        
        if (SCRIPT_INCLUDE_FILE_EXTS.contains(fileExtension)) {
            return true;
        } else {
            return false;
        }
    }
    
    static public boolean isBlazeScript(Iterable<File> scriptRoots, File file) {
        if (file == null) {
            return false;
        }
        
        if (scriptRoots == null) {
            LOG.log(Level.WARNING, "scriptRoots was null or empty (unable to evaluate if file {0} is a blaze script", file);
            return false;
        }
        
        String fileExtension = FileHelper.fileExtension(file);
        
        if (SCRIPT_EXCLUDE_FILE_EXTS.contains(fileExtension)) {
            return false;
        }
        
        if (SCRIPT_INCLUDE_FILE_EXTS.contains(fileExtension)) {
            // definitely a possible script
        } else {
            return false;
        }
        
        // is the file in the script root?
        for (File scriptRoot : scriptRoots) {
            if (file.getParentFile().getAbsoluteFile().equals(scriptRoot.getAbsoluteFile())) {
                return true;
            }
        }
        
        return false;
    }
    
}

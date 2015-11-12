/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.fizzed.blaze.netbeans;

import java.io.File;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.netbeans.api.project.Project;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 * Blaze-only project detection
 * 
 * project/
 *   blaze.jar
 *   !pom.xml || !build.xml || !build.gradle
 * 
 * 
 * Blaze-added project detection
 * 
 * project/
 *   pom.xml
 *   blaze.jar         -> its presence in project root enables blaze project plugin
 *   blaze.java        -> easy to detect this is a blaze script...
 *   scripts
 * 
 * @author joelauer
 */
public class BlazeNetbeansProjects {
    
    static private final Map<File,BlazeNetbeansProject> REGISTRY = new ConcurrentHashMap<>();
    
    static public BlazeNetbeansProject find(Project project) {
        File projectDir = FileUtil.toFile(project.getProjectDirectory());
        return REGISTRY.get(projectDir);
    }
    
    static public void register(Project project, BlazeNetbeansProject hook) {
        File projectDir = FileUtil.toFile(project.getProjectDirectory());
        REGISTRY.put(projectDir, hook);
    }
    
    static public void unregister(Project project) {
        File projectDir = FileUtil.toFile(project.getProjectDirectory());
        REGISTRY.remove(projectDir);
    }
    
    static public File getProjectDirectory(Project project) {
        FileObject projectFileObject = project.getProjectDirectory();
        return FileUtil.toFile(projectFileObject);
    }
    
}

package com.fizzed.blaze.netbeans;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.classpath.GlobalPathRegistry;
import org.netbeans.spi.java.classpath.ClassPathFactory;
import org.netbeans.spi.java.classpath.ClassPathImplementation;
import org.netbeans.spi.java.classpath.PathResourceImplementation;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;

/**
 * Mutable & optionally registered class path.  Fires event when jars are modified.
 */
public class BlazeClassPath implements ClassPathImplementation {
    static final private Logger LOG = Logger.getLogger(BlazeClassPath.class.getCanonicalName());

    private final PropertyChangeSupport changes;
    private final AtomicReference<List<URL>> jars;
    private final AtomicReference<List<PathResourceImplementation>> resources;
    private final String type;
    private ClassPath[] underlyingClassPath;
    private boolean registered;
    
    public BlazeClassPath(String type, Iterable<URL> jars, boolean register) {
        this.jars = new AtomicReference<>();
        this.resources = new AtomicReference<>();
        this.type = type;
        this.changes = new PropertyChangeSupport(this);
        internalSetJars(jars);
        buildUnderlying(register);
    }

    public ClassPath getUnderlyingClassPath() {
        return underlyingClassPath[0];
    }
    
    final public void setJars(Iterable<URL> jars) {
        internalSetJars(jars);
        changes.firePropertyChange(ClassPathImplementation.PROP_RESOURCES, null, null);
    }
    
    final public void internalSetJars(Iterable<URL> jars) {
        List<URL> newJars = new ArrayList<>();
        List<PathResourceImplementation> newResources = new ArrayList<>();
        for (URL jar : jars) {
            newJars.add(jar);
            newResources.add(ClassPathSupport.createResource(jar));
        }
        this.jars.set(newJars);
        this.resources.set(newResources);
    }
    
    final public void buildUnderlying(boolean register) {
        this.underlyingClassPath = new ClassPath[] { ClassPathFactory.createClassPath(this) };
        if (register) {
            LOG.log(Level.INFO,"Registering " + this.type + " with {0}", this.underlyingClassPath);
            GlobalPathRegistry.getDefault().register(this.type, this.underlyingClassPath);
            this.registered = true;
        }
    }
    
    final public boolean isRegistered() {
        return this.registered;
    }
    
    public List<URL> getJars() {
        return this.jars.get();
    }
    
    @Override
    public List<PathResourceImplementation> getResources() {
        return this.resources.get();
    }

    @Override
    public void addPropertyChangeListener(PropertyChangeListener listener) { 
        changes.addPropertyChangeListener(listener);
    }

    @Override
    public void removePropertyChangeListener(PropertyChangeListener listener) {
        changes.removePropertyChangeListener(listener);
    }
}


package com.fizzed.blaze.netbeans;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;

public class AggregateBlazeClassPath extends BlazeClassPath implements PropertyChangeListener {

    private final CopyOnWriteArrayList<BlazeClassPath> classpaths;
    
    public AggregateBlazeClassPath(String type, Iterable<BlazeClassPath> classpaths, boolean register) {
        super(type, Collections.<URL>emptyList(), register);
        this.classpaths = new CopyOnWriteArrayList<>(makeCollection(classpaths));
        rebuildResources();
    }

    final public void addClassPath(BlazeClassPath classpath) {
        this.classpaths.add(classpath);
        classpath.addPropertyChangeListener(this);
        rebuildResources();
    }
    
    public final void rebuildResources() {
        Set<URL> newUrls = new LinkedHashSet<>();
        
        for (BlazeClassPath cp : classpaths) {
            newUrls.addAll(cp.getJars());
        }
        
        this.setJars(newUrls);
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        rebuildResources();
    }
    
    public static <E> Collection<E> makeCollection(Iterable<E> iter) {
        Collection<E> list = new ArrayList<>();
        for (E item : iter) {
            list.add(item);
        }
        return list;
    }
    
}

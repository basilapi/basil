package uk.ac.open.kmi.stoner;

import javax.ws.rs.core.Application;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by Luca Panziera on 09/01/15.
 */
public class Stoner extends Application {
    @Override
    public Set<Class<?>> getClasses() {
        Set<Class<?>> s = new HashSet<Class<?>>();
        s.add(HelloWorldResource.class);
        s.add(SpecificationResource.class);
        return s;
    }
}


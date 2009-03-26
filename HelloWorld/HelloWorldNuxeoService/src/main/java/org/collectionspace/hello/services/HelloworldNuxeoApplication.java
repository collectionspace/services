package org.collectionspace.hello.services;

import javax.ws.rs.core.Application;
import java.util.HashSet;
import java.util.Set;

public class HelloworldNuxeoApplication extends Application {

    private Set<Object> singletons = new HashSet<Object>();
    private Set<Class<?>> empty = new HashSet<Class<?>>();

    public HelloworldNuxeoApplication() {
        singletons.add(new PersonNuxeoResource());
    }

    @Override
    public Set<Class<?>> getClasses() {
        return empty;
    }

    @Override
    public Set<Object> getSingletons() {
        return singletons;
    }
}

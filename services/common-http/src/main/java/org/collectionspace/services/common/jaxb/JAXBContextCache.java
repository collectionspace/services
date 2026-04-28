package org.collectionspace.services.common.jaxb;

import java.util.concurrent.ConcurrentHashMap;

import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;

/**
 * Store our JAXBContexts so we don't need to reinitialize all the time. Note that we have to store a single context per
 * class so that we don't pollute the namespace when marshalling XML.
 * </p>
 * Might be worth exploring if this should be a ContextResolver instead of a singleton.
 *
 * @since 9.0.0
 */
public class JAXBContextCache {
    private static final JAXBContextCache INSTANCE = new JAXBContextCache();

    /**
     * Store a cache of JAXBContexts so that the namespace does not get polluted when marshalling
     *
     * TODO: Class or String? String gets us support for package based JAXBContext, which means fewer instances total
     */
    ConcurrentHashMap<String, JAXBContext> contextCache = new ConcurrentHashMap<>();

    private JAXBContextCache() {
    }

    public static JAXBContextCache getInstance() {
        return INSTANCE;
    }

    public JAXBContext getCachedJAXBContext(Class<?> type) throws JAXBException {
        // skip computeIsAbsent b/c newInstance can throw an exception
        final var packageName = type.getPackageName();
        JAXBContext context = contextCache.get(packageName);
        if (context == null) {
            context = JAXBContext.newInstance(packageName);
            contextCache.putIfAbsent(packageName, context);
        }

        return context;
    }

}

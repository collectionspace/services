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

    public JAXBContext getCachedJAXBContext(final Class<?> type) throws JAXBException {
        final var packageName = type.getPackageName();
        return getCachedJAXBContext(packageName);
    }

    public JAXBContext getCachedJAXBContext(final String packageName) throws JAXBException {
        // avoid computeIfAbsent b/c newInstance can throw an exception
        JAXBContext context = contextCache.get(packageName);
        if (context == null) {
            context = JAXBContext.newInstance(packageName);
            contextCache.putIfAbsent(packageName, context);
        }
        return context;
    }
}

package org.collectionspace.services.systeminfo.listener;

import java.lang.reflect.Field;
import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import io.micrometer.core.instrument.Metrics;
import org.apache.catalina.core.ApplicationContext;
import org.apache.catalina.core.StandardContext;
import org.collectionspace.services.systeminfo.MeterRegistryProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A listener to help with init and shutdown of the MeterRegistry
 */
public class MeterRegistryContextListener implements ServletContextListener {

    private static final Logger log = LoggerFactory.getLogger(MeterRegistryContextListener.class);

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        if (MeterRegistryProvider.isEnabled()) {
            registerTomcatMetrics(sce.getServletContext(), MeterRegistryProvider.getInstance());
        }
    }

    /**
     * Try to access the Tomcat Manager in order for our MeterRegistry to create a TomcatMetrics binding
     * <p>
     * Note that we have to get the Tomcat Manager through navigating the following internal Tomcat classes:
     * ApplicationContextFacade -> ApplicationContext -> StandardContext
     * <p>
     * If anything fails, just log a warning and allow startup to continue
     *
     * @param servletContext The servlet context
     * @param registryProvider The MeterRegistryProvider singleton instance
     */
    private void registerTomcatMetrics(ServletContext servletContext, MeterRegistryProvider registryProvider) {
        try {
            // get the ApplicationContextField from the ApplicationContextFacade (the ServletContext we're given)
            Field applicationContextField = servletContext.getClass().getDeclaredField("context");
            applicationContextField.setAccessible(true);
            ApplicationContext appContextObj = (ApplicationContext) applicationContextField.get(servletContext);

            // then the StandardContext field...
            Field standardContextField = appContextObj.getClass().getDeclaredField("context");
            standardContextField.setAccessible(true);
            StandardContext standardContextObj = (StandardContext) standardContextField.get(appContextObj);

            // Finally get the manager
            final var manager = standardContextObj.getManager();
            registryProvider.registerTomcatMetrics(manager);
        } catch (ReflectiveOperationException e) {
            log.warn("Unable to get Tomcat Manager", e);
        }
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        if (MeterRegistryProvider.isEnabled()) {
            Metrics.removeRegistry(MeterRegistryProvider.getInstance().getRegistry());
            MeterRegistryProvider.getInstance().close();
        }
    }

}

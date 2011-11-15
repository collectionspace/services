/**
 * Copyright 2009 University of California at Berkeley
 */
package org.collectionspace.services.common;

import org.collectionspace.services.common.log.CollectionSpaceLog4jRepositorySelector;
import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import org.apache.log4j.LogManager;

/**
 * CollectionSpaceServiceContextListener is a ServletContextListener that helps initialize
 * the services layer at deployment and undeployment times
 */
public class CollectionSpaceServiceContextListener implements ServletContextListener {

    @Override
    public void contextInitialized(ServletContextEvent event) {
        try {
            //create logging repository select to stop jboss from jamming
            //our log on top of theirs
//            LogManager.setRepositorySelector(new CollectionSpaceLog4jRepositorySelector(),
//                    null);

        	//
        	// Initialize/start the Nuxeo EP server instance and create/retrieve the service workspaces
        	//
        	ServletContext servletContext = event.getServletContext();
            ServiceMain svcMain = ServiceMain.getInstance(servletContext);
            svcMain.retrieveAllWorkspaceIds();

        } catch (Exception e) {
            e.printStackTrace();
            //fail here
            throw new RuntimeException(e);
        }
    }

    @Override
    public void contextDestroyed(ServletContextEvent event) {
        ServiceMain.getInstance().release();
    }
}

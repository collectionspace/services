/**
 * Copyright 2009 University of California at Berkeley
 */
package org.collectionspace.services.common;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.collectionspace.services.common.storage.jpa.JpaStorageUtils;

/**
 * CollectionSpaceServiceContextListener is a ServletContextListener that helps initialize
 * the services layer at deployment and undeployment times
 */
public class CollectionSpaceServiceContextListener implements ServletContextListener {

    @Override
    public void contextInitialized(ServletContextEvent event) {
        try {            
            //
            // Initialize/start the Nuxeo EP server instance and create/retrieve the service workspaces
            //
            ServletContext servletContext = event.getServletContext();
            ServiceMain svcMain = ServiceMain.getInstance(servletContext);
            
            svcMain.retrieveAllWorkspaceIds();
            
            // Create required indexes (aka indices) in tables not associated
            // with any specific tenant.
            svcMain.createRequiredIndices();
            //
            // Invoke all post-initialization handlers, passing in a DataSource instance of the Nuxeo db.
            // Typically, these handlers modify column types and add indexes to the Nuxeo db schema.
            //
            svcMain.firePostInitHandlers();
                        
        } catch (Throwable e) {
            e.printStackTrace();
            //fail here
            System.err.println("[ERROR] ***");
            System.err.println("[ERROR] The CollectionSpace Services could not initialize.  Please see the log files for details.");
            System.err.println("[ERROR] ***");
//            throw new RuntimeException(e);
        }
    }

    @Override
    public void contextDestroyed(ServletContextEvent event) {
        ServiceMain instance = null;
        
        try {
        	ServiceMain.getInstance();
        } catch (Throwable t) {
        	// Do nothing.  Error already logged by the Services layer
        } finally {
	        if (instance != null) {
	        	instance.release();
	        } else {
	        	System.err.println("ERROR: The CollectionSpace Services layer failed to startup successfully.  Look in the tomcat logs and cspace-services logs for details.");
	        }
	        JpaStorageUtils.releaseEntityManagerFactories();
        }
    }
}

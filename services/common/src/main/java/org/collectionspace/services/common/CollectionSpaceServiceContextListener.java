/**
 * Copyright 2009 University of California at Berkeley
 */
package org.collectionspace.services.common;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.collectionspace.services.client.AuditClientUtils;
import org.collectionspace.services.common.storage.jpa.JpaStorageUtils;

import org.nuxeo.ecm.core.event.EventServiceAdmin;
import org.nuxeo.ecm.core.event.impl.EventListenerDescriptor;
import org.nuxeo.ecm.core.event.impl.EventListenerList;
import org.nuxeo.runtime.api.Framework;

/**
 * CollectionSpaceServiceContextListener is a ServletContextListener that helps initialize
 * the services layer at deployment and undeployment times
 */
public class CollectionSpaceServiceContextListener implements ServletContextListener {

	//
	// Get the current enabled state of the CSpace audit logger
	//
	private boolean isCSpaceAuditLoggerEnabled() {
        EventServiceAdmin eventAdmin = Framework.getService(EventServiceAdmin.class);
        
        EventListenerList listenerList = eventAdmin.getListenerList();
        EventListenerDescriptor descriptor = listenerList.getDescriptor(AuditClientUtils.SERVICE_LISTENER_NAME);
        
        return descriptor.isEnabled();
	}

	private void enableCSpaceAuditLogger(boolean state) {
        EventServiceAdmin eventAdmin = Framework.getService(EventServiceAdmin.class);
    	//
		// enable/disable audit logs when initializing authorities
    	//
        eventAdmin = Framework.getService(EventServiceAdmin.class);
        eventAdmin.setListenerEnabledFlag(AuditClientUtils.SERVICE_LISTENER_NAME, state);
	}
	
    @Override
    public void contextInitialized(ServletContextEvent event) {
    	Boolean cspaceAuditLoggerState = null;
    	
        try {
            //
            // Initialize/start the Nuxeo EP server instance and create/retrieve the service workspaces
            //
            ServletContext servletContext = event.getServletContext();
            ServiceMain svcMain = ServiceMain.getInstance(servletContext);
            
    		cspaceAuditLoggerState = isCSpaceAuditLoggerEnabled(); // get the state of the audit logger
    		enableCSpaceAuditLogger(false);

            svcMain.retrieveAllWorkspaceIds();

            // Upgrade database schema
            svcMain.upgradeDatabase();

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
        } finally {
        	if (cspaceAuditLoggerState != null) {
        		enableCSpaceAuditLogger(cspaceAuditLoggerState); // restore the state of the audit logger
        	}
        }
    }

    @Override
    public void contextDestroyed(ServletContextEvent event) {
        ServiceMain instance = null;

        try {
        	instance = ServiceMain.getInstance();
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

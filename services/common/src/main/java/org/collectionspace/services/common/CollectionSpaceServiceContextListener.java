/**
 * Copyright 2009 University of California at Berkeley
 */
package org.collectionspace.services.common;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

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
            
        	{
        		System.out.print("About to retrieveAllWorkspaceIds - Pausing 4:5 seconds for you to attached the debugger");
        		long startTime, currentTime;
        		currentTime = startTime = System.currentTimeMillis();
        		long stopTime = startTime + 5 * 1000; //5 seconds
        		do {
        			if (currentTime % 1000 == 0) {
        				System.out.print(".");
        			}
        			currentTime = System.currentTimeMillis();
        		} while (currentTime < stopTime);
        			
        		System.out.println();
        		System.out.println("Resuming cspace services initialization.");
        	}
            
            svcMain.retrieveAllWorkspaceIds();

        	{
        		System.out.print("About to firePostInitHandlers - Pausing 5:5 seconds for you to attached the debugger");
        		long startTime, currentTime;
        		currentTime = startTime = System.currentTimeMillis();
        		long stopTime = startTime + 5 * 1000; //5 seconds
        		do {
        			if (currentTime % 1000 == 0) {
        				System.out.print(".");
        			}
        			currentTime = System.currentTimeMillis();
        		} while (currentTime < stopTime);
        			
        		System.out.println();
        		System.out.println("Resuming cspace services initialization.");
        	}
            
        	//
        	// Invoke all post-initialization handlers, passing in a DataSource instance of the Nuxeo db.
        	// Typically, these handlers modify column types and add indexes to the Nuxeo db schema.
        	//
        	svcMain.firePostInitHandlers();
            
        	{
        		System.out.print("Finished to firePostInitHandlers - Pausing 6:5 seconds for you to attached the debugger");
        		long startTime, currentTime;
        		currentTime = startTime = System.currentTimeMillis();
        		long stopTime = startTime + 5 * 1000; //5 seconds
        		do {
        			if (currentTime % 1000 == 0) {
        				System.out.print(".");
        			}
        			currentTime = System.currentTimeMillis();
        		} while (currentTime < stopTime);
        			
        		System.out.println();
        		System.out.println("Resuming cspace services initialization.");
        	}
        	
        } catch (Exception e) {
            e.printStackTrace();
            //fail here
            System.err.println("[ERROR] The CollectionSpace Services could not initialize.  Please see the log files for details.");
            throw new RuntimeException(e);
        }
    }

    @Override
    public void contextDestroyed(ServletContextEvent event) {
        //ServiceMain.getInstance().release();
    }
}

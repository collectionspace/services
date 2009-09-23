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
        try{
            ServletContext sc = event.getServletContext();
            ServiceMain svcMain = ServiceMain.getInstance(); //first access initializes as well
            svcMain.retrieveAllWorkspaceIds();
        }catch(Exception e){
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

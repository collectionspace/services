package org.collectionspace.services.jaxrs;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;

import org.jboss.resteasy.core.Dispatcher;
import org.jboss.resteasy.plugins.server.servlet.ResteasyBootstrap;
import org.collectionspace.services.common.ResourceMap;
import org.collectionspace.services.common.ServiceMain;

public class CSpaceResteasyBootstrap extends ResteasyBootstrap {
	
//	private static final String 
	public void  contextInitialized(ServletContextEvent event) {
    	if (true) {
    		System.out.print("Pausing 1 seconds in RESTEasy bootstrap for you to attached the debugger");
    		long startTime, currentTime;
    		currentTime = startTime = System.currentTimeMillis();
    		long stopTime = startTime + 1 * 1000; //5 seconds
    		do {
    			if (currentTime % 1000 == 0) {
    				System.out.print(".");
    			}
    			currentTime = System.currentTimeMillis();
    		} while (currentTime < stopTime);
    			
    		System.out.println();
    		System.out.println("Resuming RESTEasy bootstrap initialization.");
    	}
		
//    	// Save a copy of the ServletContext inside our ServiceMain singleton
//		ServletContext servletContext = event.getServletContext();
//		try {
//			ServiceMain.getInstance(servletContext); //First access causes initialization of the Services' main class
//		} catch (RuntimeException e) {
//			e.printStackTrace();
//			//rethrow the exception
//			throw e;
//		}
		
		//
    	// This call to super instantiates and initializes our JAX-RS application class.
    	// The application class is org.collectionspace.services.jaxrs.CollectionSpaceJaxRsApplication.
    	//
		super.contextInitialized(event);
		CollectionSpaceJaxRsApplication app = 
			(CollectionSpaceJaxRsApplication)deployment.getApplication();
		Dispatcher disp = deployment.getDispatcher();
		disp.getDefaultContextObjects().put(ResourceMap.class, app.getResourceMap());
	}

}

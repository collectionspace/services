package org.collectionspace.services.jaxrs;

import javax.servlet.ServletContextEvent;

import org.jboss.resteasy.core.Dispatcher;
import org.jboss.resteasy.plugins.server.servlet.ResteasyBootstrap;
import org.collectionspace.services.common.ResourceMap;

public class CSpaceResteasyBootstrap extends ResteasyBootstrap {
	
	public void  contextInitialized(ServletContextEvent event) {
		super.contextInitialized(event);
		CollectionSpaceJaxRsApplication app = 
			(CollectionSpaceJaxRsApplication)deployment.getApplication();
		Dispatcher disp = deployment.getDispatcher();
		disp.getDefaultContextObjects().put(ResourceMap.class, app.getResourceMap());
	}

}

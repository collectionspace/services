package org.collectionspace.services.jaxrs;

import javax.servlet.ServletContextEvent;
import javax.ws.rs.core.Response;

import org.jboss.resteasy.core.Dispatcher;
import org.jboss.resteasy.plugins.server.servlet.ResteasyBootstrap;
import org.collectionspace.authentication.CSpaceTenant;
import org.collectionspace.services.authorization.AuthZ;
import org.collectionspace.services.client.PersonAuthorityClient;
import org.collectionspace.services.client.PersonAuthorityClientUtils;
import org.collectionspace.services.client.PoxPayloadOut;
import org.collectionspace.services.person.PersonAuthorityResource;
import org.collectionspace.services.common.ResourceMap;
import org.collectionspace.services.common.ServiceMain;
import org.collectionspace.services.common.config.TenantBindingConfigReaderImpl;
import org.collectionspace.services.config.service.AuthorityInstanceType;
import org.collectionspace.services.config.service.ServiceBindingType;
import org.collectionspace.services.config.service.ServiceBindingType.AuthorityInstanceList;
import org.collectionspace.services.config.tenant.TenantBindingType;

import java.util.Date;
import java.util.Hashtable;
import java.util.List;

public class CSpaceResteasyBootstrap extends ResteasyBootstrap {
	
	@Override
	public void  contextInitialized(ServletContextEvent event) {
		try {
			//
	    	// This call to super instantiates and initializes our JAX-RS application class.
	    	// The application class is org.collectionspace.services.jaxrs.CollectionSpaceJaxRsApplication.
	    	//
	    	System.out.println(String.format("%tc [INFO] Starting up the CollectionSpace Services' JAX-RS application.", new Date()));
			super.contextInitialized(event);
			CollectionSpaceJaxRsApplication app = (CollectionSpaceJaxRsApplication)deployment.getApplication();
			Dispatcher disp = deployment.getDispatcher();
			disp.getDefaultContextObjects().put(ResourceMap.class, app.getResourceMap());
			
			initializeAuthorities(app.getResourceMap());
			
	    	System.out.println(String.format("%tc [INFO] CollectionSpace Services' JAX-RS application started.", new Date()));
		} catch (Throwable e) {
			e.printStackTrace();
			throw e;
		}
	}
	
    @Override
    public void contextDestroyed(ServletContextEvent event) {
    	System.out.println("[INFO] Shutting down the CollectionSpace Services' JAX-RS application.");
    	//Do something if needed.
    	System.out.println("[INFO] CollectionSpace Services' JAX-RS application stopped.");
    }	

    public void initializeAuthorities(ResourceMap resourceMap) {
    	TenantBindingConfigReaderImpl tenantBindingConfigReader = ServiceMain.getInstance().getTenantBindingConfigReader();    	
    	Hashtable<String, TenantBindingType> tenantBindingsTable = tenantBindingConfigReader.getTenantBindings(false);
    	for (TenantBindingType tenantBindings : tenantBindingsTable.values()) {
    		for (ServiceBindingType serviceBinding : tenantBindings.getServiceBindings()) {
    			AuthorityInstanceList element = serviceBinding.getAuthorityInstanceList();
    			if (element != null && element.getAuthorityInstance() != null) {
    				List<AuthorityInstanceType> authorityInstanceList = element.getAuthorityInstance();
    				for (AuthorityInstanceType authorityInstance : authorityInstanceList) {
    					CSpaceTenant tenant = new CSpaceTenant(tenantBindings.getId(), tenantBindings.getName());
    					initializeAuthorityInstance(resourceMap, authorityInstance, serviceBinding.getName(), tenant);
    				}
    			}
    		}
    	}
	}

    private void initializeAuthorityInstance(ResourceMap resourceMap, AuthorityInstanceType authorityInstance, String serviceName, CSpaceTenant tenant) {
		// TODO Auto-generated method stub
		try {
			AuthZ.get().login(tenant);
			PersonAuthorityClient client = new PersonAuthorityClient();			
			PoxPayloadOut xmlPayloadOut = PersonAuthorityClientUtils.createPersonAuthorityInstance(
					authorityInstance.getTitle(), authorityInstance.getTitleRef(), client.getCommonPartName());
			String xmlPayload = xmlPayloadOut.asXML();
			PersonAuthorityResource personAuthorityResource = (PersonAuthorityResource) resourceMap.get(serviceName.toLowerCase());
			Response response = personAuthorityResource.createAuthority(xmlPayload);
			int status = response.getStatus();
						
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void initializeVocabularies() {
		// TODO Auto-generated method stub
		
	}

}

package org.collectionspace.services.jaxrs;

import static org.nuxeo.elasticsearch.ElasticSearchConstants.ES_ENABLED_PROPERTY;

import javax.servlet.ServletContextEvent;
import javax.ws.rs.core.Response;

import org.jboss.resteasy.core.Dispatcher;
import org.jboss.resteasy.plugins.server.servlet.ResteasyBootstrap;

import org.collectionspace.authentication.CSpaceTenant;
import org.collectionspace.services.account.Tenant;
import org.collectionspace.services.account.TenantResource;
import org.collectionspace.services.authorization.AuthZ;
import org.collectionspace.services.client.AuditClient;
import org.collectionspace.services.client.AuditClientUtils;
import org.collectionspace.services.client.AuthorityClient;

import org.collectionspace.services.common.CSWebApplicationException;
import org.collectionspace.services.common.ResourceMap;
import org.collectionspace.services.common.ServiceMain;
import org.collectionspace.services.common.api.RefName;
import org.collectionspace.services.common.config.ConfigUtils;
import org.collectionspace.services.common.config.TenantBindingConfigReaderImpl;
import org.collectionspace.services.common.vocabulary.AuthorityResource;

import org.collectionspace.services.config.service.AuthorityInstanceType;
import org.collectionspace.services.config.service.ServiceBindingType;
import org.collectionspace.services.config.service.ServiceBindingType.AuthorityInstanceList;
import org.collectionspace.services.config.service.Term;
import org.collectionspace.services.config.service.TermList;
import org.collectionspace.services.config.tenant.TenantBindingType;
import org.collectionspace.services.nuxeo.util.NuxeoUtils;

import org.nuxeo.ecm.core.event.EventServiceAdmin;
import org.nuxeo.ecm.core.event.impl.EventListenerDescriptor;
import org.nuxeo.ecm.core.event.impl.EventListenerList;
import org.nuxeo.elasticsearch.ElasticSearchComponent;
import org.nuxeo.elasticsearch.api.ElasticSearchService;
import org.nuxeo.runtime.api.Framework;

import java.lang.reflect.Constructor;
import java.util.Date;
import java.util.Hashtable;
import java.util.List;
import java.util.logging.Level;

public class CSpaceResteasyBootstrap extends ResteasyBootstrap {

	java.util.logging.Logger logger = java.util.logging.Logger.getAnonymousLogger();
	static final String RESET_AUTHORITIES_PROPERTY = "org.collectionspace.services.authorities.reset";
	private static final String RESET_ELASTICSEARCH_INDEX_PROPERTY = "org.collectionspace.services.elasticsearch.reset";
	private static final String QUICK_BOOT_PROPERTY = "org.collectionspace.services.quickboot";

	@Override
	public void  contextInitialized(ServletContextEvent event) {
		try {
			//
	    	// This call to super instantiates and initializes our JAX-RS application class.
	    	// The application class is org.collectionspace.services.jaxrs.CollectionSpaceJaxRsApplication.
	    	//
			logger.log(Level.INFO, String.format("%tc [INFO] Starting up the CollectionSpace Services' JAX-RS application.", new Date()));
			super.contextInitialized(event);
			CollectionSpaceJaxRsApplication app = (CollectionSpaceJaxRsApplication)deployment.getApplication();
			Dispatcher disp = deployment.getDispatcher();
			disp.getDefaultContextObjects().put(ResourceMap.class, app.getResourceMap());

			String quickBoot = System.getProperty(QUICK_BOOT_PROPERTY, Boolean.FALSE.toString()); // Property can be set in the tomcat/bin/setenv.sh (or setenv.bat) file
			if (Boolean.valueOf(quickBoot) == false) {
				String resetAuthsString = System.getProperty(RESET_AUTHORITIES_PROPERTY, Boolean.FALSE.toString()); // Property can be set in the tomcat/bin/setenv.sh (or setenv.bat) file
				initializeAuthorities(app.getResourceMap(), Boolean.valueOf(resetAuthsString));

				String resetElasticsearchIndexString = System.getProperty(RESET_ELASTICSEARCH_INDEX_PROPERTY, Boolean.FALSE.toString()); // Property can be set in the tomcat/bin/setenv.sh (or setenv.bat) file

				if (Boolean.valueOf(resetElasticsearchIndexString) == true) {
					resetElasticSearchIndex();
				}
			}

			logger.log(Level.INFO, String.format("%tc [INFO] CollectionSpace Services' JAX-RS application started.", new Date()));
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}


	@Override
	public void contextDestroyed(ServletContextEvent event) {
		logger.log(Level.INFO, "[INFO] Shutting down the CollectionSpace Services' JAX-RS application.");
		//Do something if needed.
		logger.log(Level.INFO, "[INFO] CollectionSpace Services' JAX-RS application stopped.");
	}

	public void resetElasticSearchIndex() throws Exception {
		boolean isEnabled = Boolean.parseBoolean(Framework.getProperty(ES_ENABLED_PROPERTY, "true"));

		if (!isEnabled) {
			return;
		}

		ElasticSearchComponent es = (ElasticSearchComponent) Framework.getService(ElasticSearchService.class);

		for (String repositoryName : es.getRepositoryNames()) {
			logger.log(Level.INFO, String.format("%tc [INFO] Rebuilding Elasticsearch index for repository %s", new Date(), repositoryName));

			es.dropAndInitRepositoryIndex(repositoryName);
		}

		TenantBindingConfigReaderImpl tenantBindingConfigReader = ServiceMain.getInstance().getTenantBindingConfigReader();
		Hashtable<String, TenantBindingType> tenantBindingsTable = tenantBindingConfigReader.getTenantBindings(false);

		for (TenantBindingType tenantBinding : tenantBindingsTable.values()) {
			CSpaceTenant tenant = new CSpaceTenant(tenantBinding.getId(), tenantBinding.getName());

			AuthZ.get().login(tenant);

			for (ServiceBindingType serviceBinding : tenantBinding.getServiceBindings()) {
				Boolean isElasticsearchIndexed = serviceBinding.isElasticsearchIndexed();
				String servicesRepoDomainName = serviceBinding.getRepositoryDomain();

				if (isElasticsearchIndexed && servicesRepoDomainName != null && servicesRepoDomainName.trim().isEmpty() == false) {
					String repositoryName = ConfigUtils.getRepositoryName(tenantBinding, servicesRepoDomainName);
					String docType = NuxeoUtils.getTenantQualifiedDocType(tenantBinding.getId(), serviceBinding.getObject().getName());

					logger.log(Level.INFO, String.format("%tc [INFO] Starting Elasticsearch reindexing for docType %s in repository %s", new Date(), docType, repositoryName));

					es.runReindexingWorker(repositoryName, String.format("SELECT ecm:uuid FROM %s", docType));
				}
			}
		}
	}

	//
	// Get the current enabled state of the CSpace audit logger
	//
	private boolean isCSpaceAuditLoggerEnabled() {
        EventServiceAdmin eventAdmin = Framework.getService(EventServiceAdmin.class);
        
        EventListenerList listenerList = eventAdmin.getListenerList();
        EventListenerDescriptor descriptor = listenerList.getDescriptor(AuditClientUtils.SERVICE_LISTENER_NAME);

        return descriptor.isEnabled();
	}

    /**
     * Initialize all authorities and vocabularies defined in the service bindings.
     * @param resourceMap
     * @throws Exception
     */
    public void initializeAuthorities(ResourceMap resourceMap, boolean reset) throws Exception {
    	TenantBindingConfigReaderImpl tenantBindingConfigReader = ServiceMain.getInstance().getTenantBindingConfigReader();
    	Hashtable<String, TenantBindingType> tenantBindingsTable = tenantBindingConfigReader.getTenantBindings(false);
    	
        EventServiceAdmin eventAdmin = Framework.getService(EventServiceAdmin.class);
		boolean cspaceAuditLoggerState = isCSpaceAuditLoggerEnabled();
    	try {
    		// disable audit logs when initializing authorities
	        eventAdmin = Framework.getService(EventServiceAdmin.class);
	        eventAdmin.setListenerEnabledFlag(AuditClientUtils.SERVICE_LISTENER_NAME, false);
	
	    	for (TenantBindingType tenantBindings : tenantBindingsTable.values()) {
				CSpaceTenant tenant = new CSpaceTenant(tenantBindings.getId(), tenantBindings.getName());
				if (shouldInitializeAuthorities(tenant, reset) == true) {
					logger.log(Level.INFO, String.format("Initializing vocabularies and authorities of tenant '%s'.",
							tenant.getId()));
		    		for (ServiceBindingType serviceBinding : tenantBindings.getServiceBindings()) {
		    			AuthorityInstanceList element = serviceBinding.getAuthorityInstanceList();
		    			if (element != null && element.getAuthorityInstance() != null) {
		    				List<AuthorityInstanceType> authorityInstanceList = element.getAuthorityInstance();
		    				for (AuthorityInstanceType authorityInstance : authorityInstanceList) {
		    					try {
		    						initializeAuthorityInstance(resourceMap, authorityInstance, serviceBinding, tenant, reset);
		    					} catch (Exception e) {
		    						logger.log(Level.SEVERE, "Could not initialize authorities and authority terms: " + e.getMessage());
		    						throw e;
		    					}
		    				}
		    			}
		    		}
		    		//
		    		// If we made it this far, we've either created the tenant's authorities and terms or we've reset them.  Either way,
		    		// we should mark the isAuthoritiesInitialized field of the tenant to 'true'.
		    		//
		    		setAuthoritiesInitialized(tenant, true);
				}
	    	}
    	} finally {
    		// set the enabled state of the audit logger back to what it was
	        eventAdmin.setListenerEnabledFlag(AuditClientUtils.SERVICE_LISTENER_NAME, cspaceAuditLoggerState);
    	}
	}

    @SuppressWarnings("rawtypes")
	private AuthorityClient getAuthorityClient(String classname) throws Exception {
        Class clazz = Class.forName(classname.trim());
        Constructor co = clazz.getConstructor(null);
        Object classInstance = co.newInstance(null);
        return (AuthorityClient) classInstance;
    }

    private boolean shouldInitializeAuthorities(CSpaceTenant cspaceTenant, boolean reset) {
		AuthZ.get().login(); // login as super admin
		TenantResource tenantResource = new TenantResource();
		Tenant tenantState = tenantResource.getTenant(cspaceTenant.getId());

		//
		// If the tenant's authorities have been initialized and
		// we're not being asked to reset them, we'll return 'false'
		// making any changes
		//
		return tenantState.isAuthoritiesInitialized() == false || reset == true;
    }

    private void setAuthoritiesInitialized(CSpaceTenant cspaceTenant, boolean initState) {
		AuthZ.get().login(); // login as super admin
		TenantResource tenantResource = new TenantResource();
		Tenant tenantState = tenantResource.getTenant(cspaceTenant.getId());

		tenantState.setAuthoritiesInitialized(initState);
		tenantResource.updateTenant(cspaceTenant.getId(), tenantState);
	}


    /*
     * Check to see if an an authority instance and its corresponding terms exist.  If not, try to create them.
     */
    private void initializeAuthorityInstance(ResourceMap resourceMap,
    		AuthorityInstanceType authorityInstance,
    		ServiceBindingType serviceBinding,
    		CSpaceTenant cspaceTenant,
    		boolean reset) throws Exception {
    	int status = -1;
    	Response response = null;
		String serviceName = serviceBinding.getName();

		AuthZ.get().login(cspaceTenant);
		String clientClassName = serviceBinding.getClientHandler();
		AuthorityClient client = getAuthorityClient(clientClassName);
		String authoritySpecifier = RefName.shortIdToPath(authorityInstance.getTitleRef());  // e.g., urn:cspace:name(ulan)

		//
		// Test to see if the authority instance exists already.
		//
		AuthorityResource authorityResource = (AuthorityResource) resourceMap.get(serviceName.toLowerCase());
		try {
			response = authorityResource.get(null, null, null, authoritySpecifier);
		} catch (CSWebApplicationException e) {
			response = e.getResponse();  // If the authority doesn't exist, we expect a 404 error
		}

		//
		// If it doesn't exist (status is not 200), then try to create the authority instance
		//
		status = response.getStatus();
		if (status != Response.Status.OK.getStatusCode()) {
			String xmlPayload = client.createAuthorityInstance(authorityInstance.getTitleRef(), authorityInstance.getTitle());
			response = authorityResource.createAuthority(xmlPayload);
			status = response.getStatus();
			if (status != Response.Status.CREATED.getStatusCode()) {
				throw new CSWebApplicationException(response);
			}
		}

		if (status == Response.Status.OK.getStatusCode()) {
			logger.log(Level.FINE, String.format("Authority of type '%s' with the short ID of '%s' existed already.",
					serviceName, authorityInstance.getTitleRef()));
		} else if (status == Response.Status.CREATED.getStatusCode()) {
			logger.log(Level.FINE, String.format("Created a new authority of type '%s' with the short ID of '%s'.",
					serviceName, authorityInstance.getTitleRef()));
		} else {
			logger.log(Level.WARNING, String.format("Unknown status '%d' encountered when creating or fetching authority of type '%s' with the short ID of '%s'.",
					serviceName, authorityInstance.getTitleRef()));
		}

		//
		// Next, try to create or verify the authority terms.
		//
		initializeAuthorityInstanceTerms(authorityResource, client, authoritySpecifier, resourceMap, authorityInstance, serviceName, cspaceTenant);
	}

    private void initializeAuthorityInstanceTerms(
    		AuthorityResource authorityResource,
    		AuthorityClient client,
    		String authoritySpecifier,
    		ResourceMap resourceMap,
    		AuthorityInstanceType authorityInstance,
    		String serviceName,
    		CSpaceTenant tenant) throws Exception {

    	int status = -1;
    	Response response = null;

    	TermList termListElement = authorityInstance.getTermList();
    	if (termListElement == null) {
    		return;
    	}

    	for (Term term : termListElement.getTerm()) {
    		//
    		// Check to see if the term already exists
    		//
    		try {
    			String termSpecifier = RefName.shortIdToPath(term.getId());
    			authorityResource.getAuthorityItem(null, null, resourceMap, authoritySpecifier, termSpecifier);
    			status = Response.Status.OK.getStatusCode();
    		} catch (CSWebApplicationException e) {
    			response = e.getResponse();  // If the authority doesn't exist, we expect a 404 error
    			status = response.getStatus();
    		}

    		//
    		// If the term doesn't exist, create it.
    		//
    		if (status != Response.Status.OK.getStatusCode()) {
    			String termShortId = term.getId();
    			String termDisplayName = term.getContent().trim();
	    		String xmlPayload = client.createAuthorityItemInstance(termShortId, termDisplayName);
	    		try {
	    			authorityResource.createAuthorityItem(resourceMap, null, authoritySpecifier, xmlPayload);
	    			logger.log(Level.FINE, String.format("Tenant:%s:Created a new term '%s:%s' in the authority of type '%s' with the short ID of '%s'.",
	    					tenant.getName(), termDisplayName, termShortId, serviceName, authorityInstance.getTitleRef()));
	    		} catch (CSWebApplicationException e) {
	    			response = e.getResponse();
	    			status = response.getStatus();
	    			if (status != Response.Status.CREATED.getStatusCode()) {
	    				throw new CSWebApplicationException(response);
	    			}
	    		}
    		}
    	}
    }
}

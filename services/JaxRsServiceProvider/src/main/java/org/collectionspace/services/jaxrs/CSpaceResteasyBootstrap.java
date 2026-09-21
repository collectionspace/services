package org.collectionspace.services.jaxrs;

import static org.nuxeo.elasticsearch.ElasticSearchConstants.ES_ENABLED_PROPERTY;

import java.io.File;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Set;
import javax.servlet.ServletContext;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Feature;
import javax.ws.rs.core.FeatureContext;
import javax.ws.rs.core.PathSegment;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import io.micrometer.prometheusmetrics.PrometheusMeterRegistry;
import org.apache.commons.io.IOUtils;
import org.collectionspace.authentication.AuthN;
import org.collectionspace.authentication.CSpaceTenant;
import org.collectionspace.services.account.Tenant;
import org.collectionspace.services.account.TenantResource;
import org.collectionspace.services.authorization.AuthZ;
import org.collectionspace.services.batch.BatchResource;
import org.collectionspace.services.client.AbstractCommonListUtils;
import org.collectionspace.services.client.AuthorityClient;
import org.collectionspace.services.client.BatchClient;
import org.collectionspace.services.client.CollectionSpaceClient;
import org.collectionspace.services.client.PayloadOutputPart;
import org.collectionspace.services.client.PoxPayloadOut;
import org.collectionspace.services.client.ReportClient;
import org.collectionspace.services.client.workflow.WorkflowClient;
import org.collectionspace.services.common.CSWebApplicationException;
import org.collectionspace.services.common.ResourceMap;
import org.collectionspace.services.common.ServiceMain;
import org.collectionspace.services.common.api.RefName;
import org.collectionspace.services.common.config.ConfigUtils;
import org.collectionspace.services.common.config.TenantBindingConfigReaderImpl;
import org.collectionspace.services.common.query.UriInfoImpl;
import org.collectionspace.services.common.vocabulary.AuthorityResource;
import org.collectionspace.services.config.service.AuthorityInstanceType;
import org.collectionspace.services.config.service.ServiceBindingType;
import org.collectionspace.services.config.service.ServiceBindingType.AuthorityInstanceList;
import org.collectionspace.services.config.service.Term;
import org.collectionspace.services.config.service.TermList;
import org.collectionspace.services.config.tenant.TenantBindingType;
import org.collectionspace.services.config.types.PropertyItemType;
import org.collectionspace.services.config.types.PropertyType;
import org.collectionspace.services.jaxb.AbstractCommonList;
import org.collectionspace.services.jaxb.AbstractCommonList.ListItem;
import org.collectionspace.services.nuxeo.util.NuxeoUtils;
import org.collectionspace.services.report.ReportResource;
import org.collectionspace.services.systeminfo.MeterRegistryProvider;
import org.jboss.resteasy.core.ResteasyContext;
import org.jboss.resteasy.specimpl.PathSegmentImpl;
import org.jboss.resteasy.spi.Dispatcher;
import org.jboss.resteasy.spi.ResteasyDeployment;
import org.nuxeo.elasticsearch.ElasticSearchComponent;
import org.nuxeo.elasticsearch.api.ElasticSearchService;
import org.nuxeo.runtime.api.Framework;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Note: this isn't really a proper {@link Feature}, but is just being used as a way to do work once the
 * RESTEasyDeployment is available. Previously, this extended ResteasyBootstrap which was causing multiple instances of
 * our JaxRsApplication to be created.
 */
public class CSpaceResteasyBootstrap implements Feature {
	private static final Logger logger = LoggerFactory.getLogger(CSpaceResteasyBootstrap.class);

	private static final String RESET_AUTHORITIES_PROPERTY = "org.collectionspace.services.authorities.reset";
	private static final String RESET_ELASTICSEARCH_INDEX_PROPERTY = "org.collectionspace.services.elasticsearch.reset";
	private static final String RESET_REPORTS_PROPERTY = "org.collectionspace.services.reports.reset";
	private static final String RESET_BATCH_JOBS_PROPERTY = "org.collectionspace.services.batch.reset";
	private static final String QUICK_BOOT_PROPERTY = "org.collectionspace.services.quickboot";
	private static final String REPORT_PROPERTY = "report";
	private static final String BATCH_PROPERTY = "batch";

	@Context
	private ServletContext servletContext;

	@Override
	public boolean configure(FeatureContext featureContext) {
		try {
			logger.info("Starting up the CollectionSpace Services JAX-RS application.");
			ResteasyDeployment deployment = ResteasyContext.getContextData(ResteasyDeployment.class);
			CollectionSpaceJaxRsApplication app = (CollectionSpaceJaxRsApplication) deployment.getApplication();
			Dispatcher disp = deployment.getDispatcher();
			disp.getDefaultContextObjects().put(ResourceMap.class, app.getResourceMap());

			if (MeterRegistryProvider.isEnabled()) {
				PrometheusMeterRegistry registry = MeterRegistryProvider.getInstance().getRegistry();
				servletContext.setAttribute("cspace.metrics.registry", registry);
			}

			// Property can be set in the tomcat/bin/setenv.sh (or setenv.bat) file
			String quickBoot = System.getProperty(QUICK_BOOT_PROPERTY, Boolean.FALSE.toString());

			if (!Boolean.parseBoolean(quickBoot)) {
				// The below properties can be set in the tomcat/bin/setenv.sh (or setenv.bat) file.
				String resetAuthsString = System.getProperty(RESET_AUTHORITIES_PROPERTY, Boolean.FALSE.toString());
				String resetElasticsearchIndexString = System.getProperty(RESET_ELASTICSEARCH_INDEX_PROPERTY, Boolean.FALSE.toString());
				String resetBatchJobsString = System.getProperty(RESET_BATCH_JOBS_PROPERTY, Boolean.TRUE.toString());
				String resetReportsString = System.getProperty(RESET_REPORTS_PROPERTY, Boolean.TRUE.toString());

				initializeAuthorities(app.getResourceMap(), Boolean.parseBoolean(resetAuthsString));

				if (Boolean.parseBoolean(resetElasticsearchIndexString)) {
					resetElasticSearchIndex();
				}

				if (Boolean.parseBoolean(resetReportsString)) {
					resetReports(app);
				}

				if (Boolean.parseBoolean(resetBatchJobsString)) {
					resetBatchJobs(app);
				}
			}

			logger.info("CollectionSpace Services JAX-RS application started.");
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}

		return true;
	}

	public void resetReports(CollectionSpaceJaxRsApplication app) throws Exception {
		logger.info("Resetting reports");

		TenantBindingConfigReaderImpl tenantBindingConfigReader = ServiceMain.getInstance().getTenantBindingConfigReader();
		Hashtable<String, TenantBindingType> tenantBindingsTable = tenantBindingConfigReader.getTenantBindings(false);

		for (TenantBindingType tenantBinding : tenantBindingsTable.values()) {
			ServiceBindingType reportServiceBinding = null;

			for (ServiceBindingType serviceBinding : tenantBinding.getServiceBindings()) {
				if (serviceBinding.getName().toLowerCase().trim().equals(ReportClient.SERVICE_NAME)) {
					reportServiceBinding = serviceBinding;

					break;
				}
			}

			Set<String> reportNames = new HashSet<String>();

			if (reportServiceBinding != null) {
				for (PropertyType property : reportServiceBinding.getProperties()) {
					for (PropertyItemType item : property.getItem()) {
						if (item.getKey().equals(REPORT_PROPERTY)) {
							reportNames.add(item.getValue());
						}
					}
				}
			}

			if (reportNames.size() > 0) {
				CSpaceTenant tenant = new CSpaceTenant(tenantBinding.getId(), tenantBinding.getName());

				resetTenantReports(tenant, reportNames, app);
			}
		}
	}

	private void resetTenantReports(CSpaceTenant tenant, Set<String> reportNames, CollectionSpaceJaxRsApplication app)
		throws Exception {
		logger.info("Resetting reports for tenant {}", tenant.getId());

		AuthZ.get().login(tenant);

		ResourceMap resourceMap = app.getResourceMap();
		ReportResource reportResource = (ReportResource) resourceMap.get(ReportClient.SERVICE_NAME);

		for (String reportName : reportNames) {
			File reportMetadataFile = ReportResource.getReportMetadataFile(reportName);

			if (!reportMetadataFile.exists()) {
				logger.warn(
					"Metadata file not found for report {} at {}",
					reportName, reportMetadataFile.getAbsolutePath());

				continue;
			}

			String payload = new String(Files.readAllBytes(reportMetadataFile.toPath()));
			String reportFilename = reportName + ".jrxml";

			UriInfo uriInfo = new UriInfoImpl(
				new URI(""),
				new URI(""),
				"",
				"pgSz=0&filename=" + URLEncoder.encode(reportFilename, StandardCharsets.UTF_8.toString()),
				Arrays.asList((PathSegment) new PathSegmentImpl("", false))
			);

 			AbstractCommonList list = reportResource.getList(uriInfo);

			if (list.getTotalItems() == 0) {
				logger.info("Adding report " + reportName);

				try {
					reportResource.create(resourceMap, null, payload);
				} catch(Exception e) {
					logger.error(e.getMessage(), e);
				}
			} else {
				for (ListItem item : list.getListItem()) {
					String csid = AbstractCommonListUtils.ListItemGetCSID(item);

					// Update an existing report iff:
					// - it was created autmatically (i.e., by the SPRING_ADMIN user)
					// - it was last updated automatically (i.e., by the SPRING_ADMIN user)
					// - it is not soft-deleted

					PoxPayloadOut reportPayload = reportResource.getResourceFromCsid(null, null, csid);
					PayloadOutputPart corePart = reportPayload.getPart(CollectionSpaceClient.COLLECTIONSPACE_CORE_SCHEMA);

					String createdBy = corePart.asElement().selectSingleNode(CollectionSpaceClient.COLLECTIONSPACE_CORE_CREATED_BY).getText();
					String updatedBy = corePart.asElement().selectSingleNode(CollectionSpaceClient.COLLECTIONSPACE_CORE_UPDATED_BY).getText();
					String workflowState = corePart.asElement().selectSingleNode(CollectionSpaceClient.COLLECTIONSPACE_CORE_WORKFLOWSTATE).getText();

					if (
						createdBy.equals(AuthN.SPRING_ADMIN_USER)
						&& updatedBy.equals(AuthN.SPRING_ADMIN_USER)
						&& !workflowState.equals(WorkflowClient.WORKFLOWSTATE_DELETED)
					) {
						logger.info("Updating report {} with csid {}", reportName, csid);

						try {
							reportResource.update(resourceMap, null, csid, payload);
						} catch (Exception e) {
							logger.error(e.getMessage(), e);
						}
					} else {
						logger.info(
							"Not updating report {} with csid {} - it was not auto-created, or was updated or soft-deleted",
							reportName, csid);
					}
				}
			}
		}
	}

	public void resetBatchJobs(CollectionSpaceJaxRsApplication app) throws Exception {
		logger.info("Resetting batch jobs");

		TenantBindingConfigReaderImpl tenantBindingConfigReader = ServiceMain.getInstance().getTenantBindingConfigReader();
		Hashtable<String, TenantBindingType> tenantBindingsTable = tenantBindingConfigReader.getTenantBindings(false);

		for (TenantBindingType tenantBinding : tenantBindingsTable.values()) {
			ServiceBindingType batchServiceBinding = null;

			for (ServiceBindingType serviceBinding : tenantBinding.getServiceBindings()) {
				if (serviceBinding.getName().toLowerCase().trim().equals(BatchClient.SERVICE_NAME)) {
					batchServiceBinding = serviceBinding;

					break;
				}
			}

			Set<String> batchNames = new HashSet<String>();

			if (batchServiceBinding != null) {
				for (PropertyType property : batchServiceBinding.getProperties()) {
					for (PropertyItemType item : property.getItem()) {
						if (item.getKey().equals(BATCH_PROPERTY)) {
							batchNames.add(item.getValue());
						}
					}
				}
			}

			if (batchNames.size() > 0) {
				CSpaceTenant tenant = new CSpaceTenant(tenantBinding.getId(), tenantBinding.getName());

				resetTenantBatchJobs(tenant, batchNames, app);
			}
		}
	}

	private void resetTenantBatchJobs(CSpaceTenant tenant, Set<String> batchNames, CollectionSpaceJaxRsApplication app)
		throws Exception {
		logger.info("Resetting batch jobs for tenant {}", tenant.getId());

		AuthZ.get().login(tenant);

		ResourceMap resourceMap = app.getResourceMap();
		BatchResource batchResource = (BatchResource) resourceMap.get(BatchClient.SERVICE_NAME);

		for (String batchName : batchNames) {
			InputStream batchMetadataInputStream = BatchResource.getBatchMetadataInputStream(batchName);

			if (batchMetadataInputStream == null) {
				logger.warn(
					"Metadata file not found for batch {}", batchName);

				continue;
			}

			String payload = IOUtils.toString(batchMetadataInputStream, StandardCharsets.UTF_8);

			batchMetadataInputStream.close();

			UriInfo uriInfo = new UriInfoImpl(
				new URI(""),
				new URI(""),
				"",
				"pgSz=0&classname=" + URLEncoder.encode(batchName, StandardCharsets.UTF_8.toString()),
				Arrays.asList((PathSegment) new PathSegmentImpl("", false))
			);

			AbstractCommonList list = batchResource.getList(uriInfo);

			if (list.getTotalItems() == 0) {
				logger.info("Adding batch job " + batchName);

				try {
					batchResource.create(resourceMap, null, payload);
				} catch(Exception e) {
					logger.error(e.getMessage(), e);
				}
			} else {
				for (ListItem item : list.getListItem()) {
					String csid = AbstractCommonListUtils.ListItemGetCSID(item);

					// Update an existing batch job iff:
					// - it was created autmatically (i.e., by the SPRING_ADMIN user)
					// - it was last updated automatically (i.e., by the SPRING_ADMIN user)
					// - it is not soft-deleted

					PoxPayloadOut batchPayload = batchResource.getResourceFromCsid(null, null, csid);
					PayloadOutputPart corePart = batchPayload.getPart(CollectionSpaceClient.COLLECTIONSPACE_CORE_SCHEMA);

					String createdBy = corePart.asElement().selectSingleNode(CollectionSpaceClient.COLLECTIONSPACE_CORE_CREATED_BY).getText();
					String updatedBy = corePart.asElement().selectSingleNode(CollectionSpaceClient.COLLECTIONSPACE_CORE_UPDATED_BY).getText();
					String workflowState = corePart.asElement().selectSingleNode(CollectionSpaceClient.COLLECTIONSPACE_CORE_WORKFLOWSTATE).getText();

					if (
						createdBy.equals(AuthN.SPRING_ADMIN_USER)
						&& updatedBy.equals(AuthN.SPRING_ADMIN_USER)
						&& !workflowState.equals(WorkflowClient.WORKFLOWSTATE_DELETED)
					) {
						logger.info("Updating batch job {} with csid {}", batchName, csid);

						try {
							batchResource.update(resourceMap, null, csid, payload);
						} catch (Exception e) {
							logger.error(e.getMessage(), e);
						}
					} else {
						logger.info(
							"Not updating batch job {} with csid {} - it was not auto-created, or was updated or soft-deleted",
							batchName, csid);
					}
				}
			}
		}
	}

	public void resetElasticSearchIndex() throws Exception {
		boolean isEnabled = Boolean.parseBoolean(Framework.getProperty(ES_ENABLED_PROPERTY, "true"));

		if (!isEnabled) {
			return;
		}

		ElasticSearchComponent es = (ElasticSearchComponent) Framework.getService(ElasticSearchService.class);

		for (String repositoryName : es.getRepositoryNames()) {
			logger.info("Rebuilding Elasticsearch index for repository {}", repositoryName);

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

				if (isElasticsearchIndexed && servicesRepoDomainName != null && !servicesRepoDomainName.trim()
                                                                                                       .isEmpty()) {
					String repositoryName = ConfigUtils.getRepositoryName(tenantBinding, servicesRepoDomainName);
					String docType = NuxeoUtils.getTenantQualifiedDocType(tenantBinding.getId(), serviceBinding.getObject().getName());

					logger.info("Starting Elasticsearch reindexing for docType {} in repository {}", docType, repositoryName);

					es.runReindexingWorker(repositoryName, String.format("SELECT ecm:uuid FROM %s", docType));
				}
			}
		}
	}

    /**
     * Initialize all authorities and vocabularies defined in the service bindings.
     * @param resourceMap
     * @throws Exception
     */
    public void initializeAuthorities(ResourceMap resourceMap, boolean reset) throws Exception {
    	TenantBindingConfigReaderImpl tenantBindingConfigReader = ServiceMain.getInstance().getTenantBindingConfigReader();
    	Hashtable<String, TenantBindingType> tenantBindingsTable = tenantBindingConfigReader.getTenantBindings(false);
    	for (TenantBindingType tenantBindings : tenantBindingsTable.values()) {
			CSpaceTenant tenant = new CSpaceTenant(tenantBindings.getId(), tenantBindings.getName());
			if (shouldInitializeAuthorities(tenant, reset)) {
				logger.info("Initializing vocabularies and authorities of tenant '{}'.", tenant.getId());
	    		for (ServiceBindingType serviceBinding : tenantBindings.getServiceBindings()) {
	    			AuthorityInstanceList element = serviceBinding.getAuthorityInstanceList();
	    			if (element != null && element.getAuthorityInstance() != null) {
	    				List<AuthorityInstanceType> authorityInstanceList = element.getAuthorityInstance();
	    				for (AuthorityInstanceType authorityInstance : authorityInstanceList) {
	    					try {
	    						initializeAuthorityInstance(resourceMap, authorityInstance, serviceBinding, tenant, reset);
	    					} catch (Exception e) {
	    						logger.error("Could not initialize authorities and authority terms: " + e.getMessage());
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
		return !tenantState.isAuthoritiesInitialized() || reset;
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
			logger.debug("Authority of type '{}' with the short ID of '{}' existed already.",
					serviceName, authorityInstance.getTitleRef());
		} else if (status == Response.Status.CREATED.getStatusCode()) {
			logger.debug("Created a new authority of type '{}' with the short ID of '{}'.",
					serviceName, authorityInstance.getTitleRef());
		} else {
			logger.warn("Unknown status '{}' encountered when creating or fetching authority of type '{}' with the short ID of '{}'.",
					status, serviceName, authorityInstance.getTitleRef());
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
			String termShortName = term.getId();
			String termDisplayName = term.getContent().trim();
			boolean exists = authorityResource.hasAuthorityItemWithShortNameOrDisplayName(authoritySpecifier, termShortName, termDisplayName);

			//
			// If the term doesn't exist, create it.
			//
			if (!exists) {
				String xmlPayload = client.createAuthorityItemInstance(termShortName, termDisplayName);
				try {
					authorityResource.createAuthorityItem(resourceMap, null, authoritySpecifier, xmlPayload);
					logger.debug("Tenant:{}:Created a new term '{}:{}' in the authority of type '{}' with the short ID of '{}'.",
							tenant.getName(), termDisplayName, termShortName, serviceName, authorityInstance.getTitleRef());
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

/**
 *  This document is a part of the source code and related artifacts
 *  for CollectionSpace, an open source collections management system
 *  for museums and related institutions:

 *  http://www.collectionspace.org
 *  http://wiki.collectionspace.org

 *  Copyright 2009 University of California at Berkeley

 *  Licensed under the Educational Community License (ECL), Version 2.0.
 *  You may not use this file except in compliance with this License.

 *  You may obtain a copy of the ECL 2.0 License at

 *  https://source.collectionspace.org/collection-space/LICENSE.txt

 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.collectionspace.services.common.context;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.UriInfo;

import org.collectionspace.services.client.IQueryManager;
import org.collectionspace.services.client.workflow.WorkflowClient;
import org.collectionspace.services.common.ServiceMain;
import org.collectionspace.services.common.config.PropertyItemUtils;
import org.collectionspace.services.common.config.ServiceConfigUtils;
import org.collectionspace.services.common.config.TenantBindingConfigReaderImpl;
import org.collectionspace.services.common.document.DocumentHandler;
import org.collectionspace.services.common.document.DocumentFilter;
import org.collectionspace.services.common.document.ValidatorHandler;
import org.collectionspace.services.common.security.SecurityContext;
import org.collectionspace.services.common.security.SecurityContextImpl;
import org.collectionspace.services.common.security.UnauthorizedException;
import org.collectionspace.services.config.ClientType;
import org.collectionspace.services.config.service.ObjectPartType;
import org.collectionspace.services.config.service.ServiceBindingType;
import org.collectionspace.services.config.tenant.RepositoryDomainType;
import org.collectionspace.services.config.tenant.TenantBindingType;
import org.collectionspace.services.config.types.PropertyItemType;
import org.collectionspace.services.config.types.PropertyType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * AbstractServiceContext
 *
 * $LastChangedRevision: $
 * $LastChangedDate: $
 */
/**
 * @author pschmitz
 *
 * @param <IT>
 * @param <OT>
 */
/**
 * @author pschmitz
 *
 * @param <IT>
 * @param <OT>
 */
public abstract class AbstractServiceContextImpl<IT, OT>
        implements ServiceContext<IT, OT> {

    /** The logger. */
    final Logger logger = LoggerFactory.getLogger(AbstractServiceContextImpl.class);
    /** The properties. */
    Map<String, Object> properties = new HashMap<String, Object>();
    /** The object part map. */
    Map<String, ObjectPartType> objectPartMap = new HashMap<String, ObjectPartType>();
    /** The service binding. */
    protected ServiceBindingType serviceBinding;
    /** The tenant binding. */
    private TenantBindingType tenantBinding;
    /** repository domain used by the service */
    private RepositoryDomainType repositoryDomain;
    /** The override document type. */
    private String overrideDocumentType = null;
    /** The val handlers. */
    private List<ValidatorHandler<IT, OT>> valHandlers = null;
    /** The doc handler. */
    private DocumentHandler docHandler = null;
    /** security context */
    private SecurityContext securityContext;
    /** The sessions JAX-RS URI information */
    private UriInfo uriInfo;
    /** The current repository session */
    private Object currentRepositorySession;
    /** A reference count for the current repository session */
    private int currentRepoSesssionRefCount = 0;

    /**
     * Instantiates a new abstract service context impl.
     */
    private AbstractServiceContextImpl() {
        // private constructor for singleton pattern
    }
    // request query params
    /** The query params. */
    private MultivaluedMap<String, String> queryParams;

    /**
     * Instantiates a new abstract service context impl.
     * 
     * @param serviceName the service name
     * 
     * @throws UnauthorizedException the unauthorized exception
     */
    protected AbstractServiceContextImpl(String serviceName) throws UnauthorizedException {

        //establish security context
        securityContext = new SecurityContextImpl();
        //make sure tenant context exists
        checkTenantContext();

        //retrieve service bindings
        TenantBindingConfigReaderImpl tReader =
                ServiceMain.getInstance().getTenantBindingConfigReader();
        String tenantId = securityContext.getCurrentTenantId();
        tenantBinding = tReader.getTenantBinding(tenantId);
        if (tenantBinding == null) {
            String msg = "No tenant binding found for tenantId=" + tenantId
                    + " while processing request for service= " + serviceName;
            logger.error(msg);
            throw new IllegalStateException(msg);
        }
        serviceBinding = tReader.getServiceBinding(tenantId, serviceName);
        if (serviceBinding == null) {
            String msg = "No service binding found while processing request for "
                    + serviceName + " for tenant id=" + getTenantId()
                    + " name=" + getTenantName();
            logger.error(msg);
            throw new IllegalStateException(msg);
        }
        if (logger.isDebugEnabled()) {
            logger.debug("tenantId=" + tenantId
                    + " service binding=" + serviceBinding.getName());
        }
        repositoryDomain = tReader.getRepositoryDomain(tenantId, serviceName);
        if (repositoryDomain != null) {
            if (logger.isDebugEnabled()) {
                logger.debug("tenantId=" + tenantId
                        + " repository doamin=" + repositoryDomain.getName());
            }
        }
    }

    /* (non-Javadoc)
     * @see org.collectionspace.services.common.context.ServiceContext#getCommonPartLabel()
     */
    @Override
    public String getCommonPartLabel() {
        return getCommonPartLabel(getServiceName());
    }

    /* (non-Javadoc)
     * @see org.collectionspace.services.common.context.ServiceContext#getCommonPartLabel(java.lang.String)
     */
    public String getCommonPartLabel(String schemaName) {
        return schemaName.toLowerCase() + PART_LABEL_SEPARATOR + PART_COMMON_LABEL;
    }

    /* (non-Javadoc)
     * @see org.collectionspace.services.common.context.ServiceContext#getPartsMetadata()
     */
    @Override
    public Map<String, ObjectPartType> getPartsMetadata() {
        if (objectPartMap.size() != 0) {
            return objectPartMap;
        }
        ServiceBindingUtils.getPartsMetadata(getServiceBinding(), objectPartMap);
        return objectPartMap;
    }

    /**
     * Gets the properties for part.
     * 
     * @param partLabel the part label
     * 
     * @return the properties for part
     */
    public List<PropertyItemType> getPropertiesForPart(String partLabel) {
        Map<String, ObjectPartType> partMap = getPartsMetadata();
        ObjectPartType part = partMap.get(partLabel);
        if (part == null) {
            throw new RuntimeException("No such part found: " + partLabel);
        }
        List<PropertyType> propNodeList = part.getProperties();
        return propNodeList.isEmpty() ? null : propNodeList.get(0).getItem();
    }

    /**
     * @param partLabel The name of the scehma part to search in
     * @param propName The name of the property (or properties) to find
     * @param qualified Whether the returned values should be qualified with the
     * 		partLabel. This is when the property values are schema field references.
     * @return List of property values for the matched property on the named schema part.
     */
    public List<String> getPropertyValuesForPart(String partLabel, String propName, boolean qualified) {
        List<PropertyItemType> allProps = getPropertiesForPart(partLabel);
        return PropertyItemUtils.getPropertyValuesByName(allProps, propName,
                (qualified ? (partLabel + ":") : null));
    }

    /**
     * @param propName The name of the property (or properties) to find
     * @param qualified Whether the returned values should be qualified with the
     * 		partLabel. This is when the property values are schema field references.
     * @return List of property values for the matched property on any schema part.
     */
    public List<String> getAllPartsPropertyValues(String propName, boolean qualified) {
        return ServiceBindingUtils.getAllPartsPropertyValues(getServiceBinding(), propName, qualified);
    }

    /* (non-Javadoc)
     * @see org.collectionspace.services.common.context.ServiceContext#getServiceBindingPropertyValue(java.lang.String)
     */
    public String getServiceBindingPropertyValue(String propName) {
        return ServiceBindingUtils.getPropertyValue(getServiceBinding(), propName);
    }

    /**
     * Gets the common part properties.
     * 
     * @return the common part properties
     */
    public List<PropertyItemType> getCommonPartProperties() {
        return getPropertiesForPart(getCommonPartLabel());
    }

    /**
     * @param propName The name of the property (or properties) to find
     * @param qualified Whether the returned values should be qualified with the
     * 		partLabel. This is when the property values are schema field references.
     * @return List of property values for the matched property on the common schema part.
     */
    public List<String> getCommonPartPropertyValues(String propName, boolean qualified) {
        return getPropertyValuesForPart(getCommonPartLabel(), propName, qualified);
    }

    /* (non-Javadoc)
     * @see org.collectionspace.services.common.context.ServiceContext#getQualifiedServiceName()
     */
    @Override
    public String getQualifiedServiceName() {
        return TenantBindingConfigReaderImpl.getTenantQualifiedServiceName(getTenantId(), getServiceName());
    }

    /* (non-Javadoc)
     * @see org.collectionspace.services.common.context.ServiceContext#getRepositoryClientName()
     */
    @Override
    public String getRepositoryClientName() {
        if (repositoryDomain == null) {
            return null;
        }
        return repositoryDomain.getRepositoryClient();
    }

    /* (non-Javadoc)
     * @see org.collectionspace.services.common.context.ServiceContext#getRepositoryClientType()
     */
    @Override
    public ClientType getRepositoryClientType() {
        //assumption: there is only one repository client configured
        return ServiceMain.getInstance().getClientType();
    }

    /* (non-Javadoc)
     * @see org.collectionspace.services.common.context.ServiceContext#getRepositoryDomainName()
     */
    @Override
    public String getRepositoryDomainName() {
        if (repositoryDomain == null) {
            return null;
        }
        return repositoryDomain.getName();
    }

    /* (non-Javadoc)
     * @see org.collectionspace.services.common.context.ServiceContext#getRepositoryDomainName()
     */
    @Override
    public String getRepositoryDomainStorageName() {
        if (repositoryDomain == null) {
            return null;
        }
        return repositoryDomain.getStorageName();
    }

    /* (non-Javadoc)
     * @see org.collectionspace.services.common.context.ServiceContext#getRepositoryWorkspaceId()
     */
    @Override
    public String getRepositoryWorkspaceId() {
        return ServiceMain.getInstance().getWorkspaceId(getTenantId(), getServiceName());
    }

    /* (non-Javadoc)
     * @see org.collectionspace.services.common.context.ServiceContext#getRepositoryWorkspaceName()
     */
    @Override
    public String getRepositoryWorkspaceName() {
        //service name is workspace name by convention
        return serviceBinding.getName();
    }

    /* (non-Javadoc)
     * @see org.collectionspace.services.common.context.ServiceContext#getServiceBinding()
     */
    @Override
    public ServiceBindingType getServiceBinding() {
        return serviceBinding;
    }

    /* (non-Javadoc)
     * @see org.collectionspace.services.common.context.ServiceContext#getServiceName()
     */
    @Override
    public String getServiceName() {
        return serviceBinding.getName();
    }

    /* (non-Javadoc)
     * @see org.collectionspace.services.common.context.ServiceContext#getDocumentType()
     */
    @Override
    public String getDocumentType() {
        // If they have not overridden the setting, use the type of the service
        // object.
        return (overrideDocumentType != null) ? overrideDocumentType : serviceBinding.getObject().getName();
    }
    
    @Override
    public String getTenantQualifiedDoctype(String docType) {
        // If they have not overridden the setting, use the type of the service
        // object.
        String result = ServiceBindingUtils.getTenantQualifiedDocType(this.getTenantId(), docType);
        
        return result;
    }
    
    @Override
    public String getTenantQualifiedDoctype() {
        String docType = (overrideDocumentType != null) ? overrideDocumentType : serviceBinding.getObject().getName();
    	return getTenantQualifiedDoctype(docType);
    }

    /* (non-Javadoc)
     * @see org.collectionspace.services.common.context.ServiceContext#setDocumentType(java.lang.String)
     */
    @Override
    public void setDocumentType(String docType) {
        overrideDocumentType = docType;
    }

    /* (non-Javadoc)
     * @see org.collectionspace.services.common.context.ServiceContext#getSecurityContext()
     */
    @Override
    public SecurityContext getSecurityContext() {
        return securityContext;
    }

    /* (non-Javadoc)
     * @see org.collectionspace.services.common.context.ServiceContext#getUserId()
     */
    @Override
    public String getUserId() {
        return securityContext.getUserId();
    }

    /* (non-Javadoc)
     * @see org.collectionspace.services.common.context.ServiceContext#getTenantId()
     */
    @Override
    public String getTenantId() {
        return securityContext.getCurrentTenantId();
    }

    /* (non-Javadoc)
     * @see org.collectionspace.services.common.context.ServiceContext#getTenantName()
     */
    @Override
    public String getTenantName() {
        return securityContext.getCurrentTenantName();
    }

    /* (non-Javadoc)
     * @see org.collectionspace.services.common.context.ServiceContext#getInput()
     */
    @Override
    public abstract IT getInput();

    /* (non-Javadoc)
     * @see org.collectionspace.services.common.context.ServiceContext#setInput(java.lang.Object)
     */
    @Override
    public abstract void setInput(IT input);

    /* (non-Javadoc)
     * @see org.collectionspace.services.common.context.ServiceContext#getOutput()
     */
    @Override
    public abstract OT getOutput();

    /* (non-Javadoc)
     * @see org.collectionspace.services.common.context.ServiceContext#setOutput(java.lang.Object)
     */
    @Override
    public abstract void setOutput(OT output);

    /* (non-Javadoc)
     * @see org.collectionspace.services.common.context.ServiceContext#getProperties()
     */
    @Override
    public Map<String, Object> getProperties() {
        return properties;
    }

    /* (non-Javadoc)
     * @see org.collectionspace.services.common.context.ServiceContext#setProperties(java.util.Map)
     */
    @Override
    public void setProperties(Map<String, Object> props) {
        properties.putAll(props);
    }

    /* (non-Javadoc)
     * @see org.collectionspace.services.common.context.ServiceContext#getProperty(java.lang.String)
     */
    public Object getProperty(String name) {
        return properties.get(name);
    }

    /* (non-Javadoc)
     * @see org.collectionspace.services.common.context.ServiceContext#setProperty(java.lang.String, java.lang.Object)
     */
    public void setProperty(String name, Object o) {
        properties.put(name, o);
    }

    /**
     * checkTenantContext makss sure tenant context exists
     *
     * @return the string
     *
     * @throws UnauthorizedException the unauthorized exception
     */
    private void checkTenantContext() throws UnauthorizedException {

        String tenantId = securityContext.getCurrentTenantId();
        if (tenantId == null) {
            String msg = "Could not find tenant context";
            logger.error(msg);
            throw new UnauthorizedException(msg);
        }
    }

    private static String buildWorkflowWhereClause(MultivaluedMap<String, String> queryParams) {
    	String result = null;
    	
        String includeDeleted = queryParams.getFirst(WorkflowClient.WORKFLOW_QUERY_NONDELETED);
    	if (includeDeleted != null && includeDeleted.equalsIgnoreCase(Boolean.FALSE.toString())) {    	
    		result = "ecm:currentLifeCycleState <> 'deleted'";
    	}
    	
    	return result;
    }
    
    /**
     * Creates the document handler instance.
     * 
     * @return the document handler
     * 
     * @throws Exception the exception
     */
    private DocumentHandler createDocumentHandlerInstance() throws Exception {
        docHandler = ServiceConfigUtils.createDocumentHandlerInstance(tenantBinding, serviceBinding);
        //
        // Create a default document filter
        //
        docHandler.setServiceContext(this);
        DocumentFilter docFilter = docHandler.createDocumentFilter();
        //
        // If the context was created with query parameters,
        // reflect the values of those parameters in the document filter
        // to specify sort ordering, pagination, etc.
        //
        if (this.getQueryParams() != null) {
          docFilter.setSortOrder(this.getQueryParams());
          docFilter.setPagination(this.getQueryParams());
          String workflowWhereClause = buildWorkflowWhereClause(queryParams);
          if (workflowWhereClause != null) {
        	  docFilter.appendWhereClause(workflowWhereClause, IQueryManager.SEARCH_QUALIFIER_AND);			
          }            

        }
        docHandler.setDocumentFilter(docFilter);

        return docHandler;
    }

    /* (non-Javadoc)
     * @see org.collectionspace.services.common.context.ServiceContext#getDocumentHandler()
     */
    @Override
    public DocumentHandler getDocumentHandler() throws Exception {
        DocumentHandler result = docHandler;
        // create a new instance if one does not yet exist
        if (result == null) {
            result = createDocumentHandlerInstance();
        }
        return result;
    }

    /* (non-Javadoc)
     * @see org.collectionspace.services.common.context.ServiceContext#getDocumentHanlder(javax.ws.rs.core.MultivaluedMap)
     */
    @Override
    public DocumentHandler getDocumentHandler(MultivaluedMap<String, String> queryParams) throws Exception {
        DocumentHandler result = getDocumentHandler();
        DocumentFilter documentFilter = result.getDocumentFilter(); //to see results in debugger variables view
        documentFilter.setPagination(queryParams);
        return result;
    }
    
    /*
     * If this element is set in the service binding then use it otherwise
     * assume that asserts are NOT disabled.
     */
    private boolean disableValidationAsserts() {
    	boolean result;
    	Boolean disableAsserts = getServiceBinding().isDisableAsserts();
    	result = (disableAsserts != null) ? disableAsserts : false;
    	return result;
    }
    
    /* (non-Javadoc)
     * @see org.collectionspace.services.common.context.ServiceContext#getValidatorHandlers()
     */
    @Override
    public List<ValidatorHandler<IT, OT>> getValidatorHandlers() throws Exception {
        if (valHandlers != null) {
            return valHandlers;
        }
        List<String> handlerClazzes = getServiceBinding().getValidatorHandler();
        List<ValidatorHandler<IT, OT>> handlers = new ArrayList<ValidatorHandler<IT, OT>>(handlerClazzes.size());
        ClassLoader tccl = Thread.currentThread().getContextClassLoader();
        for (String clazz : handlerClazzes) {
            clazz = clazz.trim();
            Class<?> c = tccl.loadClass(clazz);
            if (disableValidationAsserts() == false) {
            	// enable validation assertions
            	tccl.setClassAssertionStatus(clazz, true);
            }
            if (ValidatorHandler.class.isAssignableFrom(c)) {
                handlers.add((ValidatorHandler) c.newInstance());
            }
        }
        valHandlers = handlers;
        return valHandlers;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        StringBuilder msg = new StringBuilder();
        msg.append("AbstractServiceContext [");
        msg.append("service name=" + serviceBinding.getName() + " ");
        msg.append("service version=" + serviceBinding.getVersion() + " ");
        msg.append("tenant id=" + tenantBinding.getId() + " ");
        msg.append("tenant name=" + tenantBinding.getName() + " ");
        msg.append(tenantBinding.getDisplayName() + " ");
        if (repositoryDomain != null) {
            msg.append("tenant repository domain=" + repositoryDomain.getName());
        }
        for (Map.Entry<String, Object> entry : properties.entrySet()) {
            msg.append("property name=" + entry.getKey() + " value=" + entry.getValue().toString());
        }
        msg.append("]");
        return msg.toString();
    }

    /* (non-Javadoc)
     * @see org.collectionspace.services.common.context.ServiceContext#getQueryParams()
     */
    @Override
    public MultivaluedMap<String, String> getQueryParams() {

         if (queryParams == null){
              if (this.uriInfo != null){
                queryParams = this.uriInfo.getQueryParameters();
            }
         }
         if (queryParams == null){
             queryParams = new org.jboss.resteasy.specimpl.MultivaluedMapImpl<String,String>();
        }
        return this.queryParams;
    }

    @Override
     public MultivaluedMap<String, String> getQueryParamsPtr() {
           return this.queryParams;
    }

    /* (non-Javadoc)
     * @see org.collectionspace.services.common.context.ServiceContext#setQueryParams(javax.ws.rs.core.MultivaluedMap)
     */
    @Override
    public void setQueryParams(MultivaluedMap<String, String> theQueryParams) {
        this.queryParams = theQueryParams;
    }

    @Override
    public void setUriInfo(UriInfo ui){
        this.uriInfo = ui;
    }

	@Override
	public UriInfo getUriInfo() {
		return this.uriInfo;
	}
	
	/*
	 * We expect the 'currentRepositorySession' member to be set only once per instance.  Also, we expect only one open repository session
	 * per HTTP request.  We'll log an error if we see more than one attempt to set a service context's current repo session.
	 * (non-Javadoc)
	 * @see org.collectionspace.services.common.context.ServiceContext#setCurrentRepositorySession(java.lang.Object)
	 */
	@Override
	public void setCurrentRepositorySession(Object repoSession) throws Exception {
		if (repoSession == null) {
			String errMsg = "Setting a service context's repository session to null is not allowed.";
			logger.error(errMsg);
			throw new Exception(errMsg);
		} else if (currentRepositorySession != null && currentRepositorySession != repoSession) {
			String errMsg = "The current service context's repository session was replaced.  This may cause unexpected behavior and/or data loss.";
			logger.error(errMsg);
			throw new Exception(errMsg);
		}
		
		currentRepositorySession = repoSession;
		this.currentRepoSesssionRefCount++;
	}
	
	@Override
	public void clearCurrentRepositorySession() {
		if (this.currentRepoSesssionRefCount > 0) {
			currentRepoSesssionRefCount--;
		}
		
		if (currentRepoSesssionRefCount == 0) {
			this.currentRepositorySession = null;
		}
	}
	
	@Override
	public Object getCurrentRepositorySession() {
		// TODO Auto-generated method stub
		return currentRepositorySession;
	}	
}

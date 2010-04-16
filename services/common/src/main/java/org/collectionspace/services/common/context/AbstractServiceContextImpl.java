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

import java.security.acl.Group;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.security.auth.Subject;
import javax.security.jacc.PolicyContext;
import javax.security.jacc.PolicyContextException;
import javax.ws.rs.core.MultivaluedMap;

import org.collectionspace.authentication.AuthN;
import org.collectionspace.authentication.CSpaceTenant;

import org.collectionspace.services.common.ClientType;
import org.collectionspace.services.common.ServiceMain;
import org.collectionspace.services.common.config.PropertyItemUtils;
import org.collectionspace.services.common.config.TenantBindingConfigReaderImpl;
import org.collectionspace.services.common.document.DocumentHandler;
import org.collectionspace.services.common.document.DocumentFilter;
import org.collectionspace.services.common.document.ValidatorHandler;
import org.collectionspace.services.common.security.UnauthorizedException;
import org.collectionspace.services.common.service.ObjectPartType;
import org.collectionspace.services.common.service.ServiceBindingType;
import org.collectionspace.services.common.service.ServiceObjectType;
import org.collectionspace.services.common.tenant.TenantBindingType;
import org.collectionspace.services.common.types.PropertyItemType;
import org.collectionspace.services.common.types.PropertyType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * AbstractServiceContext
 *
 * $LastChangedRevision: $
 * $LastChangedDate: $
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
    private ServiceBindingType serviceBinding;
    
    /** The tenant binding. */
    private TenantBindingType tenantBinding;
    
    /** The override document type. */
    private String overrideDocumentType = null;
    
    /** The val handlers. */
    private List<ValidatorHandler> valHandlers = null;
    
    /** The doc handler. */
    private DocumentHandler docHandler = null;

    private AbstractServiceContextImpl() {} // private constructor for singleton pattern
    
    // request query params
    private MultivaluedMap<String, String> queryParams;
    
    /**
     * Instantiates a new abstract service context impl.
     * 
     * @param serviceName the service name
     * 
     * @throws UnauthorizedException the unauthorized exception
     */
    protected AbstractServiceContextImpl(String serviceName) throws UnauthorizedException {
        TenantBindingConfigReaderImpl tReader =
                ServiceMain.getInstance().getTenantBindingConfigReader();
        //FIXME retrieveTenantId is not working consistently in non-auth mode
        //TODO: get tenant binding from security context
        String tenantId = retrieveTenantId();
        if (tenantId == null) {
            //for testing purposes
            tenantId = "1"; //hardcoded for movingimages.us
        }
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
    }

    /**
     * getCommonPartLabel get common part label
     * @return
     */
    @Override
    public String getCommonPartLabel() {
        return getCommonPartLabel(getServiceName());
    }

    /**
     * getCommonPartLabel get common part label
     * @return
     */
    public String getCommonPartLabel(String schemaName) {
        return schemaName.toLowerCase() + PART_LABEL_SEPERATOR + PART_COMMON_LABEL;
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
    	if(part==null) {
    		throw new RuntimeException("No such part found: "+partLabel);
    	}
    	List<PropertyType> propNodeList = part.getProperties();
    	return propNodeList.isEmpty()?null:propNodeList.get(0).getItem();
    }

    /**
     * Gets the property values for part.
     * 
     * @param partLabel the part label
     * @param propName the prop name
     * 
     * @return the property values for part
     */
    public List<String> getPropertyValuesForPart(String partLabel, String propName) {
    	List<PropertyItemType> allProps = getPropertiesForPart(partLabel);
    	return PropertyItemUtils.getPropertyValuesByName(allProps, propName);
    }

    /**
     * Gets the all parts property values.
     * 
     * @param propName the prop name
     * 
     * @return the all parts property values
     */
    public List<String> getAllPartsPropertyValues(String propName) {
        return ServiceBindingUtils.getAllPartsPropertyValues(getServiceBinding(), propName);
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
     * Gets the common part property values.
     * 
     * @param propName the prop name
     * 
     * @return the common part property values
     */
    public List<String> getCommonPartPropertyValues(String propName) {
        return getPropertyValuesForPart(getCommonPartLabel(), propName);
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
        if (serviceBinding.getRepositoryClient() == null) {
            return null;
        }
        return serviceBinding.getRepositoryClient().trim();
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
        return tenantBinding.getRepositoryDomain();
    }

    /* (non-Javadoc)
     * @see org.collectionspace.services.common.context.ServiceContext#getRepositoryWorkspaceId()
     */
    @Override
    public String getRepositoryWorkspaceId() {
        TenantBindingConfigReaderImpl tbConfigReader = ServiceMain.getInstance().getTenantBindingConfigReader();
        return tbConfigReader.getWorkspaceId(getTenantId(), getServiceName());
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

    /* (non-Javadoc)
     * @see org.collectionspace.services.common.context.ServiceContext#setDocumentType(java.lang.String)
     */
    @Override
    public void setDocumentType(String docType) {
        overrideDocumentType = docType;
    }

    /* (non-Javadoc)
     * @see org.collectionspace.services.common.context.ServiceContext#getTenantId()
     */
    @Override
    public String getTenantId() {
        return tenantBinding.getId();
    }

    /* (non-Javadoc)
     * @see org.collectionspace.services.common.context.ServiceContext#getTenantName()
     */
    @Override
    public String getTenantName() {
        return tenantBinding.getName();
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
     * Retrieve tenant id.
     * 
     * @return the string
     * 
     * @throws UnauthorizedException the unauthorized exception
     */
    private String retrieveTenantId() throws UnauthorizedException {

        String[] tenantIds = AuthN.get().getTenantIds();
        if (tenantIds.length == 0) {
            String msg = "Could not find tenant context";
            logger.error(msg);
            throw new UnauthorizedException(msg);
        }
        //TODO: if a user is associated with more than one tenants, the tenant
        //id should be matched with the one sent over the wire
        return tenantIds[0];
    }
    
    /**
     * Creates the document handler instance.
     * 
     * @return the document handler
     * 
     * @throws Exception the exception
     */
    private DocumentHandler createDocumentHandlerInstance() throws Exception {
        ClassLoader tccl = Thread.currentThread().getContextClassLoader();
        Class c = tccl.loadClass(getDocumentHandlerClass());
        if (DocumentHandler.class.isAssignableFrom(c)) {
            docHandler = (DocumentHandler) c.newInstance();
        } else {
            throw new IllegalArgumentException("Not of type "
                    + DocumentHandler.class.getCanonicalName());
        }
        //
        // create a default document filter with pagination if the context
        // was created with query params
        //
        docHandler.setServiceContext(this);
        DocumentFilter docFilter = docHandler.createDocumentFilter();
        docFilter.setPagination(this.getQueryParams());
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

    /**
     * Gets the document handler class.
     * 
     * @return the document handler class
     */
    private String getDocumentHandlerClass() {
        if (serviceBinding.getDocumentHandler() == null
                || serviceBinding.getDocumentHandler().isEmpty()) {
            String msg = "Missing documentHandler in service binding for "
                    + getServiceName() + " for tenant id=" + getTenantId()
                    + " name=" + getTenantName();
            logger.error(msg);
            throw new IllegalStateException(msg);
        }
        return serviceBinding.getDocumentHandler().trim();
    }

    /* (non-Javadoc)
     * @see org.collectionspace.services.common.context.ServiceContext#getValidatorHandlers()
     */
    @Override
    public List<ValidatorHandler> getValidatorHandlers() throws Exception {
        if (valHandlers != null) {
            return valHandlers;
        }
        List<String> handlerClazzes = getServiceBinding().getValidatorHandler();
        List<ValidatorHandler> handlers = new ArrayList<ValidatorHandler>(handlerClazzes.size());
        ClassLoader tccl = Thread.currentThread().getContextClassLoader();
        for (String clazz : handlerClazzes) {
            clazz = clazz.trim();
            Class c = tccl.loadClass(clazz);
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
        msg.append("tenant repository domain=" + tenantBinding.getRepositoryDomain());
        for (Map.Entry<String, Object> entry : properties.entrySet()) {
            msg.append("property name=" + entry.getKey() + " value=" + entry.getValue().toString());
        }
        msg.append("]");
        return msg.toString();
    }
    
    @Override
    public MultivaluedMap<String, String> getQueryParams() {
    	return this.queryParams;
    }
    
    @Override
    public void setQueryParams(MultivaluedMap<String, String> queryParams) {
    	this.queryParams = queryParams;
    }    
}

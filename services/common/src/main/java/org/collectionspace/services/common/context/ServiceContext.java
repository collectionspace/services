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

import java.util.List;
import java.util.Map;

import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.UriInfo;

import org.collectionspace.services.common.ResourceMap;
import org.collectionspace.services.common.document.DocumentHandler;
import org.collectionspace.services.common.document.ValidatorHandler;
import org.collectionspace.services.common.security.SecurityContext;
import org.collectionspace.services.config.ClientType;
import org.collectionspace.services.config.service.ObjectPartType;
import org.collectionspace.services.config.service.ServiceBindingType;

/**
 *
 * ServiceContext is used to pass along metadata and runtime context
 * between various components of the service framework while processing
 * a service request.
 */
/**
 * @author pschmitz
 *
 * @param <IT>
 * @param <OT>
 */
public interface ServiceContext<IT, OT> {

    /**
     * The character used to separate the words in a part label
     */
    public static final String PART_LABEL_SEPARATOR = "_";
    /** The Constant PART_COMMON_LABEL. */
    public static final String PART_COMMON_LABEL = "common";
    /** Used to qualify document types **/
	public static final String TENANT_SUFFIX = "Tenant";    

	/* 
	 * Sets the current/open repository session
	 */
	public void setCurrentRepositorySession(Object repoSession) throws Exception;
	
	/*
	 * Decrements the context's repo session ref count and nulls it if the count becomes 0.
	 */
	public void clearCurrentRepositorySession();
	
	/*
	 * If a current repository session exists, returns it.
	 */
	public Object getCurrentRepositorySession();
	
    /**
     * getSecurityContext is contains security info. for the service layer
     */
    public SecurityContext getSecurityContext();

    /**
     * getUserId get authenticated user's userId
     */
    public String getUserId();

    /**
     * getTenantId get id of tenant to which authenticated user is associated with
     * @return tenant id, null if tenant context not found
     */
    public String getTenantId();

    /**
     * getTenantName get tenant name o which authenticated user is associated with
     * @return tenant name such as movingimage.us, null if tenant context not found
     */
    public String getTenantName();

    /**
     * getServiceBinding gets service binding metadata
     * @return service binding metadata
     */
    public ServiceBindingType getServiceBinding();

    /**
     * getServiceName returns the unqualified name of the service
     * @return service name
     */
    public String getServiceName();

    /**
     * getDocumentType returns the name of the (primary) DocumentType for this service
     * The value defaults to the Service Name, unless overridden with setDocumentType();
     * @return service name
     */
    public String getDocumentType();
    
    /**
     * Returns a tenant qualified document type.
     * 
     */
    public String getTenantQualifiedDoctype();
    
    /**
     * Returns a tenant qualified document type.
     * 
     */
    public String getTenantQualifiedDoctype(String docType);

    /**
     * setDocumentType sets the name of the Document Type for this service
     * The value defaults to the Service Name.
     * @return service name
     */
    public void setDocumentType(String docType);

    /**
     * getQualifiedServiceName returns tenant id qualified service name
     * @return tenant qualified service name
     */
    public String getQualifiedServiceName();

    /**
     * getRepositoryDomainName returns repository domain for the tenant
     * @return repository domain for the tenant
     */
    public String getRepositoryDomainName();
    
    /*
     * Get's the name/label used to create the storage container (folder or directory name)
     */
    public String getRepositoryDomainStorageName();    

    /**
     * getRepositoryClientName returns the repository client name as
     * configured for the service
     */
    public String getRepositoryClientName();

    /**
     * getRepositoryClientType returns the type of client configured for the
     * service layer
     * @param ctx service context
     * @return
     */
    public ClientType getRepositoryClientType();

    /**
     * getRepositoryWorkspaceName returns repository workspace for the service for
     * the tenant. Not every service has a corresponding repository workspace.
     * If the service does not have any repository workspace, this method
     * returns null.
     * @return repository workspace
     */
    public String getRepositoryWorkspaceName();

    /**
     * getRepositoryWorksapceId returns workspace id for the service for the
     * tenant. Not every service has a corresponding repository workspace.
     * If the service does not have any repository workspace, this method
     * returns null.
     * @return repository workspace
     */
    public String getRepositoryWorkspaceId();

    /**
     * Get input parts as received over the wire from service consumer
     * @return the input
     */
    public IT getInput();
    
    /**
     * setInput is used to set request input before starting to
     * process input data
     * @param input
     */
    public void setInput(IT input);

    /**
     * Get output parts to send over the wire to service consumer
     * @return the output
     */
    public OT getOutput();

    /**
     * setOutput set output
     * @param output
     */
    public void setOutput(OT output);

    /**
     * @return the map of service names to resource classes.
     */
    public ResourceMap getResourceMap();

    /**
     * @param map the map of service names to resource instances.
     */
    public void setResourceMap(ResourceMap map);

    /**
     * getPartsMetadata returns metadata for object parts used by the service
     * @return
     */
    public Map<String, ObjectPartType> getPartsMetadata();

    /**
     * getCommonPartLabel returns label for common part of a service 
     * @return label
     */
    public String getCommonPartLabel();

    /**
     * getCommonPartLabel returns label for common part of a specified schema.
     * This is useful for sub-resources. 
     * @return label
     */
    public String getCommonPartLabel(String schemaName);

    /**
     * getProperties retruns user-defined properties associated with this context
     * @return
     */
    public Map<String, Object> getProperties();

    /**
     * setProperties sets user-defined properties to this context
     * @param props
     */
    public void setProperties(Map<String, Object> props);

    /**
     * getProperty returns specified user-defined property
     */
    public Object getProperty(String name);

    /**
     * setProperty sets user-defined property with given name
     */
    public void setProperty(String name, Object o);

    /**
     * getServiceBindingPropertyValue returns configured property
     */
    public String getServiceBindingPropertyValue(String propName);

    /**
     * getDocumentHanlder returns document handler configured in the the binding
     * it creates the handler if necessary.
     * @return document handler
     */
    public DocumentHandler getDocumentHandler() throws Exception;

    /**
     * Gets the document hanlder.
     * 
     * @param queryParams the query params
     * 
     * @return the document hanlder
     * 
     * @throws Exception the exception
     */
    public DocumentHandler getDocumentHandler(MultivaluedMap<String, String> queryParams) throws Exception;

    /**
     * getValidatorHandlers returns registered (from binding) validtor handlers
     * for the service. it creates the handlers if necessary.
     * @return validation handlers
     */
    public List<ValidatorHandler<IT, OT>> getValidatorHandlers() throws Exception;

    /**
     * Gets the query params.
     * 
     * @return the query params
     */
    public MultivaluedMap<String, String> getQueryParams();

    public MultivaluedMap<String, String>  getQueryParamsPtr();

    /**
     * Sets the query params.
     * 
     * @param queryParams the query params
     */
    public void setQueryParams(MultivaluedMap<String, String> queryParams);

    public void setUriInfo(UriInfo ui);

    public UriInfo getUriInfo();

}




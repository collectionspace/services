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
package org.collectionspace.services.common;

import java.util.Map;

import org.collectionspace.services.common.config.TenantBindingConfigReaderImpl;
import org.collectionspace.services.common.context.ServiceContext;
import org.collectionspace.services.common.context.ServiceContextFactory;
import org.collectionspace.services.common.document.DocumentHandler;
import org.collectionspace.services.common.repository.RepositoryClient;
import org.collectionspace.services.common.storage.StorageClient;
//import org.jboss.resteasy.core.ResourceMethod;
import org.jboss.resteasy.spi.HttpRequest;

/**
 * The Interface CollectionSpaceResource.
 *
 * @param <IT> the generic type
 * @param <OT> the generic type
 */
public interface CollectionSpaceResource<IT, OT> {

    /**
     * Gets the service name.
     *
     * @return the service name
     */
    public String getServiceName();
    
    /**
     * Gets the common part class.
     *
     * @return the common part class
     */
    public Class<?> getCommonPartClass();

    /**
     * Gets the repository client.
     *
     * @param ctx the ctx
     * @return the repository client
     */
    public RepositoryClient getRepositoryClient(ServiceContext<IT, OT> ctx);

    /**
     * Gets the storage client.
     *
     * @param ctx the ctx
     * @return the storage client
     */
    public StorageClient getStorageClient(ServiceContext<IT, OT> ctx);
    
    /**
     * Creates the document handler.
     *
     * @param ctx the ctx
     * @return the document handler
     * @throws Exception the exception
     */
    public DocumentHandler createDocumentHandler(ServiceContext<IT, OT> ctx) throws Exception;
    
    
    /**
     * Gets the service context factory.
     *
     * @return the service context factory
     */
    public ServiceContextFactory<IT, OT> getServiceContextFactory();

    /*
     * Returns true if this resource allow anonymous access.  It addition to returning 'true', this
     * resources base URL path needs to be declared in the Spring Security config file's 'springSecurityFilterChain' bean.
     * There needs to be a 'filter-chain' element something like the following:
     * See the "applicationContext-security.xml" file for details.
     */
//			<sec:filter-chain pattern="/publicitems/*/*/content"
//                              filters="none"/>
	public boolean allowAnonymousAccess(HttpRequest request, Class<?> resourceClass);
	
    /**
     * Returns a UriRegistry entry: a map of tenant-qualified URI templates
     * for the current resource, for all tenants
     * 
     * @return a map of URI templates for the current resource, for all tenants
     */
    public Map<UriTemplateRegistryKey,StoredValuesUriTemplate> getUriRegistryEntries();
    
    /**
     * Returns a UriRegistry entry: a map of tenant-qualified URI templates
     * for the current resource, for a specified tenants
     * 
     * @return a map of URI templates for the current resource, for a specified tenant
     */
    public Map<UriTemplateRegistryKey,StoredValuesUriTemplate> getUriRegistryEntries(String tenantId,
            String docType, UriTemplateFactory.UriTemplateType type);
    
    /**
     * Returns a URI template of the appropriate type, populated with the
     * current service name as one of its stored values.
     *      * 
     * @param type a URI template type
     * @return a URI template of the appropriate type.
     */
    public StoredValuesUriTemplate getUriTemplate(UriTemplateFactory.UriTemplateType type);

    /**
     * Returns a reader for reading values from tenant bindings configuration
     * 
     * @return a tenant bindings configuration reader
     */
    public TenantBindingConfigReaderImpl getTenantBindingsReader();

    /**
     * Returns the document type of the resource based on the tenant bindings.
     * 
     * @param tenantId
     * @return
     */
	public String getDocType(String tenantId);
}

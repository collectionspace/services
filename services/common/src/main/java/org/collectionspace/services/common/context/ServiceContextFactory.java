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

import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.UriInfo;

import org.collectionspace.services.common.ResourceMap;

/**
 *
 * ServiceContextFactory creates a service context
 *
 */
public interface ServiceContextFactory<IT, OT> {

    /**
     * Creates a new ServiceContext object.
     * 
     * @param serviceName the service name
     * @param input the input
     * 
     * @return the service context
     * 
     * @throws Exception the exception
     */
    public ServiceContext<IT, OT> createServiceContext(String serviceName, IT input) throws Exception;
    
    /**
     * Creates a new ServiceContext object.
     * 
     * @param serviceName the service name
     * 
     * @return the service context< i t, o t>
     * 
     * @throws Exception the exception
     */
    public ServiceContext<IT, OT> createServiceContext(String serviceName) throws Exception;

    /**
     * Creates a new ServiceContext object.
     * 
     * @param serviceName the service name
     * @param input the input
     * @param queryParams the query params
     * 
     * @return the service context
     * 
     * @throws Exception the exception
     */
    public ServiceContext<IT, OT> createServiceContext(
    		String serviceName,
    		IT input,
    		ResourceMap resourceMap,    		
    		UriInfo uriInfo) throws Exception;    
        
    /**
     * Creates a new ServiceContext object.
     * 
     * @param serviceName the service name
     * @param input the input
     * @param queryParams the query params
     * @param documentType the document type
     * @param entityName the entity name
     * 
     * @return the service context
     * 
     * @throws Exception the exception
     */
    public ServiceContext<IT, OT> createServiceContext(
    		String serviceName,
    		IT input, 
    		ResourceMap resourceMap,
    		UriInfo uriInfo,
    		String documentType,
    		String entityName) throws Exception;
}

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

import org.collectionspace.services.client.PoxPayloadIn;
import org.collectionspace.services.client.PoxPayloadOut;
import org.collectionspace.services.common.ResourceMap;

/**
 *
 * MultipartRemoteServiceContextFactory creates a service context for
 * a service processing multipart messages
 *
 */
public class MultipartServiceContextFactory
        implements ServiceContextFactory<PoxPayloadIn, PoxPayloadOut> {

    /** The Constant self. */
    final private static MultipartServiceContextFactory self = new MultipartServiceContextFactory();

    /**
     * Instantiates a new multipart service context factory.
     */
    private MultipartServiceContextFactory() {} // private constructor as part of the singleton pattern

    /**
     * Gets the.
     * 
     * @return the multipart service context factory
     */
    public static MultipartServiceContextFactory get() {
        return self;
    }

    /* (non-Javadoc)
     * @see org.collectionspace.services.common.context.ServiceContextFactory#createServiceContext(java.lang.String)
     */
    @Override
    public ServiceContext<PoxPayloadIn, PoxPayloadOut> createServiceContext(String serviceName) throws Exception {
        MultipartServiceContext ctx = new MultipartServiceContextImpl(serviceName);
        return ctx;
    }

    /* (non-Javadoc)
     * @see org.collectionspace.services.common.context.ServiceContextFactory#createServiceContext(java.lang.String, java.lang.Object)
     */
    @Override
    public ServiceContext<PoxPayloadIn, PoxPayloadOut> createServiceContext(String serviceName,
    		PoxPayloadIn input) throws Exception {
        MultipartServiceContext ctx = new MultipartServiceContextImpl(serviceName, input);
        return ctx;
    }
        
    /* (non-Javadoc)
     * @see org.collectionspace.services.common.context.ServiceContextFactory#createServiceContext(java.lang.String, java.lang.Object, javax.ws.rs.core.MultivaluedMap)
     */
    @Override
    public ServiceContext<PoxPayloadIn, PoxPayloadOut> createServiceContext(
    		String serviceName,
    		PoxPayloadIn input,
    		ResourceMap resourceMap,
    		UriInfo uriInfo)
    			throws Exception {
    	ServiceContext<PoxPayloadIn, PoxPayloadOut> ctx = new MultipartServiceContextImpl(serviceName,
    			input,
    			resourceMap,
    			uriInfo);
    	return ctx;
    }
    
    /* (non-Javadoc)
     * @see org.collectionspace.services.common.context.ServiceContextFactory#createServiceContext(java.lang.String, java.lang.Object, javax.ws.rs.core.MultivaluedMap, java.lang.String, java.lang.String)
     */
    @Override
    public ServiceContext<PoxPayloadIn, PoxPayloadOut> createServiceContext(
    		String serviceName, 
    		PoxPayloadIn input,
    		ResourceMap resourceMap,
    		UriInfo uriInfo,
    		String documentType,
    		String entityName) throws Exception {
    	return this.createServiceContext(serviceName, input, resourceMap, uriInfo);
    }
}

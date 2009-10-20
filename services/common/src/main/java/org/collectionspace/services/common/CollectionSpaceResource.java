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

import org.collectionspace.services.common.context.RemoteServiceContext;
import org.collectionspace.services.common.context.ServiceContext;
import org.collectionspace.services.common.repository.DocumentHandler;
import org.collectionspace.services.common.repository.RepositoryClient;
import org.collectionspace.services.common.repository.RepositoryClientFactory;
import org.jboss.resteasy.plugins.providers.multipart.MultipartInput;

/**
 * CollectionSpaceResource is a resource interface implemented by every
 * entity/service in CollectionSpace
 */
public interface CollectionSpaceResource {

    /**
     * getServiceName returns the name of the service
     */
    public String getServiceName();
    
    /**
     * getRepositoryClientFactory
     * @return
     */
    public RepositoryClientFactory getRepositoryClientFactory();

    /**
     * getRepositoryClient
     * @param ctx service context
     */
    public RepositoryClient getRepositoryClient(ServiceContext ctx);

    /**
     * createServiceContext is a factory method to create a service context
     * a service context is created on every service request call
     * This form uses the serviceName as the default context
     * @param input
     * @return
     */
    public RemoteServiceContext createServiceContext(MultipartInput input) throws Exception;

    /**
     * createServiceContext is a factory method to create a service context
     * a service context is created on every service request call
     * @param input
     * @param serviceName which service/repository context to use
     * @return
     */
    public RemoteServiceContext createServiceContext(MultipartInput input, String serviceName) throws Exception;

    /**
     * createDocumentHandler creates a document handler and populates it with given
     * service context. document handler should never be used
     * across service invocations. it is a stateful object that holds request,
     * response and service context
     * @param ctx
     * @return
     */
    public DocumentHandler createDocumentHandler(RemoteServiceContext ctx) throws Exception ;
}

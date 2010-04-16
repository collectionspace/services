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

import org.collectionspace.services.common.context.ServiceContext;
import org.collectionspace.services.common.context.ServiceContextFactory;
import org.collectionspace.services.common.document.DocumentHandler;
import org.collectionspace.services.common.repository.RepositoryClient;
import org.collectionspace.services.common.storage.StorageClient;

/**
 * CollectionSpaceResource is a resource interface implemented by every
 * entity/service in CollectionSpace
 */
public interface CollectionSpaceResource<IT, OT> {

    /**
     * getServiceName returns the name of the service
     */
    public String getServiceName();
    
    /**
     * Gets the common class.
     * 
     * @return the common class
     */
    public Class getCommonPartClass();

    /**
     * getRepositoryClient
     * @param ctx service context
     */
    public RepositoryClient getRepositoryClient(ServiceContext<IT, OT> ctx);

    /**
     * getStorageClient
     * @param ctx service context
     */
    public StorageClient getStorageClient(ServiceContext<IT, OT> ctx);
    
    /**
     * createDocumentHandler creates a document handler and populates it with given
     * service context. document handler should never be used
     * across service invocations. it is a stateful object that holds request,
     * response and service context
     * @param ctx
     * @return
     */
    public DocumentHandler createDocumentHandler(ServiceContext<IT, OT> ctx) throws Exception;
    
    
    public ServiceContextFactory<IT, OT> getServiceContextFactory();

}

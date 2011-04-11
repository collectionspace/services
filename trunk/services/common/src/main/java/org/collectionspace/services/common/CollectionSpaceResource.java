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

}

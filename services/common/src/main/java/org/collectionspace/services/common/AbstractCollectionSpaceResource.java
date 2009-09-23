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
import org.collectionspace.services.common.context.ServiceContextImpl;
import org.collectionspace.services.common.repository.DocumentHandler;
import org.collectionspace.services.common.repository.RepositoryClient;
import org.collectionspace.services.common.repository.RepositoryClientFactory;
import org.jboss.resteasy.plugins.providers.multipart.MultipartInput;

public abstract class AbstractCollectionSpaceResource
        implements CollectionSpaceResource {

    // Fields for default client factory and client
    private RepositoryClientFactory repositoryClientFactory;
    private RepositoryClient repositoryClient;

    public AbstractCollectionSpaceResource() {
        repositoryClientFactory = RepositoryClientFactory.getInstance();
    }

    @Override
    abstract public String getServiceName();

    @Override
    public RepositoryClientFactory getRepositoryClientFactory() {
        return repositoryClientFactory;
    }

    @Override
    synchronized public RepositoryClient getRepositoryClient(ServiceContext ctx) {
        if(repositoryClient != null){
            return repositoryClient;
        }
        repositoryClient = repositoryClientFactory.getClient(ctx.getRepositoryClientName());
        return repositoryClient;
    }

    @Override
    public ServiceContext createServiceContext(MultipartInput input) throws Exception {
        ServiceContext ctx = new ServiceContextImpl(getServiceName());
        ctx.setInput(input);
        return ctx;
    }

    @Override
    abstract public DocumentHandler createDocumentHandler(ServiceContext ctx) throws Exception ;
}

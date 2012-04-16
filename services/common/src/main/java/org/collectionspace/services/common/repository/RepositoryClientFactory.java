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
package org.collectionspace.services.common.repository;

import java.util.Hashtable;
import org.collectionspace.services.common.ServiceMain;
import org.collectionspace.services.common.config.ServicesConfigReaderImpl;
import org.collectionspace.services.config.RepositoryClientConfigType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * RepositoryClientFactory is a singleton factory that creates required repository
 * clients. Repository clients are singletons.
 *
 * $LastChangedRevision: $
 * $LastChangedDate: $
 */
public class RepositoryClientFactory<IT, OT> {

    private static final RepositoryClientFactory self = new RepositoryClientFactory();
    final Logger logger = LoggerFactory.getLogger(RepositoryClientFactory.class);
    //clients key=client name, value=repository client
    private Hashtable<String, RepositoryClient<IT, OT>> clients = new Hashtable<String, RepositoryClient<IT, OT>>();

    private RepositoryClientFactory() {
        try{
            ServicesConfigReaderImpl scReader = ServiceMain.getInstance().getServicesConfigReader();
            RepositoryClientConfigType repositoryClientConfig = scReader.getConfiguration().getRepositoryClient();
            String clientClassName = repositoryClientConfig.getClientClass();
            String clientName = repositoryClientConfig.getName();
            ClassLoader cloader = Thread.currentThread().getContextClassLoader();

            Class jclazz = cloader.loadClass(clientClassName);
            RepositoryClient<IT, OT> jclient = (RepositoryClient<IT, OT>)jclazz.newInstance();
            clients.put(clientName, jclient);

        } catch(Exception e){
            throw new RuntimeException(e);
        }
    }

    public static RepositoryClientFactory getInstance() {
        return self;
    }

    /**
     * get repository client
     * @param clientName name of the client as found in service binding
     * @return
     */
    public RepositoryClient<IT, OT> getClient(String clientName) {
        return clients.get(clientName);
    }
}

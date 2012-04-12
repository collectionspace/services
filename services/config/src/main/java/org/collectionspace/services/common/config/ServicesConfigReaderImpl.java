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
package org.collectionspace.services.common.config;

import java.io.File;

import org.collectionspace.services.config.ClientType;
import org.collectionspace.services.config.ServiceConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * ServicesConfigReader reads service layer specific configuration
 *
 * $LastChangedRevision: $
 * $LastChangedDate: $
 */
public class ServicesConfigReaderImpl
        extends AbstractConfigReaderImpl<ServiceConfig> {

    final private static String CONFIG_FILE_NAME = "service-config.xml";
    final Logger logger = LoggerFactory.getLogger(ServicesConfigReaderImpl.class);
    private ServiceConfig serviceConfig;
    private ClientType clientType;
    private String clientClassName;

    public ServicesConfigReaderImpl(String serverRootDir) {
        super(serverRootDir);
    }

    @Override
    public String getFileName() {
        return CONFIG_FILE_NAME;
    }

    @Override
    public void read() throws Exception {
        String configFileName = getAbsoluteFileName(CONFIG_FILE_NAME);
        read(configFileName);
    }

    @Override
    public void read(String configFileName) throws Exception {
        if (logger.isDebugEnabled()) {
            logger.debug("read() config file=" + configFileName);
        }
        File configFile = new File(configFileName);
        if (!configFile.exists()) {
            String msg = "Could not find configuration file " + configFile.getAbsolutePath(); //configFileName;
            logger.error(msg);
            throw new RuntimeException(msg);
        }
        serviceConfig = (ServiceConfig) parse(configFile, ServiceConfig.class);
        clientType = serviceConfig.getRepositoryClient().getClientType();
        if (clientType == null) {
            String msg = "Missing <client-type> in <repository-client>";
            logger.error(msg);
            throw new IllegalArgumentException(msg);
        }
        clientClassName = serviceConfig.getRepositoryClient().getClientClass();
        if (clientClassName == null) {
            String msg = "Missing <client-class> in <repository-client>";
            logger.error(msg);
            throw new IllegalArgumentException(msg);
        }
        if (logger.isDebugEnabled()) {
            logger.debug("using client=" + clientType.toString() + " class=" + clientClassName);
        }
    }

    @Override
    public ServiceConfig getConfiguration() {
        return serviceConfig;
    }

    public ClientType getClientType() {
        return clientType;
    }

    public String getClientClass() {
        return clientClassName;
    }
}

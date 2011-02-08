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
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * AbstractConfigReader
 *
 * $LastChangedRevision: $
 * $LastChangedDate: $
 */
public abstract class AbstractConfigReaderImpl<T>
        implements ConfigReader<T> {

    private final Logger logger = LoggerFactory.getLogger(AbstractConfigReaderImpl.class);
    private String serverRootDir;

    AbstractConfigReaderImpl(String serverRootDir) {
        this.serverRootDir = serverRootDir;
    }

    @Override
    abstract public String getFileName();

    @Override
    abstract public void read() throws Exception;

    @Override
    abstract public void read(String configFile) throws Exception;

    @Override
    abstract public T getConfiguration();

    /**
     * parse parses given configuration file from the disk based on given class
     * definition
     * @param configFile
     * @param clazz
     * @return A JAXB object
     * @throws Exception
     */
    protected Object parse(File configFile, Class clazz) throws Exception {
        JAXBContext jc = JAXBContext.newInstance(clazz);
        Unmarshaller um = jc.createUnmarshaller();
        Object readObject = um.unmarshal(configFile);
        if (logger.isDebugEnabled()) {
            logger.debug("read() read file " + configFile.getAbsolutePath());
        }
        return readObject;
    }

    protected String getAbsoluteFileName(String configFileName) {
        return serverRootDir
                + File.separator + CSPACE_DIR_NAME
                + File.separator + CONFIG_DIR_NAME
                + File.separator + configFileName;
    }

    protected String getServerRootDir() {
        return serverRootDir;
    }
}

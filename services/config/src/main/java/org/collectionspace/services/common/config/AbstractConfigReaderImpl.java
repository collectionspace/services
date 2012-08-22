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
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
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
	 * Gets a list of File items in the specified directory.  If 'isDirectory' is true, then this
	 * method will return a list of items that are directories/folders; otherwise, it returns a list
	 * of file/document items.
	 *
	 * @param rootDir the root dir
	 * @param isDirectory the is directory
	 * @return the file children
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	List<File> getFileChildren(File rootDir, boolean getDirectories) throws IOException {
		ArrayList<File> result = new ArrayList<File>();
		File[] children = rootDir.listFiles();
		if (children != null) {
			for (File child : children) {
				if (child.isHidden() == false) {
					if (getDirectories == child.isDirectory()) {
						result.add(child);
					}
				}
			}
		} else {
			String errMessage = "An IO exception and/or error occurred while reading the directory: "
				+ rootDir.getAbsolutePath();
			logger.debug(errMessage);
			throw new IOException(errMessage);
		}
		return result;
	}
	
	/**
	 * Gets a list of files/documents in the specified folder -does not return directories/folders.
	 *
	 * @param rootDir the root dir
	 * @return the files
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	List<File> getFiles(File rootDir) throws IOException {
		return getFileChildren(rootDir, false);
	}
	
	List<File> getDirectories(File rootDir) throws IOException {
		return getFileChildren(rootDir, true);
	}
    
    protected Object parse(File configFile, Class<?> clazz) throws FileNotFoundException, JAXBException {
    	Object result = null;
    	
    	InputStream inputStream = new FileInputStream(configFile);
    	result = parse(inputStream, clazz);
        if (logger.isDebugEnabled()) {
            logger.debug("read() read file " + configFile.getAbsolutePath());
        }
    	
    	return result;
    }
    
    /**
     * parse parses given configuration file from the disk based on given class
     * definition
     * @param configFile
     * @param clazz
     * @return A JAXB object
     * @throws JAXBException 
     * @throws Exception
     */
    protected Object parse(InputStream configFileStream, Class<?> clazz) throws JAXBException {
    	Object result = null;

	        JAXBContext jc = JAXBContext.newInstance(clazz);
	        Unmarshaller um = jc.createUnmarshaller();
	        result = um.unmarshal(configFileStream);

        return result;
    }

    protected String getAbsoluteFileName(String configFileName) {
        return serverRootDir
                + File.separator + CSPACE_DIR_NAME
                + File.separator + CONFIG_DIR_PATH
                + File.separator + configFileName;
    }

    protected String getServerRootDir() {
        return serverRootDir;
    }
    
    protected String getConfigRootDir() {
        return serverRootDir
        + File.separator + CSPACE_DIR_NAME
        + File.separator + CONFIG_DIR_PATH;
    }
}

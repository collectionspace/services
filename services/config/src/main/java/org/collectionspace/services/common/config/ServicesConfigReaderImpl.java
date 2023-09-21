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
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;

import org.apache.commons.io.FileUtils;
import org.collectionspace.services.common.api.JEEServerDeployment;
import org.collectionspace.services.config.ClientType;
import org.collectionspace.services.config.ServiceConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.elca.el4j.services.xmlmerge.Configurer;
import ch.elca.el4j.services.xmlmerge.config.AttributeMergeConfigurer;
import ch.elca.el4j.services.xmlmerge.config.ConfigurableXmlMerge;

/**
 * ServicesConfigReader reads service layer specific configuration
 *
 * $LastChangedRevision: $
 * $LastChangedDate: $
 */
public class ServicesConfigReaderImpl
        extends AbstractConfigReaderImpl<ServiceConfig> {

    private static final String CONFIG_FILE_NAME = "service-config.xml";
    private static final String SECURITY_CONFIG_FILE_NAME = "service-config-security.xml";
    private static final String LOCAL_CONFIG_DIR_NAME = "local";
    private static final String MERGED_FILE_NAME = "service-config.merged.xml";

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
    public void read(boolean useAppGeneratedBindings) throws Exception {
        String localConfigDirName = getAbsoluteFileName(LOCAL_CONFIG_DIR_NAME);
        File localConfigDir = new File(localConfigDirName);
        List<String> localXmlConfigFiles = new ArrayList<>();

        if (localConfigDir.exists()) {
            List<File> localConfigDirFiles = getFiles(localConfigDir);

            Collections.sort(localConfigDirFiles, new Comparator<File>() {
                @Override
                public int compare(File file1, File file2) {
                    return file1.getName().compareTo(file2.getName());
                }
            });

            for (File candidateFile : localConfigDirFiles) {
                if (candidateFile.getName().endsWith(".xml")) {
                    localXmlConfigFiles.add(candidateFile.getAbsolutePath());
                }
            }
        }

        List<String> configFileNames = new ArrayList<>();

        configFileNames.add(getAbsoluteFileName(CONFIG_FILE_NAME));
        configFileNames.add(getAbsoluteFileName(SECURITY_CONFIG_FILE_NAME));
        configFileNames.addAll(localXmlConfigFiles);

        read(configFileNames, useAppGeneratedBindings);
    }

    @Override
    public void read(String configFileName, boolean useAppGeneratedBindings) throws Exception {
        read(Arrays.asList(configFileName), useAppGeneratedBindings);
    }

    @Override
    public void read(List<String> configFileNames, boolean useAppGeneratedBindings) throws Exception {
        List<File> files = new ArrayList<File>();

        for (String configFileName : configFileNames) {
            File configFile = new File(configFileName);

            if (configFile.exists()) {
                logger.info("Using config file " + configFileName);

                files.add(configFile);
            } else {
                logger.warn("Could not find config file " + configFile.getAbsolutePath());
            }
        }

        if (files.size() == 0) {
            throw new RuntimeException("No config files found");
        }

        InputStream mergedConfigStream = merge(files);

        serviceConfig = (ServiceConfig) parse(mergedConfigStream, ServiceConfig.class);
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

    private InputStream merge(List<File> files) throws IOException {
        InputStream result = null;
        List<InputStream> inputStreams = new ArrayList<>();

        for (File file : files) {
            inputStreams.add(new FileInputStream(file));
        }

        try {
            Configurer configurer = new AttributeMergeConfigurer();

            result = new ConfigurableXmlMerge(configurer).merge(inputStreams.toArray(new InputStream[0]));
        } catch (Exception e) {
            logger.error("Could not merge configuration files", e);
        }

        // Save the merge output to a file that is suffixed with ".merged.xml" in the same
        // directory  as the first file.

        if (result != null) {
            File outputDir = files.get(0).getParentFile();
            String mergedFileName = outputDir.getAbsolutePath() + File.separator + MERGED_FILE_NAME;
            File mergedOutFile = new File(mergedFileName);

            try {
                FileUtils.copyInputStreamToFile(result, mergedOutFile);
            } catch (IOException e) {
                logger.warn("Could not create a copy of the merged configuration at: " + mergedFileName, e);
            }

            result.reset();
        }

        return result;
	}
}

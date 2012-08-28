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
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.collectionspace.services.common.api.Tools;
import org.collectionspace.services.config.service.ServiceBindingType;
import org.collectionspace.services.config.service.ServiceObjectType;
import org.collectionspace.services.config.tenant.RepositoryDomainType;
import org.collectionspace.services.config.tenant.TenantBindingConfig;
import org.collectionspace.services.config.tenant.TenantBindingType;
import org.collectionspace.services.config.types.PropertyItemType;

import ch.elca.el4j.services.xmlmerge.Configurer;
import ch.elca.el4j.services.xmlmerge.config.AttributeMergeConfigurer;
import ch.elca.el4j.services.xmlmerge.config.ConfigurableXmlMerge;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * ServicesConfigReader reads service layer specific configuration
 *
 * $LastChangedRevision: $
 * $LastChangedDate: $
 */
public class TenantBindingConfigReaderImpl
        extends AbstractConfigReaderImpl<List<TenantBindingType>> {
    final private static String TENANT_BINDINGS_ERROR = "Tenant bindings error: ";
    final private static String TENANT_BINDINGS_ROOTDIRNAME = "tenants";
    final private static String TENANT_BINDINGS_FILENAME_PREFIX = "tenant-bindings";
    final private static String TENANT_BINDINGS_PROTOTYPE_FILENAME = TENANT_BINDINGS_FILENAME_PREFIX + "-proto.xml";
    final private static String TENANT_BINDINGS_DELTA_FILENAME = TENANT_BINDINGS_FILENAME_PREFIX + ".delta.xml";
    final private static String MERGED_SUFFIX = ".merged.xml";
    
    final Logger logger = LoggerFactory.getLogger(TenantBindingConfigReaderImpl.class);
    private List<TenantBindingType> tenantBindingTypeList;
    //tenant id, tenant binding
    private Hashtable<String, TenantBindingType> tenantBindings =
            new Hashtable<String, TenantBindingType>();
    //repository domains
    private Hashtable<String, RepositoryDomainType> domains =
            new Hashtable<String, RepositoryDomainType>();
    //tenant-qualified servicename, service binding
    private Hashtable<String, ServiceBindingType> serviceBindings =
            new Hashtable<String, ServiceBindingType>();

    //tenant-qualified service object name to service name, service binding
    private Hashtable<String, ServiceBindingType> docTypes =
            new Hashtable<String, ServiceBindingType>();


    public TenantBindingConfigReaderImpl(String serverRootDir) {
        super(serverRootDir);
    }

    @Override
    public String getFileName() {
        return TENANT_BINDINGS_DELTA_FILENAME;
    }
    
	protected File getTenantsRootDir() {
		File result = null;
		String errMessage = null;
		try {
			String tenantsRootPath = getConfigRootDir() + File.separator + TENANT_BINDINGS_ROOTDIRNAME;
			File tenantsRootDir = new File(tenantsRootPath);
			if (tenantsRootDir.exists() == true) {
				result = tenantsRootDir;
				if (logger.isDebugEnabled() == true) {
					logger.debug("The home directory for all tenants is at: " + result.getCanonicalPath());
				}
			} else {
				errMessage = "The home directory for all tenants is missing or inaccesible: ";
				try {
					errMessage = errMessage + tenantsRootDir.getCanonicalPath();
				} catch (IOException ioException) {
					errMessage = errMessage + tenantsRootDir.getAbsolutePath();
				}
			}
		} catch (IOException e) {
			// Log this exception, but continue anyway.  Caller should handle the null result gracefully.
			logger.equals(e);
		}
		
		if (errMessage != null) {
			logger.error(errMessage);
		}
		
		return result;
	}
	
    /*
     * Take the directory of the prototype bindings and the directory of the delta bindings.  Merge the two and create (replace) a file
     * named "tenant-bindings.xml"
     */

	private InputStream merge(File srcFile, File deltaFile) throws IOException {
		InputStream result = null;
		try {						
			FileInputStream srcStream = new FileInputStream(srcFile);
			FileInputStream deltaStream = new FileInputStream(deltaFile);
			InputStream[] inputStreamArray = {srcStream, deltaStream};			
			
			Configurer configurer = new AttributeMergeConfigurer();
			result = new ConfigurableXmlMerge(configurer).merge(inputStreamArray);
		} catch (Exception e) {
			logger.error("Could not merge tenant configuration delta file: " +
					deltaFile.getCanonicalPath(), e);
		}
		//
		// Try to save the merge output to a file that is suffixed with ".merged.xml" in the same directory
		// as the delta file.
		//
		if (result != null) {
			File outputDir = deltaFile.getParentFile();
			String mergedFileName = outputDir.getAbsolutePath() + File.separator +
				TenantBindingConfigReaderImpl.TENANT_BINDINGS_FILENAME_PREFIX + MERGED_SUFFIX;			
			File mergedOutFile = new File(mergedFileName);
			try {
				FileUtils.copyInputStreamToFile(result, mergedOutFile);
			} catch (IOException e) {
				logger.warn("Could not create a copy of the merged tenant configuration at: " +
						mergedFileName, e);
			}
			result.reset(); //reset the stream even if the file create failed.
		}

		return result;
	}

    @Override
    public void read() throws Exception {
    	String tenantsRootPath = getTenantsRootDir().getAbsolutePath();
        read(tenantsRootPath);
    }

    @Override
    public void read(String tenantRootDirPath) throws Exception {
        File tenantsRootDir = new File(tenantRootDirPath);
        if (tenantsRootDir.exists() == false) {
        	throw new Exception("Cound not find tenant bindings root directory: " +
        			tenantRootDirPath);
        }
        File protoBindingsFile = new File(tenantRootDirPath + File.separator +
        		TENANT_BINDINGS_PROTOTYPE_FILENAME);
        
        List<File> tenantDirs = getDirectories(tenantsRootDir);
        tenantBindingTypeList = readTenantConfigs(protoBindingsFile, tenantDirs);
        
        for (TenantBindingType tenantBinding : tenantBindingTypeList) {
        	if(tenantBindings.get(tenantBinding.getId()) != null) {
        		TenantBindingType tenantBindingOld = tenantBindings.get(tenantBinding.getId());
                logger.error("Ignoring duplicate binding definition for tenant id=" 
                		+ tenantBinding.getId()
                        + " existing name=" + tenantBindingOld.getName()
                        + " conflicting (ignored) name=" + tenantBinding.getName());
                continue;
        	}
            tenantBindings.put(tenantBinding.getId(), tenantBinding);
            readDomains(tenantBinding);
            readServiceBindings(tenantBinding);
            if (logger.isInfoEnabled()) {
                logger.info("Finished reading tenant bindings for tenant id=" + tenantBinding.getId()
                        + " name=" + tenantBinding.getName());
            }
        }
    }
    
    /*
     * Take the directory of the prototype bindings and the directory of the delta bindings.  Merge the two and create (replace) a file
     * named "tenant-bindings.xml"
     *
	private static String merge(String original, String patch) {
		InputStream result = null;
		try {
			Configurer configurer = new AttributeMergeConfigurer();
			
			
			FileInputStream ins1 = new FileInputStream(".\\src\\main\\resources\\File1.xml");
			FileInputStream ins2 = new FileInputStream(".\\src\\main\\resources\\File2.xml");
			InputStream[] inputStreamArray = {ins1, ins2};			
			
			result = new ConfigurableXmlMerge(configurer).merge(inputStreamArray);
//			result = new ConfigurableXmlMerge(configurer).merge(new String[] {original, patch});
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		File mergedOutFile = new File(".\\target\\merged.xml");
		try {
			FileUtils.copyInputStreamToFile(result, mergedOutFile);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return null;
	}
	*/
    
	/**
     * Merge and read the prototype bindsings with each tenant specific bindings delta to create the final
     * tenant bindings.
     *
     * @param protoBindingsFile - The prototypical bindings file.
     * @param tenantDirList - The list of tenant directories containing tenant specific bindings
     * @return A list of tenant bindings.
     * @throws IOException Signals that an I/O exception has occurred.
     */
    List<TenantBindingType> readTenantConfigs(File protoBindingsFile, List<File> tenantDirList) throws IOException {
		List<TenantBindingType> result = new ArrayList<TenantBindingType>();		
		//
		// Iterate through a list of directories.
		//
		for (File tenantDir : tenantDirList) {
			boolean found = false;
			String errMessage = null;
			File configFile = new File(tenantDir.getAbsoluteFile() + File.separator + getFileName());
			if (configFile.exists() == true) {
		        InputStream tenantBindingsStream = this.merge(protoBindingsFile, configFile);
		        TenantBindingConfig tenantBindingConfig = null;
		        try {
					tenantBindingConfig = (TenantBindingConfig) parse(tenantBindingsStream,
							TenantBindingConfig.class);
		        } catch (Exception e) {
		        	logger.error("Could not parse the merged tenant bindings.", e);
		        }
				if (tenantBindingConfig != null) {
					TenantBindingType binding = tenantBindingConfig.getTenantBinding();
					if (binding != null) {
						result.add(binding);
						found = true;
						if (logger.isInfoEnabled() == true) {
							logger.info("Parsed tenant configureation for: " + binding.getDisplayName());
						}
					} else {
						errMessage = "Cound not parse the tentant bindings in: ";
					}
				} else {
					errMessage = "Could not parse the tenant bindings file: ";				
				}
			} else {
				errMessage = "Cound not find a tenant configuration file: ";
			}
			if (found == false) {
				if (logger.isErrorEnabled() == true) {
					errMessage = errMessage != null ? errMessage : TENANT_BINDINGS_ERROR;
					logger.error(errMessage + configFile.getAbsolutePath());
				}
			}
		} // else-for
		
		return result;
	}
    
    private void readDomains(TenantBindingType tenantBinding) throws Exception {
        for (RepositoryDomainType domain : tenantBinding.getRepositoryDomain()) {
        	String key = getTenantQualifiedIdentifier(tenantBinding.getId(),
        			domain.getName());
            domains.put(key, domain);
        }
    }

    private void readServiceBindings(TenantBindingType tenantBinding) throws Exception {
        for (ServiceBindingType serviceBinding : tenantBinding.getServiceBindings()) {
            String key = getTenantQualifiedServiceName(tenantBinding.getId(),
                    serviceBinding.getName());
            serviceBindings.put(key, serviceBinding);

            if (serviceBinding!=null){
                ServiceObjectType objectType = serviceBinding.getObject();
                if (objectType!=null){
                    String docType = objectType.getName();
                    String docTypeKey = getTenantQualifiedIdentifier(tenantBinding.getId(), docType);
                    docTypes.put(docTypeKey, serviceBinding);
                }
            }
            if (logger.isTraceEnabled()) {
                logger.trace("readServiceBindings() added service "
                        + " name=" + key
                        + " workspace=" + serviceBinding.getName());
            }
        }
    }

    @Override
    public List<TenantBindingType> getConfiguration() {
        return tenantBindingTypeList;
    }

    /**
     * getTenantBindings returns all the tenant bindings read from configuration
     * @return
     */
    public Hashtable<String, TenantBindingType> getTenantBindings() {
        return tenantBindings;
    }

    /**
     * getTenantBinding gets tenant binding for given tenant
     * @param tenantId
     * @return
     */
    public TenantBindingType getTenantBinding(
            String tenantId) {
        return tenantBindings.get(tenantId);
    }

    /**
     * getRepositoryDomain gets repository domain configuration for the given name
     * @param domainName
     * @return
     */
    public RepositoryDomainType getRepositoryDomain(String domainName) {
        return domains.get(domainName.trim());
    }

    /**
     * getRepositoryDomain gets repository domain configuration for the given service
     * and given tenant id
     * @param tenantId
     * @param serviceName
     * @return
     */
    public RepositoryDomainType getRepositoryDomain(String tenantId, String serviceName) {
        ServiceBindingType serviceBinding = getServiceBinding(tenantId, serviceName);
        if (serviceBinding == null) {
            throw new IllegalArgumentException("no service binding found for " + serviceName
                    + " of tenant with id=" + tenantId);
        }
        String repoDomain = serviceBinding.getRepositoryDomain(); 
        if (repoDomain == null) {
        	/* This is excessive - every call to a JPA based service dumps this msg.
            if (logger.isDebugEnabled()) {
                logger.debug("No repository domain configured for " + serviceName
                        + " of tenant with id=" + tenantId);
            }
            */
            return null;
        }
        String key = this.getTenantQualifiedIdentifier(tenantId, repoDomain.trim());
        return domains.get(key);
    }

    /**
     * getServiceBinding gets service binding for given tenant for a given service
     * @param tenantId
     * @param serviceName
     * @return
     */
    public ServiceBindingType getServiceBinding(
            String tenantId, String serviceName) {
        String key = getTenantQualifiedServiceName(tenantId, serviceName);
        return serviceBindings.get(key);
    }

    /**
     * getServiceBinding gets service binding for given tenant for a given service
     * @param tenantId
     * @param docType
     * @return
     */
    public ServiceBindingType getServiceBindingForDocType (String tenantId, String docType) {
        String key = getTenantQualifiedIdentifier(tenantId, docType);
        return docTypes.get(key);
    }

    /**
     * getServiceBinding gets service binding for given tenant for a given service
     * @param tenantId
     * @param serviceName
     * @return
     */
    public List<ServiceBindingType> getServiceBindingsByType(
            String tenantId, String serviceType) {
    	List<String> serviceTypes = new ArrayList<String>(1);
    	serviceTypes.add(serviceType);
    	return getServiceBindingsByType(tenantId, serviceTypes);
    }
    /**
     * getServiceBinding gets service binding for given tenant for a given service
     * @param tenantId
     * @param serviceName
     * @return
     */
    public List<ServiceBindingType> getServiceBindingsByType(
            String tenantId, List<String> serviceTypes) {
        ArrayList<ServiceBindingType> list = null;
        TenantBindingType tenant = tenantBindings.get(tenantId);
        if (tenant != null) {
            for (ServiceBindingType sb : tenant.getServiceBindings()) {
                if (serviceTypes.contains(sb.getType())) {
                    if (list == null) {
                        list = new ArrayList<ServiceBindingType>();
                    }
                    list.add(sb);
                }
            }
        }
        return list;
    }

    /**
     * @param tenantId
     * @param serviceName
     * @return the properly qualified service name
     */
    public static String getTenantQualifiedServiceName(
            String tenantId, String serviceName) {
//        return tenantId + "." + serviceName.toLowerCase();
        return getTenantQualifiedIdentifier(tenantId, serviceName.toLowerCase());
    }

    public static String getTenantQualifiedIdentifier(String tenantId, String identifier) {
        return tenantId + "." + identifier;
    }
    /**
     * Sets properties in the passed list on the local properties for this TenantBinding.
     * Note: will only set properties not already set on the TenantBinding.
     * 
     * @param propList
     * @param propagateToServices If true, recurses to set set properties 
     * 			on the associated services.
     */
    public void setDefaultPropertiesOnTenants(List<PropertyItemType> propList,
            boolean propagateToServices) {
        // For each tenant, set properties in list that are not already set
        if (propList == null || propList.isEmpty()) {
            return;
        }
        for (TenantBindingType tenant : tenantBindings.values()) {
            for (PropertyItemType prop : propList) {
                TenantBindingUtils.setPropertyValue(tenant,
                        prop, TenantBindingUtils.SET_PROP_IF_MISSING);
            }
            if (propagateToServices) {
                TenantBindingUtils.propagatePropertiesToServices(tenant,
                        TenantBindingUtils.SET_PROP_IF_MISSING);
            }
        }
    }

    public String getResourcesDir(){
        return getConfigRootDir() + File.separator + RESOURCES_DIR_NAME;
    }
    
    
    /**
     * Returns a list of tenant identifiers (tenant IDs).
     * 
     * @return a list of tenant IDs
     */
    public List<String> getTenantIds() {
        List<String> tenantIds = new ArrayList<String>();
        String tenantId;
        Hashtable<String, TenantBindingType> tenantBindings = getTenantBindings();
        if (tenantBindings != null && !tenantBindings.isEmpty()) {
            Enumeration keys = tenantBindings.keys();
            while (keys.hasMoreElements()) {
                tenantId = (String) keys.nextElement();
                if (Tools.notBlank(tenantId)) {
                    tenantIds.add(tenantId);
                }
            }
        }
        return tenantIds;
    }
}

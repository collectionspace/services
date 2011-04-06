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
import java.io.IOException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import org.collectionspace.services.common.service.ServiceBindingType;
import org.collectionspace.services.common.service.ServiceObjectType;
import org.collectionspace.services.common.tenant.RepositoryDomainType;
import org.collectionspace.services.common.tenant.TenantBindingType;
import org.collectionspace.services.common.tenant.TenantBindingConfig;
import org.collectionspace.services.common.types.PropertyItemType;
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
    final private static String TENANT_BINDINGS_FILENAME = "tenant-bindings.xml";
    final private static String TENANT_BINDINGS_ROOTDIRNAME = "tenants";
    
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
        return TENANT_BINDINGS_FILENAME;
    }
    
	protected File getTenantsRootDir() {
		File result = null;
		String tenantsRootPath = getConfigRootDir() + File.separator + TENANT_BINDINGS_ROOTDIRNAME;
		File tenantsRootDir = new File(tenantsRootPath);
		if (tenantsRootDir.exists() == true) {
			result = tenantsRootDir;
			logger.debug("Tenants home directory is: " + tenantsRootDir.getAbsolutePath()); //FIXME: REM - Add proper if (logger.isDebug() == true) check
		} else {
			logger.error("Tenants home directory is missing.  Can't find: " + tenantsRootDir.getAbsolutePath()); //FIXME: REM - Add proper if (logger.isError() == true) check
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
        List<File> tenantDirs = getDirectories(tenantsRootDir);
        tenantBindingTypeList = readTenantConfigs(tenantDirs);
        
        for (TenantBindingType tenantBinding : tenantBindingTypeList) {
            tenantBindings.put(tenantBinding.getId(), tenantBinding);
            readDomains(tenantBinding);
            readServiceBindings(tenantBinding);
            if (logger.isDebugEnabled()) {
                logger.debug("read() added tenant id=" + tenantBinding.getId()
                        + " name=" + tenantBinding.getName());
            }
        }
    }

	List<TenantBindingType> readTenantConfigs(List<File> tenantDirList) throws IOException {
		List<TenantBindingType> result = new ArrayList<TenantBindingType>();		
		//
		// Iterate through a list of directories.
		//
		for (File tenantDir : tenantDirList) {
			boolean found = false;
			String errMessage = null;
			File configFile = new File(tenantDir.getAbsoluteFile() + File.separator + TENANT_BINDINGS_FILENAME);
			if (configFile.exists() == true) {
				TenantBindingConfig tenantBindingConfig = (TenantBindingConfig) parse(
						configFile, TenantBindingConfig.class);
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
            domains.put(domain.getName(), domain);
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
            if (logger.isDebugEnabled()) {
                logger.debug("readServiceBindings() added service "
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
        return domains.get(repoDomain.trim());
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
        ArrayList<ServiceBindingType> list = null;
        TenantBindingType tenant = tenantBindings.get(tenantId);
        if (tenant != null) {
            for (ServiceBindingType sb : tenant.getServiceBindings()) {
                if (serviceType.equals(sb.getType())) {
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
        return tenantId + "." + serviceName.toLowerCase();
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
        return getConfigRootDir() + File.separator + "resources";
    }
}

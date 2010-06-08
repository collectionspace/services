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
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import org.collectionspace.services.common.service.ServiceBindingType;
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
        extends AbstractConfigReaderImpl<TenantBindingConfig> {

    final private static String CONFIG_FILE_NAME = "tenant-bindings.xml";
    final Logger logger = LoggerFactory.getLogger(TenantBindingConfigReaderImpl.class);
    private TenantBindingConfig tenantBindingConfig;
    //tenant id, tenant binding
    private Hashtable<String, TenantBindingType> tenantBindings =
            new Hashtable<String, TenantBindingType>();
    //repository domains
    private Hashtable<String, RepositoryDomainType> domains =
            new Hashtable<String, RepositoryDomainType>();
    //tenant-qualified servicename, service binding
    private Hashtable<String, ServiceBindingType> serviceBindings =
            new Hashtable<String, ServiceBindingType>();

    public TenantBindingConfigReaderImpl(String serverRootDir) {
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
            String msg = "Could not find configuration file " + configFileName;
            logger.error(msg);
            throw new RuntimeException(msg);
        }
        tenantBindingConfig = (TenantBindingConfig) parse(configFile, TenantBindingConfig.class);
        for (TenantBindingType tenantBinding : tenantBindingConfig.getTenantBinding()) {
            tenantBindings.put(tenantBinding.getId(), tenantBinding);
            readDomains(tenantBinding);
            readServiceBindings(tenantBinding);
            if (logger.isDebugEnabled()) {
                logger.debug("read() added tenant id=" + tenantBinding.getId()
                        + " name=" + tenantBinding.getName());
            }
        }
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
            if (logger.isDebugEnabled()) {
                logger.debug("readServiceBindings() added service "
                        + " name=" + key
                        + " workspace=" + serviceBinding.getName());
            }
        }
    }

    @Override
    public TenantBindingConfig getConfiguration() {
        return tenantBindingConfig;
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
        if (serviceBinding.getRepositoryDomain() == null) {
            if (logger.isDebugEnabled()) {
                logger.debug("No repository domain configured for " + serviceName
                        + " of tenant with id=" + tenantId);
            }
            return null;
        }
        return domains.get(serviceBinding.getRepositoryDomain().trim());
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
}

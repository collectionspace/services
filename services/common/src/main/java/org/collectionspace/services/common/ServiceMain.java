/**
 * Copyright 2009 University of California at Berkeley
 */
package org.collectionspace.services.common;

import java.util.Hashtable;
import java.util.List;

import org.collectionspace.services.common.config.ServicesConfigReaderImpl;
import org.collectionspace.services.common.config.TenantBindingConfigReaderImpl;
import org.collectionspace.services.common.tenant.TenantBindingType;
import org.collectionspace.services.common.types.PropertyItemType;
import org.collectionspace.services.common.types.PropertyType;
import org.collectionspace.services.nuxeo.client.java.NuxeoConnector;
import org.collectionspace.services.nuxeo.client.java.TenantRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Main class for Services layer. It reads configuration and performs service
 * level initialization. It is a singleton.
 * @author 
 */
public class ServiceMain {

    /**
     * volatile is used here to assume about ordering (post JDK 1.5)
     */
    private static volatile ServiceMain instance = null;
    final Logger logger = LoggerFactory.getLogger(ServiceMain.class);
    private NuxeoConnector nuxeoConnector;
    private String serverRootDir = null;
    private ServicesConfigReaderImpl servicesConfigReader;
    private TenantBindingConfigReaderImpl tenantBindingConfigReader;

    private ServiceMain() {
    }

    /**
     * getInstance returns the ServiceMain singleton instance after proper
     * initialization in a thread-safe manner
     * @return
     */
    public static ServiceMain getInstance() {
        if (instance == null) {
            synchronized (ServiceMain.class) {
                if (instance == null) {
                    ServiceMain temp = new ServiceMain();
                    try {
                        temp.initialize();
                    } catch (Exception e) {
                        instance = null;
                        if (e instanceof RuntimeException) {
                            throw (RuntimeException) e;
                        } else {
                            throw new RuntimeException(e);
                        }
                    }
                    instance = temp;
                }
            }
        }
        return instance;
    }

    private void initialize() throws Exception {
        setServerRootDir();
        readConfig();
        propagateConfiguredProperties();
        if (getClientType().equals(ClientType.JAVA)) {
            nuxeoConnector = NuxeoConnector.getInstance();
            nuxeoConnector.initialize(
                    getServicesConfigReader().getConfiguration().getRepositoryClient());
        }
    }

    /**
     * release releases all resources occupied by service layer infrastructure
     * but not necessarily those occupied by individual services
     */
    public void release() {
        try {
            if (nuxeoConnector != null) {
                nuxeoConnector.release();
            }
            instance = null;
        } catch (Exception e) {
            e.printStackTrace();
            //gobble it
        }
    }

    private void readConfig() throws Exception {
        //read service config
        servicesConfigReader = new ServicesConfigReaderImpl(getServerRootDir());
        getServicesConfigReader().read();

        tenantBindingConfigReader = new TenantBindingConfigReaderImpl(getServerRootDir());
        getTenantBindingConfigReader().read();
    }

    private void propagateConfiguredProperties() {
        List<PropertyType> repoPropListHolder =
                servicesConfigReader.getConfiguration().getRepositoryClient().getProperties();
        if (repoPropListHolder != null && !repoPropListHolder.isEmpty()) {
            List<PropertyItemType> propList = repoPropListHolder.get(0).getItem();
            if (propList != null && !propList.isEmpty()) {
                tenantBindingConfigReader.setDefaultPropertiesOnTenants(propList, true);
            }
        }
    }

    void retrieveAllWorkspaceIds() throws Exception {
        //all configs are read, connector is initialized, retrieve workspaceids
        Hashtable<String, TenantBindingType> tenantBindings =
                getTenantBindingConfigReader().getTenantBindings();
        TenantRepository.get().retrieveAllWorkspaceIds(tenantBindings);
    }

    /**
     * getWorkspaceId returns workspace id for given tenant and service name
     * @param tenantId
     * @param serviceName
     * @return
     */
    public String getWorkspaceId(String tenantId, String serviceName) {
        return TenantRepository.get().getWorkspaceId(tenantId, serviceName);
    }

    /**
     * @return the nuxeoConnector
     */
    public NuxeoConnector getNuxeoConnector() {
        return nuxeoConnector;
    }
    
    /**
     * @return the serverRootDir
     */
    public String getServerRootDir() {
        return serverRootDir;
    }

    private void setServerRootDir() {
        serverRootDir = System.getProperty("jboss.server.home.dir");
        if (serverRootDir == null) {
            serverRootDir = "."; //assume server is started from server root, e.g. server/cspace
        }
    }


    /**
     * @return the serviceConfig
     */
    public ServiceConfig getServiceConfig() {
        return getServicesConfigReader().getConfiguration();
    }

    /**
     * @return the clientType
     */
    public ClientType getClientType() {
        return getServicesConfigReader().getClientType();
    }

    /**
     * @return the servicesConfigReader
     */
    public ServicesConfigReaderImpl getServicesConfigReader() {
        return servicesConfigReader;
    }

    /**
     * @return the tenantBindingConfigReader
     */
    public TenantBindingConfigReaderImpl getTenantBindingConfigReader() {
        return tenantBindingConfigReader;
    }
}

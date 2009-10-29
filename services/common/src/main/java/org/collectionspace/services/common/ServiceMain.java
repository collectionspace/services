/**
 * Copyright 2009 University of California at Berkeley
 */
package org.collectionspace.services.common;

import java.util.Hashtable;
import org.collectionspace.services.common.config.ServicesConfigReader;
import org.collectionspace.services.common.config.TenantBindingConfigReader;
import org.collectionspace.services.nuxeo.client.java.NuxeoConnector;
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
    private ServicesConfigReader servicesConfigReader;
    private TenantBindingConfigReader tenantBindingConfigReader;

    private ServiceMain() {
    }

    /**
     * getInstance returns the ServiceMain singleton instance after proper
     * initialization in a thread-safe manner
     * @return
     */
    public static ServiceMain getInstance() {
        if(instance == null){
            synchronized(ServiceMain.class){
                if(instance == null){
                    ServiceMain temp = new ServiceMain();
                    try{
                        temp.initialize();
                    }catch(Exception e){
                        instance = null;
                        if(e instanceof RuntimeException){
                            throw (RuntimeException) e;
                        }else{
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
        if(getClientType().equals(ClientType.JAVA)){
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
        try{
            if(nuxeoConnector != null){
                nuxeoConnector.release();
            }
            instance = null;
        }catch(Exception e){
            e.printStackTrace();
            //gobble it
        }
    }

    private void readConfig() throws Exception {
        //read service config
        servicesConfigReader = new ServicesConfigReader(getServerRootDir());
        getServicesConfigReader().read();

        tenantBindingConfigReader = new TenantBindingConfigReader(getServerRootDir());
        getTenantBindingConfigReader().read();
    }

    void retrieveAllWorkspaceIds() throws Exception {
        //all configs are read, connector is initialized, retrieve workspaceids
        getTenantBindingConfigReader().retrieveAllWorkspaceIds();
    }

    /**
     * @return the nuxeoConnector
     */
    public NuxeoConnector getNuxeoConnector() {
        return nuxeoConnector;
    }

    private void setServerRootDir() {
        serverRootDir = System.getProperty("jboss.server.home.dir");
        if(serverRootDir == null){
            serverRootDir = "."; //assume server is started from server root, e.g. server/cspace
        }
    }

    /**
     * @return the serverRootDir
     */
    public String getServerRootDir() {
        return serverRootDir;
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
    public ServicesConfigReader getServicesConfigReader() {
        return servicesConfigReader;
    }

    /**
     * @return the tenantBindingConfigReader
     */
    public TenantBindingConfigReader getTenantBindingConfigReader() {
        return tenantBindingConfigReader;
    }
}

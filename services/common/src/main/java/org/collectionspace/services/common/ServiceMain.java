/**
 * Copyright 2009 University of California at Berkeley
 */
package org.collectionspace.services.common;

import java.io.File;
import java.util.Hashtable;
import java.util.List;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;
import org.collectionspace.services.common.ServiceConfig.NuxeoWorkspace;
import org.collectionspace.services.common.ServiceConfig.NuxeoWorkspace.Workspace;
import org.collectionspace.services.nuxeo.NuxeoConnector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Main class for Services layer. It reads configuration and performs service
 * level initialization. It is a singleton.
 * @author 
 */
public class ServiceMain {

    private static volatile ServiceMain instance = null;
    final private static String CONFIG_FILE_NAME = "./cs/service-config.xml";
    final Logger logger = LoggerFactory.getLogger(ServiceMain.class);
    private ServiceConfig serviceConfig;
    private Hashtable<String, String> serviceWorkspaces = new Hashtable<String, String>();
    private NuxeoConnector nuxeoConnector;

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
        serviceConfig = readConfig();
        nuxeoConnector = NuxeoConnector.getInstance();
        nuxeoConnector.initialize(serviceConfig.getNuxeoClientConfig());
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
            serviceWorkspaces.clear();
            instance = null;
        }catch(Exception e){
            e.printStackTrace();
        //gobble it
        }
    }

    private ServiceConfig readConfig() throws Exception {
        JAXBContext jc = JAXBContext.newInstance(ServiceConfig.class);
        Unmarshaller um = jc.createUnmarshaller();
        File configFile = new File(CONFIG_FILE_NAME);
        if(!configFile.exists()){
            String msg = "Could not find configuration file " + CONFIG_FILE_NAME;
            logger.error(msg);
            throw new RuntimeException(msg);
        }
        ServiceConfig sconfig = (ServiceConfig) um.unmarshal(configFile);
        if(logger.isDebugEnabled()){
            logger.debug("readConfig() read config file " + configFile.getAbsolutePath());
        }
        return sconfig;
    }

    synchronized public void getWorkspaceIds() throws Exception {
        Hashtable<String, String> workspaceIds = nuxeoConnector.retrieveWorkspaceIds();
        NuxeoWorkspace nuxeoWorkspace = serviceConfig.getNuxeoWorkspace();
        List<Workspace> workspaces = nuxeoWorkspace.getWorkspace();
        for(Workspace workspace : workspaces){
            String workspaceId = workspaceIds.get(workspace.getWorkspaceName().toLowerCase());
            if(workspaceId == null){
                logger.error("failed to retrieve workspace id for " + workspace.getWorkspaceName());
                //FIXME: should we throw an exception here?
                continue;
            }
            serviceWorkspaces.put(workspace.getServiceName(), workspaceId);
            if(logger.isDebugEnabled()){
                logger.debug("retrieved workspace id=" + workspaceId +
                        " service=" + workspace.getServiceName() +
                        " workspace=" + workspace.getWorkspaceName());
            }
        }
    }

    /**
     * @return the serviceConfig
     */
    public ServiceConfig getServiceConfig() {
        return serviceConfig;
    }

    synchronized public String getWorkspaceId(String serviceName) {
        return serviceWorkspaces.get(serviceName);
    }

    /**
     * @return the nuxeoConnector
     */
    public NuxeoConnector getNuxeoConnector() {
        return nuxeoConnector;
    }
}

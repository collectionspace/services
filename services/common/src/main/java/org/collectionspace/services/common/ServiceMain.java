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
import org.collectionspace.services.nuxeo.client.java.NuxeoConnector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Main class for Services layer. It reads configuration and performs service
 * level initialization. It is a singleton.
 * @author 
 */
public class ServiceMain {

    private static volatile ServiceMain instance = null;
    final public static String CSPACE_DIR_NAME = "cspace";
    final public static String CONFIG_DIR_NAME = "config" + File.separator + "services";
    final private static String CONFIG_FILE_NAME = "service-config.xml";
    final Logger logger = LoggerFactory.getLogger(ServiceMain.class);
    private ServiceConfig serviceConfig;
    private Hashtable<String, String> serviceWorkspaces = new Hashtable<String, String>();
    private NuxeoConnector nuxeoConnector;
    private String serverRootDir = null;
    private NuxeoClientType nuxeoClientType = null;

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
        serviceConfig = readConfig();
        if(getNuxeoClientType().equals(NuxeoClientType.JAVA)){
            nuxeoConnector = NuxeoConnector.getInstance();
            nuxeoConnector.initialize(serviceConfig.getNuxeoClientConfig());
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
        String configFileName = getServerRootDir() +
                File.separator + CSPACE_DIR_NAME +
                File.separator + CONFIG_DIR_NAME +
                File.separator + CONFIG_FILE_NAME;
        File configFile = new File(configFileName);
        if(!configFile.exists()){
            String msg = "Could not find configuration file " + configFileName;
            logger.error(msg);
            throw new RuntimeException(msg);
        }
        ServiceConfig sconfig = (ServiceConfig) um.unmarshal(configFile);
        if(logger.isDebugEnabled()){
            logger.debug("readConfig() read config file " + configFile.getAbsolutePath());
        }
        nuxeoClientType = sconfig.getNuxeoClientConfig().getClientType();
        if(logger.isDebugEnabled()) {
            logger.debug("using Nuxeo client=" + nuxeoClientType.toString());
        }
        return sconfig;
    }

    synchronized public void getWorkspaceIds() throws Exception {
        Hashtable<String, String> workspaceIds = new Hashtable<String, String>();

        if(getNuxeoClientType().equals(NuxeoClientType.JAVA)){
            workspaceIds = nuxeoConnector.retrieveWorkspaceIds();
        }
        NuxeoWorkspace nuxeoWorkspace = serviceConfig.getNuxeoWorkspace();
        List<Workspace> workspaces = nuxeoWorkspace.getWorkspace();
        String workspaceId = null;
        for(Workspace workspace : workspaces){
            if(getNuxeoClientType().equals(NuxeoClientType.JAVA)){
                workspaceId = workspaceIds.get(workspace.getWorkspaceName().toLowerCase());
                if(workspaceId == null){
                    logger.warn("failed to retrieve workspace id for " + workspace.getWorkspaceName());
                    //FIXME: should we throw an exception here?
                    continue;
                }
            }else{
                workspaceId = workspace.getWorkspaceId();
                if(workspaceId == null || "".equals(workspaceId)){
                    logger.error("could not find workspace id for " + workspace.getWorkspaceName());
                    //FIXME: should we throw an exception here?
                    continue;
                }
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
     * @return the nuxeoClientType
     */
    public NuxeoClientType getNuxeoClientType() {
        return nuxeoClientType;
    }
}

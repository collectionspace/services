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
package org.collectionspace.services.nuxeo.client.java;

import java.io.File;
import java.util.Collection;
import java.util.Hashtable;
import java.util.Iterator;

import org.collectionspace.services.config.RepositoryClientConfigType;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.DocumentModelList;
import org.nuxeo.ecm.core.api.repository.RepositoryInstance;
import org.nuxeo.ecm.core.client.DefaultLoginHandler;
import org.nuxeo.ecm.core.client.NuxeoApp;
import org.nuxeo.ecm.core.client.NuxeoClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * NuxeoConnector creates Nuxeo remoting runtime and provides access to Nuxeo client.
 * @author 
 */
public class NuxeoConnector {

    public final static String NUXEO_CLIENT_DIR = "nuxeo-client";
    private Logger logger = LoggerFactory.getLogger(NuxeoConnector.class);
    private static final NuxeoConnector self = new NuxeoConnector();
	private static final String NUXEO_CLIENT_USERNAME = "NUXEO_CLIENT_USERNAME";
	private static final String NUXEO_CLIENT_PASSWORD = "NUXEO_CLIENT_PASSWORD";
    private NuxeoApp app;
    private NuxeoClient client;
    private volatile boolean initialized = false; //use volatile for lazy initialization in singleton
    private RepositoryClientConfigType repositoryClientConfig;

    private NuxeoConnector() {
    }

    public final static NuxeoConnector getInstance() {
        return self;
    }

    private String getClientUserName(RepositoryClientConfigType repositoryClientConfig) {
    	String username = System.getenv(NUXEO_CLIENT_USERNAME);
    	if (username == null) {
    		username = repositoryClientConfig.getUser();
    	}
    	return username;
    }
    
    private String getClientPassword(RepositoryClientConfigType repositoryClientConfig) {
    	String password = System.getenv(NUXEO_CLIENT_PASSWORD);
    	if (password == null) {
    		password = repositoryClientConfig.getPassword();
    	}
    	return password;
    }
    
    /**
     * initialize initialize the Nuxeo connector. It makes sure that the connector
     * is initialized in a thread-safe manner and not initialized more than once.
     * Initialization involves starting Nuxeo runtime, loading Nuxeo APIs jars
     * in OSGI container as well as establishing initial connection.
     * @param repositoryClientConfig
     * @throws java.lang.Exception
     */
    public void initialize(RepositoryClientConfigType repositoryClientConfig) throws Exception {

        if(initialized == false){
            synchronized(this){
                if(initialized == false){
                    try{
                        this.repositoryClientConfig = repositoryClientConfig;
                        setProperties(repositoryClientConfig);
                        app = new NuxeoApp();
                        app.start();
                        if(logger.isDebugEnabled()){
                            logger.debug("initialize() NuxeoApp started");
                        }
                        loadBundles();
                        client = NuxeoClient.getInstance();
                        String username = getClientUserName(repositoryClientConfig);
                        String password = getClientPassword(repositoryClientConfig);
//                        DefaultLoginHandler loginHandler = new DefaultLoginHandler(
//                                repositoryClientConfig.getUser(), repositoryClientConfig.getPassword());
                        DefaultLoginHandler loginHandler = new DefaultLoginHandler(username, password);
                        client.setLoginHandler(loginHandler);
                        client.connect(repositoryClientConfig.getHost(),
                                repositoryClientConfig.getPort());
                        if(logger.isDebugEnabled()){
                            logger.debug("initialize(): connection successful port=" +
                                    repositoryClientConfig.getPort());
                        }
                        initialized = true;
                    }catch(Exception e){
                        if(logger.isDebugEnabled()){
                            logger.debug("Caught exception while initializing", e);
                        }
                    }
                }
            }
        }
    }

    /**
     * release releases resources occupied by Nuxeo remoting client runtime
     * @throws java.lang.Exception
     */
    public void release() throws Exception {
        if(initialized == true){
            client.disconnect();
            app.shutdown();
        }
    }

    private void loadBundles() throws Exception {
        String bundles = "nuxeo-client/lib/nuxeo-runtime-*:nuxeo-client/lib/nuxeo-*";
//        String serverRootDir = ServiceMain.getInstance().getServerRootDir();
        //can't call ServiceMain here because loadBundles is called within
        //the iniitialization context of ServiceMain, recrusion problem
        String serverRootDir = System.getProperty("jboss.server.home.dir");
        if(serverRootDir == null){
            serverRootDir = "."; //assume server is started from server root, e.g. server/cspace
        }
        File clientLibDir = new File(serverRootDir);
        if(!clientLibDir.exists()){
            String msg = "Library bundles requried to deploy Nuxeo client not found: " +
                    " directory named nuxeo-client with bundles does not exist in " + serverRootDir;
            logger.error(msg);
            throw new IllegalStateException(msg);
        }
        Collection<File> files = null;
        if(bundles != null){
            files = NuxeoApp.getBundleFiles(new File(serverRootDir), bundles, ":");
        }
        if(logger.isDebugEnabled()){
            logger.debug("loadBundles(): deploying bundles: " + files);
        }
        if(files != null){
            app.deployBundles(files);
        }
    }

    private void setProperties(RepositoryClientConfigType repositoryClientConfig) {
        System.setProperty("nuxeo.client.on.jboss", Boolean.TRUE.toString());
        System.setProperty("org.nuxeo.runtime.server.enabled", Boolean.FALSE.toString());
        System.setProperty("org.nuxeo.runtime.server.port", "" + repositoryClientConfig.getPort());
        System.setProperty("org.nuxeo.runtime.server.host", repositoryClientConfig.getHost());
        //System.setProperty("org.nuxeo.runtime.1.3.3.streaming.port", "3233");
        System.setProperty("org.nuxeo.runtime.streaming.serverLocator", "socket://" + repositoryClientConfig.getHost() + ":3233");
        System.setProperty("org.nuxeo.runtime.streaming.isServer", Boolean.FALSE.toString());
        //org.nuxeo.client.remote is part of the fix to Nuxeo Runtime to use Java Remote APIs
        //from JBoss
        System.setProperty("org.nuxeo.client.remote", Boolean.TRUE.toString());
    }

    /**
     * getClient get Nuxeo client for accessing Nuxeo services remotely using
     * Nuxeo Java (EJB) Remote APIS
     * @return NuxeoClient
     * @throws java.lang.Exception
     */
    public NuxeoClient getClient() throws Exception {
        if(initialized == true){
            if(client.isConnected()){
                client.login();
                return client;
            }else{
                client.forceConnect(repositoryClientConfig.getHost(), repositoryClientConfig.getPort());
                if(logger.isDebugEnabled()){
                    logger.debug("getClient(): connection successful port=" +
                            repositoryClientConfig.getPort());
                }
                return client;
            }
        }
        String msg = "NuxeoConnector is not initialized!";
        logger.error(msg);
        throw new IllegalStateException(msg);
    }

    /**
     * getRepositorySession get session to default repository
     * @return RepositoryInstance
     * @throws java.lang.Exception
     */
    public RepositoryInstance getRepositorySession() throws Exception {
        RepositoryInstance repoSession = getClient().openRepository();
        if(logger.isDebugEnabled()){
            logger.debug("getRepositorySession() opened repository session");
        }
        return repoSession;
    }

    /**
     * releaseRepositorySession releases given repository session
     * @param repoSession
     * @throws java.lang.Exception
     */
    public void releaseRepositorySession(RepositoryInstance repoSession) throws Exception {
        if(repoSession != null){
            getClient().releaseRepository(repoSession);

            if(logger.isDebugEnabled()){
                logger.debug("releaseRepositorySession() released repository session");
            }
        }
    }

    /**
     * retrieveWorkspaceIds retrieves all workspace ids from default repository
     * @param tenantDomain domain representing tenant
     * @return
     * @throws java.lang.Exception
     */
    public Hashtable<String, String> retrieveWorkspaceIds(String tenantDomain) throws Exception {
        RepositoryInstance repoSession = null;
        Hashtable<String, String> workspaceIds = new Hashtable<String, String>();
        try{
            repoSession = getRepositorySession();
            DocumentModel rootDoc = repoSession.getRootDocument();
            DocumentModelList rootChildrenList = repoSession.getChildren(rootDoc.getRef());
            Iterator<DocumentModel> diter = rootChildrenList.iterator();
            while(diter.hasNext()){
                DocumentModel domain = diter.next();
                String domainPath = "/" + tenantDomain;
                if(!domain.getPathAsString().equalsIgnoreCase(domainPath)){
                    continue;
}
                if(logger.isDebugEnabled()){
                    logger.debug("domain=" + domain.toString());
                }
                DocumentModelList domainChildrenList = repoSession.getChildren(domain.getRef());
                Iterator<DocumentModel> witer = domainChildrenList.iterator();
                while(witer.hasNext()){
                    DocumentModel childNode = witer.next();
                    if("Workspaces".equalsIgnoreCase(childNode.getName())){
                        DocumentModelList workspaceList = repoSession.getChildren(childNode.getRef());
                        Iterator<DocumentModel> wsiter = workspaceList.iterator();
                        while(wsiter.hasNext()){
                            DocumentModel workspace = wsiter.next();
                            if(logger.isDebugEnabled()){
                                logger.debug("workspace name=" + workspace.getName() +
                                        " id=" + workspace.getId());
                            }
                            workspaceIds.put(workspace.getName().toLowerCase(), workspace.getId());
                        }
                    }
                }
            }
        }catch(Exception e){
            if(logger.isDebugEnabled()){
                logger.debug("retrieveWorkspaceIds() caught exception ", e);
            }
            throw e;
        }finally{
            if(repoSession != null){
                releaseRepositorySession(repoSession);
            }
        }
        return workspaceIds;
    }
}

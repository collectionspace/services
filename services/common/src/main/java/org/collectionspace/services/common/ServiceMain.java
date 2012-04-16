/**
 * Copyright 2009-2010 University of California at Berkeley
 */
package org.collectionspace.services.common;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.UUID;

import javax.naming.NamingException;
import javax.servlet.ServletContext;
import javax.sql.DataSource;

import org.collectionspace.authentication.AuthN;

import org.collectionspace.services.config.service.InitHandler;
import org.collectionspace.services.common.authorization_mgt.AuthorizationCommon;
import org.collectionspace.services.common.config.ServicesConfigReaderImpl;
import org.collectionspace.services.common.config.TenantBindingConfigReaderImpl;
import org.collectionspace.services.common.init.IInitHandler;
import org.collectionspace.services.common.security.SecurityUtils;
import org.collectionspace.services.common.storage.JDBCTools;
import org.collectionspace.services.common.storage.DatabaseProductType;
import org.collectionspace.services.config.ClientType;
import org.collectionspace.services.config.ServiceConfig;
import org.collectionspace.services.config.service.ServiceBindingType;
import org.collectionspace.services.config.tenant.TenantBindingType;
import org.collectionspace.services.config.types.PropertyItemType;
import org.collectionspace.services.config.types.PropertyType;
import org.collectionspace.services.nuxeo.client.java.NuxeoConnectorEmbedded;
import org.collectionspace.services.nuxeo.client.java.TenantRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Main class for Services layer. It reads configuration and performs service
 * level initialization. It is a singleton.
 * @author 
 */
public class ServiceMain {

    final Logger logger = LoggerFactory.getLogger(ServiceMain.class);
    /**
     * volatile is used here to assume about ordering (post JDK 1.5)
     */
    private static volatile ServiceMain instance = null;
    private static volatile boolean initFailed = false;
    
    private NuxeoConnectorEmbedded nuxeoConnector;
    private static ServletContext servletContext = null;
    private String serverRootDir = null;
    private ServicesConfigReaderImpl servicesConfigReader;
    private TenantBindingConfigReaderImpl tenantBindingConfigReader;
    
    private static final String SERVER_HOME_PROPERTY = "catalina.home";
    
    private ServiceMain() {
    	//empty
    }
    
    /*
     * FIXME: REM - This method is no longer necessary and can should be removed.
     * 
     * Set this singletons ServletContext without any call to initialize
     */
    @Deprecated
    private static void setServletContext(ServletContext servletContext) {
		if (servletContext != null) {
	    	synchronized (ServiceMain.class) {
	    		ServiceMain.servletContext = servletContext;
	    	}
		}
    }
    
    public boolean inServletContext() {
    	return ServiceMain.servletContext != null;
    }
    
    public static ServiceMain getInstance(ServletContext servletContext) {
    	ServiceMain.servletContext = servletContext;
    	return ServiceMain.getInstance();
    }
    
    /**
     * getInstance returns the ServiceMain singleton instance after proper
     * initialization in a thread-safe manner
     * @return
     */
    public static ServiceMain getInstance() {
        if (instance == null && initFailed == false) {
            synchronized (ServiceMain.class) {
                if (instance == null && initFailed == false) {
                    ServiceMain temp = new ServiceMain();
                    try {
                    	//assume the worse
                    	initFailed = true;
                        temp.initialize();
                    	//celebrate success
                        initFailed = false;
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
        
        if (instance == null) {
        	throw new RuntimeException("Could not initialize the CollectionSpace services.  Please see the CollectionSpace services log file(s) for details.");
        }
        
        return instance;
    }

    private void initialize() throws Exception {
    	if (logger.isTraceEnabled() == true)     	{
    		System.out.print("About to initialize ServiceMain singleton - Pausing 5 seconds for you to attached the debugger");
    		long startTime, currentTime;
    		currentTime = startTime = System.currentTimeMillis();
    		long stopTime = startTime + 5 * 1000; //5 seconds
    		do {
    			if (currentTime % 1000 == 0) {
    				System.out.print(".");
    			}
    			currentTime = System.currentTimeMillis();
    		} while (currentTime < stopTime);
    			
    		System.out.println();
    		System.out.println("Resuming cspace services initialization.");
    	}
    	
    	setDataSources();
    	setServerRootDir();
        readConfig();
        propagateConfiguredProperties();
        //
        // Start up and initialize our embedded Nuxeo server instance
        //
        if (getClientType().equals(ClientType.JAVA)) {
            nuxeoConnector = NuxeoConnectorEmbedded.getInstance();
            nuxeoConnector.initialize(
            		getServerRootDir(),
            		getServicesConfigReader().getConfiguration().getRepositoryClient(),
            		ServiceMain.servletContext);
        } else {
        	//
        	// Exit if we don't have the correct/known client type
        	//
        	throw new RuntimeException("Unknown CollectionSpace services client type: " + getClientType());
        }
        //
        // Create all the default user accounts and permissions
        //
        try {
        	AuthorizationCommon.createDefaultWorkflowPermissions(tenantBindingConfigReader);        	
        	AuthorizationCommon.createDefaultAccounts(tenantBindingConfigReader);     
        } catch(Throwable e) {        	
        	logger.error("Default accounts and permissions setup failed with exception(s): " + e.getLocalizedMessage(), e);
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
        servicesConfigReader.read();

        tenantBindingConfigReader = new TenantBindingConfigReaderImpl(getServerRootDir()); 
        tenantBindingConfigReader.read();
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

    public void firePostInitHandlers() throws Exception {
    	DataSource dataSource = JDBCTools.getDataSource(JDBCTools.NUXEO_REPOSITORY_NAME);
        Hashtable<String, TenantBindingType> tenantBindingTypeMap = tenantBindingConfigReader.getTenantBindings();
        //Loop through all tenants in tenant-bindings.xml
        for (TenantBindingType tbt: tenantBindingTypeMap.values()){
            //String name = tbt.getName();
            //String id = tbt.getId();
            //Loop through all the services in this tenant
            List<ServiceBindingType> sbtList = tbt.getServiceBindings();
            for (ServiceBindingType sbt: sbtList){
                //Get the list of InitHandler elements, extract the first one (only one supported right now) and fire it using reflection.
                List<org.collectionspace.services.config.service.InitHandler> list = sbt.getInitHandler();
                if (list!=null && list.size()>0){
                	org.collectionspace.services.config.service.InitHandler handlerType = list.get(0);
                    String initHandlerClassname = handlerType.getClassname();

                    List<org.collectionspace.services.config.service.InitHandler.Params.Field>
                            fields = handlerType.getParams().getField();

                    List<org.collectionspace.services.config.service.InitHandler.Params.Property>
                            props = handlerType.getParams().getProperty();

                    //org.collectionspace.services.common.service.InitHandler.Fields ft = handlerType.getFields();
                    //List<String> fields = ft.getField();
                    Object o = instantiate(initHandlerClassname, IInitHandler.class);
                    if (o != null && o instanceof IInitHandler){
                        IInitHandler handler = (IInitHandler)o;
                        handler.onRepositoryInitialized(dataSource, sbt, fields, props);
                        //The InitHandler may be the default one,
                        //  or specialized classes which still implement this interface and are registered in tenant-bindings.xml.
                    }
                }
            }
        }
    }

    /*
     * A generic mechanism for instantiating a instance/object from a class name.
     */
    public Object instantiate(String clazz, Class<?> castTo) throws Exception {
        ClassLoader tccl = Thread.currentThread().getContextClassLoader();
        clazz = clazz.trim();
        Class<?> c = tccl.loadClass(clazz);
        if (castTo.isAssignableFrom(c)) {
            return c.newInstance();
        }
        return null;
    }

    void retrieveAllWorkspaceIds() throws Exception {
        //all configs are read, connector is initialized, retrieve workspaceids
        Hashtable<String, TenantBindingType> tenantBindings =
        	tenantBindingConfigReader.getTenantBindings();
        TenantRepository.get().setup(tenantBindings);
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
    public NuxeoConnectorEmbedded getNuxeoConnector() {
        return nuxeoConnector;
    }
    
    /**
     * @return the serverRootDir
     */
    public String getServerRootDir() {
        return serverRootDir;
    }

    /*
     * Save a copy of the DataSource instances that exist in our initial JNDI context.  For some reason, after starting up
     * our instance of embedded Nuxeo, we can find our datasources.  Therefore, we need to preserve the datasources in these
     * static members.
     */
    private void setDataSources() throws NamingException {
    	//
    	// As a side-effect of calling JDBCTools.getDataSource(...), the DataSource instance will be
    	// cached in a static hash map of the JDBCTools class.  This will speed up lookups as well as protect our
    	// code from JNDI lookup problems -for example, if the JNDI context gets stepped on or corrupted.
    	//
    	DataSource cspaceDataSource = JDBCTools.getDataSource(JDBCTools.CSPACE_REPOSITORY_NAME);
    	DataSource nuxeoDataSource = JDBCTools.getDataSource(JDBCTools.NUXEO_REPOSITORY_NAME);
    	//
    	// Set our AuthN's datasource to be the cspaceDataSource
    	//
    	AuthN.setDataSource(cspaceDataSource);
    }
    
    private void setServerRootDir() {
        serverRootDir = System.getProperty(SERVER_HOME_PROPERTY);
        if (serverRootDir == null) {
            serverRootDir = "."; //assume server is started from server root, e.g. server/cspace
            logger.warn("System property '" +
            		SERVER_HOME_PROPERTY + "' was not set.  Using \"" +
            		serverRootDir +
            		"\" instead.");
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

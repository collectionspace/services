/**
 * Copyright 2009-2010 University of California at Berkeley
 */
package org.collectionspace.services.common;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;

import javax.naming.NamingException;
import javax.servlet.ServletContext;
import javax.sql.DataSource;

import org.collectionspace.authentication.AuthN;

import org.collectionspace.services.common.authorization_mgt.AuthorizationCommon;
import org.collectionspace.services.common.config.ConfigReader;
import org.collectionspace.services.common.config.ConfigUtils;
import org.collectionspace.services.common.config.ServicesConfigReaderImpl;
import org.collectionspace.services.common.config.TenantBindingConfigReaderImpl;
import org.collectionspace.services.common.init.AddIndices;
import org.collectionspace.services.config.service.InitHandler.Params.Field;
import org.collectionspace.services.common.init.IInitHandler;
import org.collectionspace.services.common.storage.DatabaseProductType;
import org.collectionspace.services.common.storage.JDBCTools;
import org.collectionspace.services.config.ClientType;
import org.collectionspace.services.config.ServiceConfig;
import org.collectionspace.services.config.service.ServiceBindingType;
import org.collectionspace.services.config.tenant.RepositoryDomainType;
import org.collectionspace.services.config.tenant.TenantBindingType;
import org.collectionspace.services.config.types.PropertyItemType;
import org.collectionspace.services.config.types.PropertyType;
import org.collectionspace.services.nuxeo.client.java.NuxeoConnectorEmbedded;
import org.collectionspace.services.nuxeo.client.java.TenantRepository;
import org.jboss.resteasy.spi.ResteasyProviderFactory;

import org.apache.tomcat.dbcp.dbcp.BasicDataSource;

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
    private UriTemplateRegistry uriTemplateRegistry = new UriTemplateRegistry();
    
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
    	
    	setServerRootDir();
        readConfig();
    	setDataSources();
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
        
        /*
         * This might be useful for something, but the reader grants are better handled in the ReportPostInitHandler.
        try {
        	handlePostNuxeoInitDBTasks();
        } catch(Throwable e) {        	
        	logger.error("handlePostNuxeoInitDBTasks failed with exception(s): " + e.getLocalizedMessage(), e);
        }
        */
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
    
	/**
	 * Create required indexes (aka indices) in database tables not associated
	 * with any specific tenant.
	 * 
	 * We need to loop over each repository/db declared in the tenant bindings.
	 * The assumption here is that each repo/db is a Nuxeo repo/DB.
	 * 
	 * @throws Exception
	 */
	void createRequiredIndices() throws Exception {
        Hashtable<String, TenantBindingType> tenantBindingTypeMap = tenantBindingConfigReader.getTenantBindings();

        //
        //Loop through all tenants in tenant-bindings.xml
        //
        for (TenantBindingType tbt : tenantBindingTypeMap.values()) {
        	List<String> repositoryNameList = ConfigUtils.getRepositoryNameList(tbt);
			if (repositoryNameList != null && repositoryNameList.isEmpty() == false) {
				//
				// Loop through each repo/DB defined in a tenant bindings file
				//
				for (String repositoryName : repositoryNameList) {
					// Define a set of columns (fields) and their associated
					// tables, on which database indexes should always be created
					final String COLLECTIONSPACE_CORE_TABLE_NAME = "collectionspace_core";
					final String NUXEO_FULLTEXT_TABLE_NAME = "fulltext";
					final String NUXEO_HIERARCHY_TABLE_NAME = "hierarchy";
			
					Map<Integer, List<String>> fieldsToIndex = new HashMap<Integer, List<String>>();
					fieldsToIndex.put(1, new ArrayList<String>(Arrays.asList(COLLECTIONSPACE_CORE_TABLE_NAME, "tenantid")));
					fieldsToIndex.put(2, new ArrayList<String>(Arrays.asList(COLLECTIONSPACE_CORE_TABLE_NAME, "updatedat")));
					fieldsToIndex.put(3, new ArrayList<String>(Arrays.asList(NUXEO_FULLTEXT_TABLE_NAME, "jobid")));
					fieldsToIndex.put(4, new ArrayList<String>(Arrays.asList(NUXEO_HIERARCHY_TABLE_NAME, "name")));
			
					// Invoke existing post-init code to create these indexes,
					// sending in the set of values above, in contrast to
					// drawing these values from per-tenant configuration.
//					DataSource dataSource = JDBCTools.getDataSource(JDBCTools.NUXEO_DATASOURCE_NAME);
					AddIndices addindices = new AddIndices();
					List<Field> fields = new ArrayList<Field>();
					for (Map.Entry<Integer, List<String>> entry : fieldsToIndex.entrySet()) {
						Field field = new Field();
						field.setTable(entry.getValue().get(0)); // Table name from List
																	// item 0
						field.setCol(entry.getValue().get(1)); // Column name from List item
																// 1
						fields.add(field);
					}
					addindices.onRepositoryInitialized(JDBCTools.NUXEO_DATASOURCE_NAME, repositoryName, null, fields, null);
				}
			} else {
				String errMsg = "repositoryNameList was empty or null.";
				logger.error(errMsg);
				throw new Exception(errMsg);
			}
        }
	}

    public void firePostInitHandlers() throws Exception {
        Hashtable<String, TenantBindingType> tenantBindingTypeMap = tenantBindingConfigReader.getTenantBindings();
        //
        //Loop through all tenants in tenant-bindings.xml
        //
        for (TenantBindingType tbt : tenantBindingTypeMap.values()) {
        	//
        	//Loop through all the services in this tenant
        	//
            List<ServiceBindingType> sbtList = tbt.getServiceBindings();
            for (ServiceBindingType sbt: sbtList) {
            	String repositoryName = ConfigUtils.getRepositoryName(tbt, sbt.getRepositoryDomain()); // Each service can have a different repo domain
                //Get the list of InitHandler elements, extract the first one (only one supported right now) and fire it using reflection.
                List<org.collectionspace.services.config.service.InitHandler> list = sbt.getInitHandler();
                if (list != null && list.size() > 0) {
                	org.collectionspace.services.config.service.InitHandler handlerType = list.get(0);  // REM - 12/2012: We might want to think about supporting multiple post-init handlers
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
                        handler.onRepositoryInitialized(JDBCTools.NUXEO_DATASOURCE_NAME, repositoryName, sbt, fields, props);
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
    
    /**
     * @return the server resources path
     */
    public String getServerResourcesPath() {
        return getServerRootDir() + File.separator + ConfigReader.RESOURCES_DIR_PATH + File.separator;
    }
    
    public InputStream getResourceAsStream(String resourceName) throws FileNotFoundException {
    	InputStream result = new FileInputStream(new File(getServerResourcesPath() + resourceName));
    	return result;
    }

    /*
     * Save a copy of the DataSource instances that exist in our initial JNDI context.  For some reason, after starting up
     * our instance of embedded Nuxeo, we can find our datasources.  Therefore, we need to preserve the datasources in these
     * static members.
     */
    private void setDataSources() throws NamingException, Exception {
    	final String DB_EXISTS_QUERY_PSQL = 
    			"SELECT 1 AS result FROM pg_database WHERE datname=?";
    	final String DB_EXISTS_QUERY_MYSQL = 
    			"SELECT 1 AS result FROM INFORMATION_SCHEMA.SCHEMATA WHERE SCHEMA_NAME=?";
    	//
    	// As a side-effect of calling JDBCTools.getDataSource(...), the DataSource instance will be
    	// cached in a static hash map of the JDBCTools class.  This will speed up lookups as well as protect our
    	// code from JNDI lookup problems -for example, if the JNDI context gets stepped on or corrupted.
    	//
    	DataSource cspaceDataSource = JDBCTools.getDataSource(JDBCTools.CSPACE_DATASOURCE_NAME);
    	DataSource nuxeoDataSource = JDBCTools.getDataSource(JDBCTools.NUXEO_DATASOURCE_NAME);
    	DataSource nuxeoMgrDataSource = JDBCTools.getDataSource(JDBCTools.NUXEO_MANAGER_DATASOURCE_NAME);
    	DataSource nuxeoReaderDataSource = JDBCTools.getDataSource(JDBCTools.NUXEO_READER_DATASOURCE_NAME);
    	
    	// We need to fetch the user name and password from the nuxeoDataSource, to do grants below
    	org.apache.tomcat.dbcp.dbcp.BasicDataSource tomcatDataSource =
    			(org.apache.tomcat.dbcp.dbcp.BasicDataSource)nuxeoDataSource;
    	// Get the template URL value from the JNDI datasource and substitute the databaseName
    	String nuxeoUser = tomcatDataSource.getUsername();
    	String nuxeoPW = tomcatDataSource.getPassword();
    	// Get reader data source, if any
    	String readerUser = null;
    	String readerPW = null;
    	if(nuxeoReaderDataSource!= null) {
	    	tomcatDataSource =
	    			(org.apache.tomcat.dbcp.dbcp.BasicDataSource)nuxeoReaderDataSource;
	    	// Get the template URL value from the JNDI datasource and substitute the databaseName
	    	readerUser = tomcatDataSource.getUsername();
	    	readerPW = tomcatDataSource.getPassword();
    	}
    	
    	//
    	// Set our AuthN's datasource to be the cspaceDataSource
    	//
    	AuthN.setDataSource(cspaceDataSource);

    	// Get the NuxeoDS info and create the necessary databases.
    	// Consider the tenant bindings to find and get the data sources for each tenant.
    	// There may be only one, one per tenant, or something in between.
    	DatabaseProductType dbType = JDBCTools.getDatabaseProductType(
    			JDBCTools.CSPACE_DATASOURCE_NAME,
    			JDBCTools.DEFAULT_CSPACE_DATABASE_NAME); // only returns PG or MYSQL
    	String dbExistsQuery = (dbType==DatabaseProductType.POSTGRESQL)?
    								DB_EXISTS_QUERY_PSQL : DB_EXISTS_QUERY_MYSQL;

    	Hashtable<String, TenantBindingType> tenantBindings =
    			tenantBindingConfigReader.getTenantBindings();
    	HashSet<String> nuxeoDBsChecked = new HashSet<String>();
    	PreparedStatement pstmt = null;
    	Statement stmt = null;
		Connection conn = null;
		
    	try {
    		conn = nuxeoMgrDataSource.getConnection();
			// First check and create the roles as needed. (nuxeo and reader)

    		
    		pstmt = conn.prepareStatement(dbExistsQuery); // create a statement
			stmt = conn.createStatement();
			
    		for (TenantBindingType tenantBinding : tenantBindings.values()) {
    			String tId = tenantBinding.getId();
    			String tName = tenantBinding.getName();
    			List<RepositoryDomainType> repoDomainList = tenantBinding.getRepositoryDomain();
    			for (RepositoryDomainType repoDomain : repoDomainList) {
    				String repoDomainName = repoDomain.getName();
    				String dbName = JDBCTools.getDatabaseName(repoDomain.getRepositoryName());
    				if(nuxeoDBsChecked.contains(dbName)) {
    					if (logger.isDebugEnabled()) {
    						logger.debug("Another user of db: "+dbName+": Repo: "+repoDomainName+" and tenant: "
    								+tName+" (id:"+tId+")");
    					}
    				} else {
    					if (logger.isDebugEnabled()) {
    						logger.debug("Need to prepare db: "+dbName+" for Repo: "+repoDomainName+" and tenant: "
    								+tName+" (id:"+tId+")");
    					}

        				pstmt.setString(1, dbName);			// set dbName param
            			ResultSet rs = pstmt.executeQuery();
            			// extract data from the ResultSet
            			boolean dbExists = rs.next(); 
            			rs.close();
            			if(dbExists) {
        					if (logger.isDebugEnabled()) {
        						logger.debug("Database: "+dbName+" already exists.");
        					}
            			} else {
            				// Create the user as needed
            				createUserIfNotExists(conn, dbType, nuxeoUser, nuxeoPW);
            				if(readerUser!=null) {
            					createUserIfNotExists(conn, dbType, readerUser, readerPW);
            				}
            				// Create the database
            				createDatabaseWithRights(conn, dbType, dbName, nuxeoUser, nuxeoPW, readerUser, readerPW);
            			}
    					nuxeoDBsChecked.add(dbName);
    				}
    			} // Loop on repos for tenant
    		} // Loop on tenants
    	} catch(SQLException se) {
    		//Handle errors for JDBC
    		se.printStackTrace();
    	} catch(Exception e) {
    		//Handle errors for Class.forName
    		e.printStackTrace();
    	} finally {   //close resources
    		try {
    			if(stmt!=null) {
    				stmt.close();
    			}
    		} catch(SQLException se2) {
    			// nothing we can do
    		}
    		try{
    			if(conn!=null) {
    				conn.close();
    			}
    		}catch(SQLException se){
    			se.printStackTrace();
    		}
    	}
    }
    
    private void createUserIfNotExists(Connection conn, DatabaseProductType dbType,
    		String username, String userPW) throws Exception {
    	PreparedStatement pstmt = null;
    	Statement stmt = null;
    	final String USER_EXISTS_QUERY_PSQL = 
    			"SELECT 1 AS result FROM pg_roles WHERE rolname=?";
    	String userExistsQuery;
    	if(dbType==DatabaseProductType.POSTGRESQL) {
    		userExistsQuery = USER_EXISTS_QUERY_PSQL;
    	} else {
    		throw new UnsupportedOperationException("CreateUserIfNotExists only supports PSQL - MySQL NYI!");
    	}
    	try {
    		pstmt = conn.prepareStatement(userExistsQuery); // create a statement
    		pstmt.setString(1, username);			// set dbName param
    		ResultSet rs = pstmt.executeQuery();
    		// extract data from the ResultSet
    		boolean userExists = rs.next();
    		rs.close();
    		if(userExists) {
    			if (logger.isDebugEnabled()) {
    				logger.debug("User: "+username+" already exists.");
    			}
    		} else {
    			stmt = conn.createStatement();
    			String sql = "CREATE ROLE "+username+" WITH PASSWORD '"+userPW+"' LOGIN";
    			stmt.executeUpdate(sql);
    			// Really should do the grants as well. 
    			if (logger.isDebugEnabled()) {
    				logger.debug("Created Users: '"+username+"' and 'reader'");
    			}
    		}
    	} catch(Exception e) {
    		logger.error("createUserIfNotExists failed on exception: " + e.getLocalizedMessage());
    		throw e;	// propagate
    	} finally {   //close resources
    		try {
    			if(pstmt!=null) {
    				pstmt.close();
    			}
    			if(stmt!=null) {
    				stmt.close();
    			}
    		} catch(SQLException se) {
    			// nothing we can do
    		}
    	}
    }
    
    private void createDatabaseWithRights(Connection conn, DatabaseProductType dbType, String dbName,
    		String ownerName, String ownerPW, String readerName, String readerPW) throws Exception {
    	Statement stmt = null;
    	try {
			stmt = conn.createStatement();
    		if(dbType==DatabaseProductType.POSTGRESQL) {
    			// Postgres does not need passwords.
    			String sql = "CREATE DATABASE "+dbName+" ENCODING 'UTF8' OWNER "+ownerName;
    			stmt.executeUpdate(sql);
    			if (logger.isDebugEnabled()) {
    				logger.debug("Created db: '"+dbName+"' with owner: '"+ownerName+"'");
    			}
    			if(readerName!= null) {
	    			sql = "GRANT CONNECT ON DATABASE "+dbName+" TO "+readerName;
	    			stmt.executeUpdate(sql);
	    			if (logger.isDebugEnabled()) {
	    				logger.debug(" Granted connect rights on: '"+dbName+"' to reader: '"+readerName+"'");
	    			}
    			}
    			// Note that select rights for reader must be granted after Nuxeo startup.
    		} else if(dbType==DatabaseProductType.MYSQL) {
    			String sql = "CREATE database "+dbName+" DEFAULT CHARACTER SET utf8";
    			stmt.executeUpdate(sql);
    			sql = "GRANT ALL PRIVILEGES ON "+dbName+".* TO '"+ownerName+"'@'localhost' IDENTIFIED BY '"
    					+ownerPW+"' WITH GRANT OPTION";
    			stmt.executeUpdate(sql);
    			if (logger.isDebugEnabled()) {
    				logger.debug("Created db: '"+dbName+"' with owner: '"+ownerName+"'");
    			}
    			if(readerName!= null) {
	    			sql = "GRANT SELECT ON "+dbName+".* TO '"+readerName+"'@'localhost' IDENTIFIED BY '"
	    					+readerPW+"' WITH GRANT OPTION";
	    			stmt.executeUpdate(sql);
	    			if (logger.isDebugEnabled()) {
	    				logger.debug(" Granted SELECT rights on: '"+dbName+"' to reader: '"+readerName+"'");
	    			}
    			}
    		} else {
    			throw new UnsupportedOperationException("createDatabaseWithRights only supports PSQL - MySQL NYI!");
    		}
    	} catch(Exception e) {
    		logger.error("createDatabaseWithRights failed on exception: " + e.getLocalizedMessage());
    		throw e;	// propagate
    	} finally {   //close resources
    		try {
    			if(stmt!=null) {
    				stmt.close();
    			}
    		} catch(SQLException se) {
    			// nothing we can do
    		}
    	}

    }
    
    /*
     * This might be useful for something, but the reader grants are better handled in the ReportPostInitHandler.
    private void handlePostNuxeoInitDBTasks() throws Exception {
    	Statement stmt = null;
		Connection conn = null;
		
    	try {
        	DataSource nuxeoMgrDataSource = JDBCTools.getDataSource(JDBCTools.NUXEO_MANAGER_DATASOURCE_NAME);
        	DataSource nuxeoReaderDataSource = JDBCTools.getDataSource(JDBCTools.NUXEO_READER_DATASOURCE_NAME);
        	
        	if(nuxeoReaderDataSource!=null) {
	        	// We need to fetch the user name and password from the nuxeoDataSource, to do grants below
	        	org.apache.tomcat.dbcp.dbcp.BasicDataSource tomcatDataSource =
	        			(org.apache.tomcat.dbcp.dbcp.BasicDataSource)nuxeoReaderDataSource;
	        	// Get the template URL value from the JNDI datasource and substitute the databaseName
	        	String readerUser = tomcatDataSource.getUsername();
	        	DatabaseProductType dbType = JDBCTools.getDatabaseProductType(
	        			JDBCTools.CSPACE_DATASOURCE_NAME,
	        			JDBCTools.DEFAULT_CSPACE_DATABASE_NAME); // only returns PG or MYSQL
	
	    		conn = nuxeoMgrDataSource.getConnection();
	        	stmt = conn.createStatement();
	    		if(dbType==DatabaseProductType.POSTGRESQL) {
	    			// Note that select rights for reader must be granted after Nuxeo startup.
	    			String sql = "GRANT SELECT ON ALL TABLES IN SCHEMA public TO "+readerUser;
	    			stmt.executeUpdate(sql);
	    			if (logger.isDebugEnabled()) {
	    				logger.debug(" Granted SELECT rights on all public tables to reader: '"+readerUser+"'");
	    			}
	    		} else if(dbType==DatabaseProductType.MYSQL) {
	    		} else {
	    			throw new UnsupportedOperationException("handlePostNuxeoInitDBTasks only supports Postgres/MySQL.");
	    		}
        	}
    	} catch(Exception e) {
    		logger.error("handlePostNuxeoInitDBTasks failed on exception: " + e.getLocalizedMessage());
    		throw e;	// propagate
    	} finally {   //close resources
    		try {
    			if(stmt!=null) {
    				stmt.close();
    			}
    		} catch(SQLException se) {
    			// nothing we can do
    		}
    	}

    }
     */
    
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
    
    /**
     *  Populate a registry of URI templates by querying each resource
     *  for its own entries in the registry.
     * 
     *  These entries consist of one or more URI templates associated
     *  with that resource, for building URIs to access that resource.
     */
    private synchronized void populateUriTemplateRegistry() {
       if (uriTemplateRegistry.isEmpty()) {
            ResourceBase resource = null;
            ResourceMap resourceMap = ResteasyProviderFactory.getContextData(ResourceMap.class);
            for (Map.Entry<String, ResourceBase> entry : resourceMap.entrySet()) {
                resource = entry.getValue();
                Map<UriTemplateRegistryKey, StoredValuesUriTemplate> entries =
                        resource.getUriRegistryEntries();
                uriTemplateRegistry.putAll(entries);
            }

            // FIXME: Contacts itself should not have an entry in the URI template registry;
            // there should be a Contacts entry in that registry only for use in
            // building URIs for resources that have contacts as a sub-resource
            // (This may also fall out during implementation of CSPACE-2698.)
       }
    }

    public UriTemplateRegistry getUriTemplateRegistry() {
        if (uriTemplateRegistry.isEmpty()) {
            populateUriTemplateRegistry();
        }
        return uriTemplateRegistry;
    }


}

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
import javax.security.auth.login.LoginException;
import javax.servlet.ServletContext;
import javax.sql.DataSource;

import org.collectionspace.authentication.AuthN;
import org.collectionspace.services.common.config.ServicesConfigReaderImpl;
import org.collectionspace.services.common.config.TenantBindingConfigReaderImpl;
import org.collectionspace.services.common.init.IInitHandler;
import org.collectionspace.services.common.security.SecurityUtils;
import org.collectionspace.services.common.service.*;
import org.collectionspace.services.common.storage.JDBCTools;
import org.collectionspace.services.common.storage.DatabaseProductType;
import org.collectionspace.services.common.tenant.TenantBindingType;
import org.collectionspace.services.common.types.PropertyItemType;
import org.collectionspace.services.common.types.PropertyType;
import org.collectionspace.services.nuxeo.client.java.DocHandlerBase;
//import org.collectionspace.services.nuxeo.client.java.NuxeoConnector;
//import org.collectionspace.services.nuxeo.client.java.NxConnect;
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

    /**
     * volatile is used here to assume about ordering (post JDK 1.5)
     */
    private static volatile ServiceMain instance = null;
    final Logger logger = LoggerFactory.getLogger(ServiceMain.class);
    private NuxeoConnectorEmbedded nuxeoConnector;
    private static ServletContext servletContext;
    private String serverRootDir = null;
    private ServicesConfigReaderImpl servicesConfigReader;
    private TenantBindingConfigReaderImpl tenantBindingConfigReader;
    
    private static final String TENANT_ADMIN_ACCT_PREFIX = "admin@"; 
    private static final String TENANT_READER_ACCT_PREFIX = "reader@"; 
    private static final String ROLE_PREFIX = "ROLE_"; 
    private static final String SPRING_ADMIN_ROLE = "ROLE_SPRING_ADMIN"; 
    private static final String TENANT_ADMIN_ROLE_SUFFIX = "_TENANT_ADMINISTRATOR"; 
    private static final String TENANT_READER_ROLE_SUFFIX = "_TENANT_READER"; 
    private static final String DEFAULT_ADMIN_PASSWORD = "Administrator";
    private static final String DEFAULT_READER_PASSWORD = "reader";
    private static final String SERVER_HOME_PROPERTY = "catalina.home";
    
    private static DataSource cspaceDataSource = null;
    private static DataSource nuxeoDataSource = null;
    
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
    	if (logger.isDebugEnabled() == true) {
    		System.out.print("Pausing 1 seconds for you to attached the debugger");
    		long startTime, currentTime;
    		currentTime = startTime = System.currentTimeMillis();
    		long stopTime = startTime + 1 * 1000; //5 seconds
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
        try {
        	createDefaultAccounts();
        } catch(Exception e) {
        	logger.error("Default Account setup failed on exception: " + e.getLocalizedMessage());
        }
        //
        // Start up and initialize our embedded Nuxeo server instance
        //
        if (getClientType().equals(ClientType.JAVA)) {
            nuxeoConnector = NuxeoConnectorEmbedded.getInstance();
            nuxeoConnector.initialize(getServicesConfigReader().getConfiguration().getRepositoryClient(),
            		ServiceMain.servletContext);
        }

        try {
        	//
        	// Invoke all post-initialization handlers, passing in a DataSource instance of the Nuxeo db.
        	// Typically, these handlers modify column types and add indexes to the Nuxeo db schema.
        	//
            firePostInitHandlers(ServiceMain.nuxeoDataSource);
        } catch(Exception e) {
            logger.error("ServiceMain.initialize firePostInitHandlers failed on exception: " + e.getLocalizedMessage());
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
    
    /*
     * FIXME: REM - This method is way too big -over 300 lines!  We need to break it up into
     * smaller, discrete, sub-methods.
     */
    private void createDefaultAccounts() {
    	if (logger.isDebugEnabled()) {
    		logger.debug("ServiceMain.createDefaultAccounts starting...");
    	}
        Hashtable<String, TenantBindingType> tenantBindings =
        	tenantBindingConfigReader.getTenantBindings();
        Hashtable<String, String> tenantInfo = new Hashtable<String, String>();
        for (TenantBindingType tenantBinding : tenantBindings.values()) {
        	String tId = tenantBinding.getId();
        	String tName = tenantBinding.getName();
        	tenantInfo.put(tId, tName);
        	if (logger.isDebugEnabled()) {
        		logger.debug("createDefaultAccounts found configured tenant id: "+tId+" name: "+tName);
        	}
        }
        Connection conn = null;
        PreparedStatement pstmt = null;
    	Statement stmt = null;
        // TODO - need to put in tests for existence first.
        // We could just look for the accounts per tenant up front, and assume that
        // the rest is there if the accounts are.
        // Could add a sql script to remove these if need be - Spring only does roles, 
        // and we're not touching that, so we could safely toss the 
        // accounts, users, account-tenants, account-roles, and start over.
        try {
        	conn = getConnection();
        	// First find or create the tenants
        	String queryTenantSQL = 
        		"SELECT id,name FROM tenants";
        	stmt = conn.createStatement();
			ResultSet rs = stmt.executeQuery(queryTenantSQL);
	        ArrayList<String> existingTenants = new ArrayList<String>();
			while (rs.next()) {
				String tId = rs.getString("id");
				String tName = rs.getString("name");
				if(tenantInfo.containsKey(tId)) {
					existingTenants.add(tId);
					if(!tenantInfo.get(tId).equalsIgnoreCase(tName)) {
						logger.warn("Configured name for tenant: "
								+tId+" in repository: "+tName
								+" does not match config'd name: "+ tenantInfo.get(tId));
					}
				}
			}
			rs.close();

        	String insertTenantSQL = 
        		"INSERT INTO tenants (id,name,created_at) VALUES (?,?, now())";
        	pstmt = conn.prepareStatement(insertTenantSQL); // create a statement
        	for(String tId : tenantInfo.keySet()) {
        		if(existingTenants.contains(tId)) {
                	if (logger.isDebugEnabled()) {
                		logger.debug("createDefaultAccounts: tenant exists (skipping): "
                				+tenantInfo.get(tId));
                	}
        			continue;
        		}
        		pstmt.setString(1, tId);					// set id param
        		pstmt.setString(2, tenantInfo.get(tId));	// set name param
            	if (logger.isDebugEnabled()) {
            		logger.debug("createDefaultAccounts adding entry for tenant: "+tId);
            	}
        		pstmt.executeUpdate();
        	}
        	pstmt.close();
        	// Second find or create the users
        	String queryUserSQL = 
        		"SELECT username FROM users WHERE username LIKE '"
        			+TENANT_ADMIN_ACCT_PREFIX+"%' OR username LIKE '"
        			+TENANT_READER_ACCT_PREFIX+"%'";
			rs = stmt.executeQuery(queryUserSQL);
	        ArrayList<String> usersInRepo = new ArrayList<String>();
			while (rs.next()) {
				String uName = rs.getString("username");
				usersInRepo.add(uName);
			}
			rs.close();
        	String insertUserSQL = 
        		"INSERT INTO users (username,passwd, created_at)"
        		+" VALUES (?,?, now())";
        	pstmt = conn.prepareStatement(insertUserSQL); // create a statement
        	for(String tName : tenantInfo.values()) {
        		String adminAcctName = getDefaultAdminUserID(tName);
        		if(!usersInRepo.contains(adminAcctName)) {
	        		String secEncPasswd = SecurityUtils.createPasswordHash(
	        				adminAcctName, DEFAULT_ADMIN_PASSWORD);
	        		pstmt.setString(1, adminAcctName);	// set username param
	        		pstmt.setString(2, secEncPasswd);	// set passwd param
	            	if (logger.isDebugEnabled()) {
	            		logger.debug("createDefaultAccounts adding user: "
	            				+adminAcctName+" for tenant: "+tName);
	            	}
	        		pstmt.executeUpdate();
        		} else if (logger.isDebugEnabled()) {
            		logger.debug("createDefaultAccounts: user: "+adminAcctName
            						+" already exists - skipping.");
            	}


        		String readerAcctName =  getDefaultReaderUserID(tName);
        		if(!usersInRepo.contains(readerAcctName)) {
	        		String secEncPasswd = SecurityUtils.createPasswordHash(
	        				readerAcctName, DEFAULT_READER_PASSWORD);
	        		pstmt.setString(1, readerAcctName);	// set username param
	        		pstmt.setString(2, secEncPasswd);	// set passwd param
	            	if (logger.isDebugEnabled()) {
	            		logger.debug("createDefaultAccounts adding user: "
	            				+readerAcctName+" for tenant: "+tName);
	            	}
	        		pstmt.executeUpdate();
        		} else if (logger.isDebugEnabled()) {
            		logger.debug("createDefaultAccounts: user: "+readerAcctName
            						+" already exists - skipping.");
        		}
        	}
        	pstmt.close();
        	// Third, create the accounts. Assume that if the users were already there,
        	// then the accounts were as well
            String insertAccountSQL = 
            	"INSERT INTO accounts_common "
            	+ "(csid, email, userid, status, screen_name, metadata_protection, roles_protection, created_at) "
            	+ "VALUES (?,?,?,'ACTIVE',?, 'immutable', 'immutable', now())";
            Hashtable<String, String> tenantAdminAcctCSIDs = new Hashtable<String, String>();
            Hashtable<String, String> tenantReaderAcctCSIDs = new Hashtable<String, String>();
        	pstmt = conn.prepareStatement(insertAccountSQL); // create a statement
        	for(String tId : tenantInfo.keySet()) {
        		String tName = tenantInfo.get(tId);
            	String adminCSID = UUID.randomUUID().toString();
            	tenantAdminAcctCSIDs.put(tId, adminCSID);
        		String adminAcctName =  getDefaultAdminUserID(tName);
        		if(!usersInRepo.contains(adminAcctName)) {
	        		pstmt.setString(1, adminCSID);			// set csid param
	        		pstmt.setString(2, adminAcctName);	// set email param (bogus)
	        		pstmt.setString(3, adminAcctName);	// set userid param
	        		pstmt.setString(4, "Administrator");// set screen name param
	            	if (logger.isDebugEnabled()) {
	            		logger.debug("createDefaultAccounts adding account: "
	            				+adminAcctName+" for tenant: "+tName);
	            	}
	        		pstmt.executeUpdate();
        		} else if (logger.isDebugEnabled()) {
            		logger.debug("createDefaultAccounts: user: "+adminAcctName
            						+" already exists - skipping account generation.");
        		}

        		String readerCSID = UUID.randomUUID().toString();	
            	tenantReaderAcctCSIDs.put(tId, readerCSID);
        		String readerAcctName =  getDefaultReaderUserID(tName);
        		if(!usersInRepo.contains(readerAcctName)) {
        			pstmt.setString(1, readerCSID);		// set csid param
        			pstmt.setString(2, readerAcctName);	// set email param (bogus)
        			pstmt.setString(3, readerAcctName);	// set userid param
        			pstmt.setString(4, "Reader");		// set screen name param
        			if (logger.isDebugEnabled()) {
        				logger.debug("createDefaultAccounts adding account: "
        						+readerAcctName+" for tenant: "+tName);
        			}
        			pstmt.executeUpdate();
        		} else if (logger.isDebugEnabled()) {
            		logger.debug("createDefaultAccounts: user: "+readerAcctName
            						+" already exists - skipping account creation.");
        		}
        	}
        	pstmt.close();
        	// Fourth, bind accounts to tenants. Assume that if the users were already there,
        	// then the accounts were bound to tenants correctly
        	String insertAccountTenantSQL;
        	DatabaseProductType databaseProductType = JDBCTools.getDatabaseProductType();
        	if (databaseProductType == DatabaseProductType.MYSQL) {
        		insertAccountTenantSQL =
        			"INSERT INTO accounts_tenants (TENANTS_ACCOUNTSCOMMON_CSID,tenant_id) "
        			+ " VALUES(?, ?)";
        	} else if (databaseProductType == DatabaseProductType.POSTGRESQL) {
        		insertAccountTenantSQL =
        			"INSERT INTO accounts_tenants (HJID, TENANTS_ACCOUNTSCOMMON_CSID,tenant_id) "
        			+ " VALUES(nextval('hibernate_sequence'), ?, ?)";
        	} else {
        		throw new Exception("Unrecognized database system.");
        	}
        	pstmt = conn.prepareStatement(insertAccountTenantSQL); // create a statement
        	for(String tId : tenantInfo.keySet()) {
        		String tName = tenantInfo.get(tId);
        		if(!usersInRepo.contains(getDefaultAdminUserID(tName))) {
	        		String adminAcct = tenantAdminAcctCSIDs.get(tId);
	        		pstmt.setString(1, adminAcct);		// set acct CSID param
	        		pstmt.setString(2, tId);			// set tenant_id param
	            	if (logger.isDebugEnabled()) {
	            		logger.debug("createDefaultAccounts binding account id: "
	            				+adminAcct+" to tenant id: "+tId);
	            	}
	        		pstmt.executeUpdate();
        		}
        		if(!usersInRepo.contains(getDefaultReaderUserID(tName))) {
	        		String readerAcct = tenantReaderAcctCSIDs.get(tId);
	        		pstmt.setString(1, readerAcct);		// set acct CSID param
	        		pstmt.setString(2, tId);			// set tenant_id param
	            	if (logger.isDebugEnabled()) {
	            		logger.debug("createDefaultAccounts binding account id: "
	            				+readerAcct+" to tenant id: "+tId);
	            	}
	        		pstmt.executeUpdate();
        		}
        	}
        	pstmt.close();
        	// Fifth, fetch and save the default roles
			String springAdminRoleCSID = null;
        	String querySpringRole = 
        		"SELECT csid from roles WHERE rolename='"+SPRING_ADMIN_ROLE+"'";
			rs = stmt.executeQuery(querySpringRole);
    		if(rs.next()) {
    			springAdminRoleCSID = rs.getString(1);
            	if (logger.isDebugEnabled()) {
            		logger.debug("createDefaultAccounts found Spring Admin role: "
            				+springAdminRoleCSID);
            	}
    		} else {
                String insertSpringAdminRoleSQL =
                	"INSERT INTO roles (csid, rolename, displayName, rolegroup, created_at, tenant_id) "
                	+ "VALUES ('-1', 'ROLE_SPRING_ADMIN', 'SPRING_ADMIN', 'Spring Security Administrator', now(), '0')";
    			stmt.executeUpdate(insertSpringAdminRoleSQL);
    			springAdminRoleCSID = "-1";
            	if (logger.isDebugEnabled()) {
            		logger.debug("createDefaultAccounts CREATED Spring Admin role: "
            				+springAdminRoleCSID);
            	}
    		}
        	rs.close();
        	String getRoleCSIDSql =
        		"SELECT csid from roles WHERE tenant_id=? and rolename=?";
        	pstmt = conn.prepareStatement(getRoleCSIDSql); // create a statement
        	rs = null;
            Hashtable<String, String> tenantAdminRoleCSIDs = new Hashtable<String, String>();
            Hashtable<String, String> tenantReaderRoleCSIDs = new Hashtable<String, String>();
        	for(String tId : tenantInfo.keySet()) {
        		pstmt.setString(1, tId);						// set tenant_id param
        		pstmt.setString(2, getDefaultAdminRole(tId));	// set rolename param
        		rs = pstmt.executeQuery();
        		// extract data from the ResultSet
        		if(!rs.next()) {
        			throw new RuntimeException("Cannot find role: "+getDefaultAdminRole(tId)
        					+" for tenant id: "+tId+" in roles!");
        		}
    			String tenantAdminRoleCSID = rs.getString(1);
            	if (logger.isDebugEnabled()) {
            		logger.debug("createDefaultAccounts found role: "
            				+getDefaultAdminRole(tId)+"("+tenantAdminRoleCSID
            				+") for tenant id: "+tId);
            	}
    			tenantAdminRoleCSIDs.put(tId, tenantAdminRoleCSID);
        		pstmt.setString(1, tId);						// set tenant_id param
        		pstmt.setString(2, getDefaultReaderRole(tId));	// set rolename param
        		rs.close();
        		rs = pstmt.executeQuery();
        		// extract data from the ResultSet
        		if(!rs.next()) {
        			throw new RuntimeException("Cannot find role: "+getDefaultReaderRole(tId)
        					+" for tenant id: "+tId+" in roles!");
        		}
    			String tenantReaderRoleCSID = rs.getString(1);
            	if (logger.isDebugEnabled()) {
            		logger.debug("createDefaultAccounts found role: "
            				+getDefaultReaderRole(tId)+"("+tenantReaderRoleCSID
            				+") for tenant id: "+tId);
            	}
    			tenantReaderRoleCSIDs.put(tId, tenantReaderRoleCSID);
        		rs.close();
        	}
        	pstmt.close();
        	// Sixth, bind the accounts to roles. If the users already existed,
        	// we'll assume they were set up correctly.
					String insertAccountRoleSQL;
					if (databaseProductType == DatabaseProductType.MYSQL) {
						insertAccountRoleSQL =
						"INSERT INTO accounts_roles(account_id, user_id, role_id, role_name, created_at)"
							+" VALUES(?, ?, ?, ?, now())";
					} else if (databaseProductType == DatabaseProductType.POSTGRESQL) {
						insertAccountRoleSQL =
						"INSERT INTO accounts_roles(HJID, account_id, user_id, role_id, role_name, created_at)"
							+" VALUES(nextval('hibernate_sequence'), ?, ?, ?, ?, now())";
					} else {
							throw new Exception("Unrecognized database system.");
					}
        	if (logger.isDebugEnabled()) {
        		logger.debug("createDefaultAccounts binding accounts to roles with SQL:\n"
        				+insertAccountRoleSQL);
        	}
        	pstmt = conn.prepareStatement(insertAccountRoleSQL); // create a statement
        	for(String tId : tenantInfo.keySet()) {
        		String adminUserId =  getDefaultAdminUserID(tenantInfo.get(tId));
        		if(!usersInRepo.contains(adminUserId)) {
            		String adminAcct = tenantAdminAcctCSIDs.get(tId);
	        		String adminRoleId = tenantAdminRoleCSIDs.get(tId);
	        		pstmt.setString(1, adminAcct);		// set acct CSID param
	        		pstmt.setString(2, adminUserId);	// set user_id param
	        		pstmt.setString(3, adminRoleId);	// set role_id param
	        		pstmt.setString(4, getDefaultAdminRole(tId));	// set rolename param
	            	if (logger.isDebugEnabled()) {
	            		logger.debug("createDefaultAccounts binding account: "
	            				+adminUserId+" to Admin role("+adminRoleId
	            				+") for tenant id: "+tId);
	            	}
	        		pstmt.executeUpdate();
	        		// Now add the Spring Admin Role to the admin accounts
	        		pstmt.setString(3, springAdminRoleCSID);	// set role_id param
	        		pstmt.setString(4, SPRING_ADMIN_ROLE);		// set rolename param
	            	if (logger.isDebugEnabled()) {
	            		logger.debug("createDefaultAccounts binding account: "
	            				+adminUserId+" to Spring Admin role: "+springAdminRoleCSID);
	            	}
	        		pstmt.executeUpdate();
        		}
        		String readerUserId = getDefaultReaderUserID(tenantInfo.get(tId));
        		if(!usersInRepo.contains(readerUserId)) {
	        		String readerAcct = tenantReaderAcctCSIDs.get(tId);
	        		String readerRoleId = tenantReaderRoleCSIDs.get(tId);
	        		pstmt.setString(1, readerAcct);		// set acct CSID param
	        		pstmt.setString(2, readerUserId);	// set user_id param
	        		pstmt.setString(3, readerRoleId);	// set role_id param
	        		pstmt.setString(4, getDefaultReaderRole(tId));	// set rolename param
	            	if (logger.isDebugEnabled()) {
	            		logger.debug("createDefaultAccounts binding account: "
	            				+readerUserId+" to Reader role("+readerRoleId
	            				+") for tenant id: "+tId);
	            	}
	        		pstmt.executeUpdate();
        		}
        	}
        	pstmt.close();
			stmt.close();
        } catch (RuntimeException rte) {
        	if (logger.isDebugEnabled()) {
        		logger.debug("Exception in createDefaultAccounts: "+
						rte.getLocalizedMessage());
        		logger.debug(rte.getStackTrace().toString());
        	}
            throw rte;
        } catch (SQLException sqle) {
            // SQLExceptions can be chained. We have at least one exception, so
            // set up a loop to make sure we let the user know about all of them
            // if there happens to be more than one.
        	if (logger.isDebugEnabled()) {
        		SQLException tempException = sqle;
        		while (null != tempException) {
        			logger.debug("SQL Exception: " + sqle.getLocalizedMessage());
        			tempException = tempException.getNextException();
        		}
        		logger.debug(sqle.getStackTrace().toString());
        	}
            throw new RuntimeException("SQL problem in createDefaultAccounts: ", sqle);
        } catch (Exception e) {
        	if (logger.isDebugEnabled()) {
        		logger.debug("Exception in createDefaultAccounts: "+
						e.getLocalizedMessage());
        	}
        } finally {
        	try {
            	if(conn!=null)
                    conn.close();
            	if(pstmt!=null)
                    pstmt.close();
            	if(stmt!=null)
                    stmt.close();
            } catch (SQLException sqle) {
            	if (logger.isDebugEnabled()) {
        			logger.debug("SQL Exception closing statement/connection: "
        					+ sqle.getLocalizedMessage());
            	}
        	}
        }    	
    }
    
    private String getDefaultAdminRole(String tenantId) {
    	return ROLE_PREFIX+tenantId+TENANT_ADMIN_ROLE_SUFFIX;
    }
    
    private String getDefaultReaderRole(String tenantId) {
    	return ROLE_PREFIX+tenantId+TENANT_READER_ROLE_SUFFIX;
    }
    
    private String getDefaultAdminUserID(String tenantName) {
    	return TENANT_ADMIN_ACCT_PREFIX+tenantName;
    }
    
    private String getDefaultReaderUserID(String tenantName) {
    	return TENANT_READER_ACCT_PREFIX+tenantName;
    }

    private void firePostInitHandlers(DataSource dataSource) throws Exception {
        Hashtable<String, TenantBindingType> tenantBindingTypeMap = tenantBindingConfigReader.getTenantBindings();
        //Loop through all tenants in tenant-bindings.xml
        for (TenantBindingType tbt: tenantBindingTypeMap.values()){
            //String name = tbt.getName();
            //String id = tbt.getId();
            //Loop through all the services in this tenant
            List<ServiceBindingType> sbtList = tbt.getServiceBindings();
            for (ServiceBindingType sbt: sbtList){
                //Get the list of InitHandler elements, extract the first one (only one supported right now) and fire it using reflection.
                List<org.collectionspace.services.common.service.InitHandler> list = sbt.getInitHandler();
                if (list!=null && list.size()>0){
                    org.collectionspace.services.common.service.InitHandler handlerType = list.get(0);
                    String initHandlerClassname = handlerType.getClassname();

                    List<org.collectionspace.services.common.service.InitHandler.Params.Field>
                            fields = handlerType.getParams().getField();

                    List<org.collectionspace.services.common.service.InitHandler.Params.Property>
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


    public Object instantiate(String clazz, Class castTo) throws Exception {
        ClassLoader tccl = Thread.currentThread().getContextClassLoader();
        clazz = clazz.trim();
        Class<?> c = tccl.loadClass(clazz);
        if (castTo.isAssignableFrom(c)) {
            return c.newInstance();
        }
        return null;
    }

    private Connection getConnection() throws LoginException, SQLException {
        return JDBCTools.getConnection(JDBCTools.CSPACE_REPOSITORY_NAME);
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
    	ServiceMain.cspaceDataSource = JDBCTools.getDataSource(JDBCTools.CSPACE_REPOSITORY_NAME);
    	ServiceMain.nuxeoDataSource = JDBCTools.getDataSource(JDBCTools.NUXEO_REPOSITORY_NAME);
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

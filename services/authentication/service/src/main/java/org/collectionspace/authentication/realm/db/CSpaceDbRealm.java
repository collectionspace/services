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
 *//**
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
/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.collectionspace.authentication.realm.db;

import java.lang.reflect.Constructor;
import java.net.ConnectException;
import java.security.Principal;
import java.security.acl.Group;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.security.auth.login.FailedLoginException;
import javax.security.auth.login.LoginException;
import javax.sql.DataSource;

//import org.apache.commons.logging.Log;
//import org.apache.commons.logging.LogFactory;





import org.collectionspace.authentication.AuthN;
import org.collectionspace.authentication.CSpaceTenant;
import org.collectionspace.authentication.realm.CSpaceRealm;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * CSpaceDbRealm provides access to user, password, role, tenant database
 * @author 
 */
public class CSpaceDbRealm implements CSpaceRealm {
	private Logger logger = LoggerFactory.getLogger(CSpaceDbRealm.class);
    
    private String datasourceName;
    private String principalsQuery;
    private String rolesQuery;
    private String tenantsQueryNoDisabled;
    private String tenantsQueryWithDisabled;
    private boolean suspendResume;

	private long maxRetrySeconds = MAX_RETRY_SECONDS;
	private static final int MAX_RETRY_SECONDS = 5;
    private static final String MAX_RETRY_SECONDS_STR = "maxRetrySeconds";

	private long delayBetweenAttemptsMillis = DELAY_BETWEEN_ATTEMPTS_MILLISECONDS;
    private static final String DELAY_BETWEEN_ATTEMPTS_MILLISECONDS_STR = "delayBetweenAttemptsMillis";
	private static final long DELAY_BETWEEN_ATTEMPTS_MILLISECONDS = 200;
	
	protected void setMaxRetrySeconds(Map options) {
		Object optionsObj = options.get(MAX_RETRY_SECONDS_STR);
		if (optionsObj != null) {
			String paramValue = optionsObj.toString();
			try {
				maxRetrySeconds = Long.parseLong(paramValue);
			} catch (NumberFormatException e) {
				logger.warn(String.format("The Spring Security login authentication parameter '%s' with value '%s' could not be parsed to a long value.  The default value of '%d' will be used instead.",
						MAX_RETRY_SECONDS_STR, paramValue, maxRetrySeconds));
			}
		}
	}
	
	protected long getMaxRetrySeconds() {
		return this.maxRetrySeconds;
	}
	
	protected void setDelayBetweenAttemptsMillis(Map options) {
		Object optionsObj = options.get(DELAY_BETWEEN_ATTEMPTS_MILLISECONDS_STR);
		if (optionsObj != null) {
			String paramValue = optionsObj.toString();
			try {
				delayBetweenAttemptsMillis = Long.parseLong(paramValue);
			} catch (NumberFormatException e) {
				logger.warn(String.format("The Spring Security login authentication parameter '%s' with value '%s' could not be parsed to a long value.  The default value of '%d' will be used instead.",
						MAX_RETRY_SECONDS_STR, paramValue, delayBetweenAttemptsMillis));
			}
		}
	}
	
	protected long getDelayBetweenAttemptsMillis() {
		return this.delayBetweenAttemptsMillis;
	}
    
    /**
     * CSpace Database Realm
     * @param datasourceName datasource name
     */
    public CSpaceDbRealm(Map options) {
        datasourceName = (String) options.get("dsJndiName");
        if (datasourceName == null) {
            datasourceName = "java:/DefaultDS";
        }
        Object tmp = options.get("principalsQuery");
        if (tmp != null) {
            principalsQuery = tmp.toString();
        }
        tmp = options.get("rolesQuery");
        if (tmp != null) {
            rolesQuery = tmp.toString();
        }
        tmp = options.get("tenantsQueryNoDisabled");
        if (tmp != null) {
            tenantsQueryNoDisabled = tmp.toString();
        }
        tmp = options.get("tenantsQueryWithDisabled");
        if (tmp != null) {
        	tenantsQueryWithDisabled = tmp.toString();
        }
        tmp = options.get("suspendResume");
        if (tmp != null) {
            suspendResume = Boolean.valueOf(tmp.toString()).booleanValue();
        }
        
        this.setMaxRetrySeconds(options);
        this.setDelayBetweenAttemptsMillis(options);
        
        if (logger.isTraceEnabled()) {
            logger.trace("DatabaseServerLoginModule, dsJndiName=" + datasourceName);
            logger.trace("principalsQuery=" + principalsQuery);
            logger.trace("rolesQuery=" + rolesQuery);
            logger.trace("suspendResume=" + suspendResume);
        }

    }

    @Override
    public String getUsersPassword(String username) throws LoginException {

        String password = null;
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = getConnection();
            // Get the password
            if (logger.isDebugEnabled()) {
                logger.debug("Executing query: " + principalsQuery + ", with username: " + username);
            }
            ps = conn.prepareStatement(principalsQuery);
            ps.setString(1, username);
            rs = ps.executeQuery();
            if (rs.next() == false) {
                if (logger.isDebugEnabled()) {
                    logger.debug(principalsQuery + " returned no matches from db");
                }
                throw new FailedLoginException("No matching username found");
            }

            password = rs.getString(1);
        } catch (SQLException ex) {
        	if (logger.isTraceEnabled() == true) {
        		logger.error("Could not open database to read AuthN tables.", ex);
        	}
            LoginException le = new LoginException("Authentication query failed: " + ex.getLocalizedMessage());
            le.initCause(ex);
            throw le;
        } catch (Exception ex) {
            LoginException le = new LoginException("Unknown Exception");
            le.initCause(ex);
            throw le;
        } finally {
            if (rs != null) {
                try {
                    rs.close();
                } catch (SQLException e) {
                }
            }
            if (ps != null) {
                try {
                    ps.close();
                } catch (SQLException e) {
                }
            }
            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException ex) {
                }
            }
        }
        return password;
    }

    /**
     * Execute the rolesQuery against the datasourceName to obtain the roles for
     * the authenticated user.
     * @return collection containing the roles
     */
    @Override
    public Collection<Group> getRoles(String username, String principalClassName, String groupClassName) throws LoginException {

        if (logger.isDebugEnabled()) {
            logger.debug("getRoleSets using rolesQuery: " + rolesQuery + ", username: " + username);
        }

        Connection conn = null;
        HashMap<String, Group> groupsMap = new HashMap<String, Group>();
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            conn = getConnection();
            // Get the user role names
            if (logger.isDebugEnabled()) {
                logger.debug("Executing query: " + rolesQuery + ", with username: " + username);
            }

            ps = conn.prepareStatement(rolesQuery);
            try {
                ps.setString(1, username);
            } catch (ArrayIndexOutOfBoundsException ignore) {
                // The query may not have any parameters so just try it
            }
            rs = ps.executeQuery();
            if (rs.next() == false) {
                if (logger.isDebugEnabled()) {
                    logger.debug("No roles found");
                }
//                if(aslm.getUnauthenticatedIdentity() == null){
//                    throw new FailedLoginException("No matching username found in Roles");
//                }
                /* We are running with an unauthenticatedIdentity so create an
                empty Roles set and return.
                 */

                Group g = createGroup(groupClassName, "Roles");
                groupsMap.put(g.getName(), g);
                return groupsMap.values();
            }

            do {
                String roleName = rs.getString(1);
                String groupName = rs.getString(2);
                if (groupName == null || groupName.length() == 0) {
                    groupName = "Roles";
                }

                Group group = (Group) groupsMap.get(groupName);
                if (group == null) {
                    group = createGroup(groupClassName, groupName);
                    groupsMap.put(groupName, group);
                }

                try {
                    Principal p = createPrincipal(principalClassName, roleName);
                    if (logger.isDebugEnabled()) {
                        logger.debug("Assign user to role " + roleName);
                    }

                    group.addMember(p);
                } catch (Exception e) {
                    logger.error("Failed to create principal: " + roleName + " " + e.toString());
                }

            } while (rs.next());
        } catch (SQLException ex) {
            LoginException le = new LoginException("Query failed");
            le.initCause(ex);
            throw le;
        } catch (Exception e) {
            LoginException le = new LoginException("unknown exception");
            le.initCause(e);
            throw le;
        } finally {
            if (rs != null) {
                try {
                    rs.close();
                } catch (SQLException e) {
                }
            }
            if (ps != null) {
                try {
                    ps.close();
                } catch (SQLException e) {
                }
            }
            if (conn != null) {
                try {
                    conn.close();
                } catch (Exception ex) {
                }
            }

        }

        return groupsMap.values();

    }
    @Override
    public Collection<Group> getTenants(String username, String groupClassName) throws LoginException {
    	return getTenants(username, groupClassName, false);
    }
    
    private boolean userIsTenantManager(Connection conn, String username) {
    	String acctQuery = "SELECT csid FROM accounts_common WHERE userid=?";
        PreparedStatement ps = null;
        ResultSet rs = null;
        boolean accountIsTenantManager = false;
        try {
            ps = conn.prepareStatement(acctQuery);
            ps.setString(1, username);
            rs = ps.executeQuery();
            if (rs.next()) {
                String acctCSID = rs.getString(1);
                if(AuthN.TENANT_MANAGER_ACCT_ID.equals(acctCSID)) {
                	accountIsTenantManager = true;
                }
            }
        } catch (SQLException ex) {
            if(logger.isDebugEnabled()) {
            	logger.debug("userIsTenantManager query failed on SQL error: " + ex.getLocalizedMessage());
            }
        } catch (Exception e) {
            if(logger.isDebugEnabled()) {
            	logger.debug("userIsTenantManager unknown error: " + e.getLocalizedMessage());
            }
        } finally {
            if (rs != null) {
                try {
                    rs.close();
                } catch (SQLException e) {
                }
            }
            if (ps != null) {
                try {
                    ps.close();
                } catch (SQLException e) {
                }
            }
        }
        return accountIsTenantManager;
    }
    
    /**
     * Execute the tenantsQuery against the datasourceName to obtain the tenants for
     * the authenticated user.
     * @return collection containing the roles
     */
    @Override
    public Collection<Group> getTenants(String username, String groupClassName, boolean includeDisabledTenants) throws LoginException {

    	String tenantsQuery = getTenantQuery(includeDisabledTenants);
    	
        if (logger.isDebugEnabled()) {
            logger.debug("getTenants using tenantsQuery: " + tenantsQuery + ", username: " + username);
        }

        Connection conn = null;
        HashMap<String, Group> groupsMap = new HashMap<String, Group>();
        PreparedStatement ps = null;
        ResultSet rs = null;
    	final String defaultGroupName = "Tenants";

        try {
            conn = getConnection();

            ps = conn.prepareStatement(tenantsQuery);
            try {
                ps.setString(1, username);
            } catch (ArrayIndexOutOfBoundsException ignore) {
                // The query may not have any parameters so just try it
            }
            rs = ps.executeQuery();
            if (rs.next() == false) {
        		Group group = (Group) groupsMap.get(defaultGroupName);
        		if (group == null) {
        			group = createGroup(groupClassName, defaultGroupName);
        			groupsMap.put(defaultGroupName, group);
        		}
            	// Check for the tenantManager
            	if(userIsTenantManager(conn, username)) {
            		if (logger.isDebugEnabled()) {
            			logger.debug("GetTenants called with tenantManager - synthesizing the pseudo-tenant");
            		}
            		try {
            			Principal p = createTenant("PseudoTenant", AuthN.TENANT_MANAGER_ACCT_ID);
            			if (logger.isDebugEnabled()) {
            				logger.debug("Assign tenantManager to tenant " + AuthN.TENANT_MANAGER_ACCT_ID);
            			}
            			group.addMember(p);
            		} catch (Exception e) {
            			logger.error("Failed to create pseudo-tenant: " + e.toString());
            		}
            	} else {
            		if (logger.isDebugEnabled()) {
            			logger.debug("No tenants found");
            		}
            		// We are running with an unauthenticatedIdentity so return an
            		// empty Tenants set.
            		// FIXME  should this be allowed?
            	}
        		return groupsMap.values();
            }

            do {
                String tenantId = rs.getString(1);
                String tenantName = rs.getString(2);
                String groupName = rs.getString(3);
                if (groupName == null || groupName.length() == 0) {
                    groupName = defaultGroupName;
                }

                Group group = (Group) groupsMap.get(groupName);
                if (group == null) {
                    group = createGroup(groupClassName, groupName);
                    groupsMap.put(groupName, group);
                }

                try {
                    Principal p = createTenant(tenantName, tenantId);
                    if (logger.isDebugEnabled()) {
                        logger.debug("Assign user to tenant " + tenantName);
                    }

                    group.addMember(p);
                } catch (Exception e) {
                    logger.error("Failed to create tenant: " + tenantName + " " + e.toString());
                }
            } while (rs.next());
        } catch (SQLException ex) {
            LoginException le = new LoginException("Query failed");
            le.initCause(ex);
            throw le;
        } catch (Exception e) {
            LoginException le = new LoginException("unknown exception");
            le.initCause(e);
            throw le;
        } finally {
            if (rs != null) {
                try {
                    rs.close();
                } catch (SQLException e) {
                }
            }
            if (ps != null) {
                try {
                    ps.close();
                } catch (SQLException e) {
                }
            }
            if (conn != null) {
                try {
                    conn.close();
                } catch (Exception ex) {
                }
            }

        }

        return groupsMap.values();
    }

    private CSpaceTenant createTenant(String name, String id) throws Exception {
        return new CSpaceTenant(name, id);
    }

    private Group createGroup(String groupClassName, String name) throws Exception {
        return (Group) createPrincipal(groupClassName, name);
    }

    private Principal createPrincipal(String principalClassName, String name) throws Exception {
        ClassLoader loader = Thread.currentThread().getContextClassLoader();
        Class clazz = loader.loadClass(principalClassName);
        Class[] ctorSig = {String.class};
        Constructor ctor = clazz.getConstructor(ctorSig);
        Object[] ctorArgs = {name};
        Principal p = (Principal) ctor.newInstance(ctorArgs);
        return p;
    }

    /*
     * This method will attempt to get a connection.  If a network error prevents it from getting a connection on the first try
     * it will retry for the next 'getMaxRetrySeconds()' seconds.  If it is unable to get the connection then it will timeout and
     * throw an exception.
     */
    private Connection getConnection() throws Exception {
        Connection result = null;
		boolean failed = true;
		Exception lastException = null;
		int requestAttempts = 0;

		long quittingTime = System.currentTimeMillis() + getMaxRetrySeconds() * 1000; // This is how long we attempt retries
		do {
			if (requestAttempts > 0) {
				Thread.sleep(getDelayBetweenAttemptsMillis()); // Wait a little time between reattempts.
			}
			
			try {
				// proceed to the original request by calling doFilter()
				result = this.getConnection(getDataSourceName());
				if (result != null) {
					failed = false;
					break; // the request was successfully executed, so we can break out of this retry loop
				} else {
					failed = true;
					throw new ConnectException(); // The 'response' argument indicated a network related failure, so let's throw a generic connection exception
				}
			} catch (Exception e) {
				lastException = e;
				if (exceptionChainContainsNetworkError(lastException) == false) {
					// Break if the exception chain does not contain a
					// network related exception because we don't want to retry if it's not a network related failure
					break;
				}
				requestAttempts++; // keep track of how many times we've tried the request
			}
		} while (System.currentTimeMillis() < quittingTime);  // keep trying until we run out of time
		
		//
		// Add a warning to the logs if we encountered *any* failures on our re-attempts.  Only add the warning
		// if we were eventually successful.
		//
		if (requestAttempts > 0 && failed == false) {
			logger.warn(String.format("Request to get a connection from data source '%s' failed with exception '%s' at attempt number '%d' before finally succeeding on the next attempt.",
					getDataSourceName(),
					lastException.getClass().getName(),
					requestAttempts));
		}

		if (failed == true) {
			// If we get here, it means all of our attempts to get a successful call to chain.doFilter() have failed.
			throw lastException;
		}
		
		return result;
	}
    
	/*
	 * Don't call this method directly.  Instead, use the getConnection() method that take no arguments.
	 */
	private Connection getConnection(String dataSourceName) throws LoginException, SQLException {
        InitialContext ctx = null;
        Connection conn = null;
        DataSource ds = null;
        
        try {
            ctx = new InitialContext();
            try {
            	ds = (DataSource) ctx.lookup(dataSourceName);
            } catch (Exception e) {}
            
	        try {
	        	Context envCtx = (Context) ctx.lookup("java:comp/env");
	        	ds = (DataSource) envCtx.lookup(dataSourceName);
	        } catch (Exception e) {}
	        
	        try {
	        	Context envCtx = (Context) ctx.lookup("java:comp");
	        	ds = (DataSource) envCtx.lookup(dataSourceName);
	        } catch (Exception e) {}
	        
	        try {
	        	Context envCtx = (Context) ctx.lookup("java:");
	        	ds = (DataSource) envCtx.lookup(dataSourceName);
	        } catch (Exception e) {}
	        
	        try {
	        	Context envCtx = (Context) ctx.lookup("java");
	        	ds = (DataSource) envCtx.lookup(dataSourceName);
	        } catch (Exception e) {}
	        
	        try {
	        	ds = (DataSource) ctx.lookup("java:/" + dataSourceName);
	        } catch (Exception e) {}  

	        if (ds == null) {
            	ds = AuthN.getDataSource();
	        }
	        
            if (ds == null) {
                throw new IllegalArgumentException("datasource not found: " + dataSourceName);
            }
            
            conn = ds.getConnection();
            if (conn == null) {
            	conn = AuthN.getDataSource().getConnection();  //FIXME:REM - This is the result of some type of JNDI mess.  Should try to solve this problem and clean up this code.
            }
            
            return conn;
            
        } catch (NamingException ex) {
            LoginException le = new LoginException("Error looking up DataSource from: " + dataSourceName);
            le.initCause(ex);
            throw le;
        } finally {
            if (ctx != null) {
                try {
                    ctx.close();
                } catch (Exception e) {
                	e.printStackTrace();  // We should be using a logger here instead.
                }
            }
        }

    }

    /**
     * @return the datasourceName
     */
    public String getDataSourceName() {
        return datasourceName;
    }

    /**
     * @return the principalQuery
     */
    public String getPrincipalQuery() {
        return principalsQuery;
    }

    /**
     * @param principalQuery the principalQuery to set
     */
    public void setPrincipalQuery(String principalQuery) {
        this.principalsQuery = principalQuery;
    }

    /**
     * @return the roleQuery
     */
    public String getRoleQuery() {
        return rolesQuery;
    }

    /**
     * @param roleQuery the roleQuery to set
     */
    public void setRoleQuery(String roleQuery) {
        this.rolesQuery = roleQuery;
    }

    /**
     * @return the tenantQuery
     */
    public String getTenantQuery(boolean includeDisabledTenants) {
        return includeDisabledTenants?tenantsQueryWithDisabled:tenantsQueryNoDisabled;
    }

    /**
     * @param tenantQuery the tenantQuery to set
    public void setTenantQuery(String tenantQuery) {
        this.tenantsQueryNoDisabled = tenantQuery;
    }
     */
    
    /*
     * This method crawls the exception chain looking for network related exceptions and
     * returns 'true' if it finds one.
     */
	public static boolean exceptionChainContainsNetworkError(Throwable exceptionChain) {
		boolean result = false;
		Throwable cause = exceptionChain;

		while (cause != null) {
			if (isExceptionNetworkRelated(cause) == true) {
				result = true;
				break;
			}
			
			cause = cause.getCause();
		}

		return result;
	}
	
	/*
	 * Return 'true' if the exception is in the "java.net" package.
	 */
	private static boolean isExceptionNetworkRelated(Throwable cause) {
		boolean result = false;

		String className = cause.getClass().getCanonicalName();
		if (className.contains("java.net") == true) {
			result = true;
		}

		return result;
	}
    
}

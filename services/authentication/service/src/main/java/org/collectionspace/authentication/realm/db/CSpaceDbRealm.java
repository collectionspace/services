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

import java.net.ConnectException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.security.auth.login.AccountException;
import javax.security.auth.login.AccountNotFoundException;
import javax.sql.DataSource;

import org.collectionspace.authentication.AuthN;
import org.collectionspace.authentication.CSpaceTenant;
import org.collectionspace.authentication.realm.CSpaceRealm;
import org.postgresql.util.PSQLState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * CSpaceDbRealm provides access to user, password, role, tenant database
 * @author 
 */
public class CSpaceDbRealm implements CSpaceRealm {
	public static String DEFAULT_DATASOURCE_NAME = "CspaceDS";
	
    private Logger logger = LoggerFactory.getLogger(CSpaceDbRealm.class);
    
    private String datasourceName;
    private String principalsQuery;
    private String saltQuery;
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
	
	protected void setMaxRetrySeconds(Map<String, ?> options) {
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
	
	protected void setDelayBetweenAttemptsMillis(Map<String, ?> options) {
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
	
	public CSpaceDbRealm() {
        datasourceName = DEFAULT_DATASOURCE_NAME;
	}
    
    /**
     * CSpace Database Realm
     * @param datasourceName datasource name
     */
    public CSpaceDbRealm(Map<String, ?> options) {
        datasourceName = (String) options.get("dsJndiName");
        if (datasourceName == null) {
            datasourceName = DEFAULT_DATASOURCE_NAME;
        }
        Object tmp = options.get("principalsQuery");
        if (tmp != null) {
            principalsQuery = tmp.toString();
        }
        tmp = options.get("saltQuery");
        if (tmp != null) {
        	saltQuery = tmp.toString();
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
    public String getPassword(String username) throws AccountException {

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
                throw new AccountNotFoundException("No matching username found");
            }

            password = rs.getString(1);
        } catch (SQLException ex) {
            if (logger.isTraceEnabled() == true) {
                logger.error("Could not open database to read AuthN tables.", ex);
            }
            AccountException ae = new AccountException("Authentication query failed: " + ex.getLocalizedMessage());
            ae.initCause(ex);
            throw ae;
        } catch (AccountNotFoundException ex) {
            throw ex;
        } catch (Exception ex) {
            AccountException ae = new AccountException("Unknown Exception");
            ae.initCause(ex);
            throw ae;
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

    @Override
    public Set<String> getRoles(String username) throws AccountException {
        if (logger.isDebugEnabled()) {
            logger.debug("getRoleSets using rolesQuery: " + rolesQuery + ", username: " + username);
        }

        Set<String> roles = new LinkedHashSet<String>();

        Connection conn = null;
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
                
                return roles;
            }

            do {
                String roleName = rs.getString(1);
                roles.add(roleName);
                
            } while (rs.next());
        } catch (SQLException ex) {
            AccountException ae = new AccountException("Query failed");
            ae.initCause(ex);
            throw ae;
        } catch (Exception e) {
            AccountException ae = new AccountException("unknown exception");
            ae.initCause(e);
            throw ae;
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

        return roles;

    }
    @Override
    public Set<CSpaceTenant> getTenants(String username) throws AccountException {
        return getTenants(username, false);
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
     * @return set containing the roles
     */
    @Override
    public Set<CSpaceTenant> getTenants(String username, boolean includeDisabledTenants) throws AccountException {

    	String tenantsQuery = getTenantQuery(includeDisabledTenants);
    	
        if (logger.isDebugEnabled()) {
            logger.debug("getTenants using tenantsQuery: " + tenantsQuery + ", username: " + username);
        }

        Set<CSpaceTenant> tenants = new LinkedHashSet<CSpaceTenant>();
        
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

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
                // Check for the tenantManager
                if(userIsTenantManager(conn, username)) {
                    if (logger.isDebugEnabled()) {
                        logger.debug("GetTenants called with tenantManager - synthesizing the pseudo-tenant");
                    }
                    
                    tenants.add(new CSpaceTenant(AuthN.TENANT_MANAGER_ACCT_ID, "PseudoTenant"));
                } else {
                    if (logger.isDebugEnabled()) {
                        logger.debug("No tenants found");
                    }
                    // We are running with an unauthenticatedIdentity so return an
                    // empty Tenants set.
                    // FIXME  should this be allowed?
                }
                
                return tenants;
            }

            do {
                String tenantId = rs.getString(1);
                String tenantName = rs.getString(2);

                tenants.add(new CSpaceTenant(tenantId, tenantName));
            } while (rs.next());
        } catch (SQLException ex) {
            AccountException ae = new AccountException("Query failed");
            ae.initCause(ex);
            throw ae;
        } catch (Exception e) {
            AccountException ae = new AccountException("unknown exception");
            ae.initCause(e);
            throw ae;
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

        return tenants;
    }

    /*
     * This method will attempt to get a connection.  If a network error prevents it from getting a connection on the first try
     * it will retry for the next 'getMaxRetrySeconds()' seconds.  If it is unable to get the connection then it will timeout and
     * throw an exception.
     */
    public Connection getConnection() throws Exception {
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
	private Connection getConnection(String dataSourceName) throws AccountException, SQLException {
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
            AccountException ae = new AccountException("Error looking up DataSource from: " + dataSourceName);
            ae.initCause(ex);
            throw ae;
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

	@Override
	public String getSalt(String username) throws AccountException {
        String salt = null;
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = getConnection();
            // Get the salt
            if (logger.isDebugEnabled()) {
                logger.debug("Executing query: " + saltQuery + ", with username: " + username);
            }
            ps = conn.prepareStatement(saltQuery);
            ps.setString(1, username);
            rs = ps.executeQuery();
            if (rs.next() == false) {
                if (logger.isDebugEnabled()) {
                    logger.debug(saltQuery + " returned no matches from db");
                }
                throw new AccountNotFoundException("No matching username found");
            }

            salt = rs.getString(1);
        } catch (SQLException ex) {
        	// Assuming PostgreSQL
            if (PSQLState.UNDEFINED_COLUMN.getState().equals(ex.getSQLState())) {
            	String msg = "'USERS' table is missing 'salt' column for password encyrption.  Assuming existing passwords are unsalted.";
            	logger.warn(msg);
            } else {
                AccountException ae = new AccountException("Authentication query failed: " + ex.getLocalizedMessage());
                ae.initCause(ex);
                throw ae;
            }
        } catch (AccountNotFoundException ex) {
            throw ex;
        } catch (Exception ex) {
            AccountException ae = new AccountException("Unknown Exception");
            ae.initCause(ex);
            throw ae;
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
        
        return salt;
    }
    
}

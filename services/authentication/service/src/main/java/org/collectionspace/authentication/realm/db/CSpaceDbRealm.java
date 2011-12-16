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
    private String tenantsQuery;
    private boolean suspendResume;

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
        tmp = options.get("tenantsQuery");
        if (tmp != null) {
            tenantsQuery = tmp.toString();
        }
        tmp = options.get("suspendResume");
        if (tmp != null) {
            suspendResume = Boolean.valueOf(tmp.toString()).booleanValue();
        }
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

    /**
     * Execute the tenantsQuery against the datasourceName to obtain the tenants for
     * the authenticated user.
     * @return collection containing the roles
     */
    @Override
    public Collection<Group> getTenants(String username, String groupClassName) throws LoginException {

        if (logger.isDebugEnabled()) {
            logger.debug("getTenants using tenantsQuery: " + tenantsQuery + ", username: " + username);
        }

        Connection conn = null;
        HashMap<String, Group> groupsMap = new HashMap<String, Group>();
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            conn = getConnection();
            // Get the user role names
            if (logger.isDebugEnabled()) {
                logger.debug("Executing query: " + tenantsQuery + ", with username: " + username);
            }

            ps = conn.prepareStatement(tenantsQuery);
            try {
                ps.setString(1, username);
            } catch (ArrayIndexOutOfBoundsException ignore) {
                // The query may not have any parameters so just try it
            }
            rs = ps.executeQuery();
            if (rs.next() == false) {
                if (logger.isDebugEnabled()) {
                    logger.debug("No tenants found");
                }
                // We are running with an unauthenticatedIdentity so create an
                // empty Tenants set and return.
                // FIXME  should this be allowed?
                Group g = createGroup(groupClassName, "Tenants");
                groupsMap.put(g.getName(), g);
                return groupsMap.values();
            }

            do {
                String tenantId = rs.getString(1);
                String tenantName = rs.getString(2);
                String groupName = rs.getString(3);
                if (groupName == null || groupName.length() == 0) {
                    groupName = "Tenants";
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

    private Connection getConnection() throws LoginException, SQLException {
        InitialContext ctx = null;
        Connection conn = null;
        String dataSourceName = getDataSourceName();
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
                	e.printStackTrace();
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
    public String getTenantQuery() {
        return tenantsQuery;
    }

    /**
     * @param tenantQuery the tenantQuery to set
     */
    public void setTenantQuery(String tenantQuery) {
        this.tenantsQuery = tenantQuery;
    }
}

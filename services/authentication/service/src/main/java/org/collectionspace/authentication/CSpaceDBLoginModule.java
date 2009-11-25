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
package org.collectionspace.authentication;

import java.lang.reflect.Constructor;
import java.security.Principal;
import java.security.acl.Group;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import java.util.HashMap;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.security.auth.login.FailedLoginException;
import javax.security.auth.login.LoginException;
import javax.sql.DataSource;
import org.jboss.security.SimpleGroup;
import org.jboss.security.SimplePrincipal;
import org.jboss.security.auth.spi.DatabaseServerLoginModule;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;

public class CSpaceDBLoginModule extends DatabaseServerLoginModule {

    //disabled due to classloading problem
//    private Logger logger = LoggerFactory.getLogger(CSpaceDBLoginModule.class);
    private boolean log = true; //logger.isDebugEnabled();

    private void log(String str) {
        System.out.println(str);
    }

    protected String getUsersPassword() throws LoginException {

        String username = getUsername();
        String password = null;
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        InitialContext ctx = null;
        try {

            ctx = new InitialContext();
            DataSource ds = (DataSource) ctx.lookup(dsJndiName);
            if (ds == null) {
                throw new IllegalArgumentException("datasource not found: " + dsJndiName);
            }
            conn = ds.getConnection();
            // Get the password
            if (log) {
                log("Executing query: " + principalsQuery + ", with username: " + username);
            }
            ps = conn.prepareStatement(principalsQuery);
            ps.setString(1, username);
            rs = ps.executeQuery();
            if (rs.next() == false) {
                if (log) {
                    log("Query returned no matches from db");
                }
                throw new FailedLoginException("No matching username found");
            }

            password = rs.getString(1);
            password = convertRawPassword(password);
            if (log) {
                log("Obtained user password");
            }
        } catch (NamingException ex) {
            LoginException le = new LoginException("Error looking up DataSource from: " + dsJndiName);
            le.initCause(ex);
            throw le;
        } catch (SQLException ex) {
            LoginException le = new LoginException("Query failed");
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
            if (ctx != null) {
                try {
                    ctx.close();
                } catch (Exception e) {
                }
            }
        }
        return password;
    }

    /** Execute the rolesQuery against the dsJndiName to obtain the roles for
    the authenticated user.

    @return Group[] containing the sets of roles
     */
    protected Group[] getRoleSets() throws LoginException {
        String username = getUsername();
        if (log) {
            log("getRoleSets using rolesQuery: " + rolesQuery + ", username: " + username);
        }

        Connection conn = null;
        HashMap setsMap = new HashMap();
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            InitialContext ctx = new InitialContext();
            DataSource ds = (DataSource) ctx.lookup(dsJndiName);
            conn = ds.getConnection();
            // Get the user role names
            if (log) {
                log("Executing query: " + rolesQuery + ", with username: " + username);
            }

            ps = conn.prepareStatement(rolesQuery);
            try {
                ps.setString(1, username);
            } catch (ArrayIndexOutOfBoundsException ignore) {
                // The query may not have any parameters so just try it
            }
            rs = ps.executeQuery();
            if (rs.next() == false) {
                if (log) {
                    log("No roles found");
                }
//                if(aslm.getUnauthenticatedIdentity() == null){
//                    throw new FailedLoginException("No matching username found in Roles");
//                }
                /* We are running with an unauthenticatedIdentity so create an
                empty Roles set and return.
                 */

                Group[] roleSets = {new SimpleGroup("Roles")};
                return roleSets;
            }

            do {
                String name = rs.getString(1);
                String groupName = rs.getString(2);
                if (groupName == null || groupName.length() == 0) {
                    groupName = "Roles";
                }

                Group group = (Group) setsMap.get(groupName);
                if (group == null) {
                    group = new SimpleGroup(groupName);
                    setsMap.put(groupName, group);
                }

                try {
//                    Principal p = aslm.createIdentity(name);
                    Principal p = createIdentity(name);
                    if (log) {
                        log("Assign user to role " + name);
                    }

                    group.addMember(p);
                } catch (Exception e) {
                    log("Failed to create principal: " + name + " " + e.toString());
                }

            } while (rs.next());
        } catch (NamingException ex) {
            LoginException le = new LoginException("Error looking up DataSource from: " + dsJndiName);
            le.initCause(ex);
            throw le;
        } catch (SQLException ex) {
            LoginException le = new LoginException("Query failed");
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
                } catch (Exception ex) {
                }
            }

        }

        Group[] roleSets = new Group[setsMap.size()];
        setsMap.values().toArray(roleSets);
        return roleSets;
    }

    /** Utility method to create a Principal for the given username. This
     * creates an instance of the principalClassName type if this option was
     * specified using the class constructor matching: ctor(String). If
     * principalClassName was not specified, a SimplePrincipal is created.
     *
     * @param username the name of the principal
     * @return the principal instance
     * @throws java.lang.Exception thrown if the custom principal type cannot be created.
     */
    protected Principal createIdentity(String username)
            throws Exception {
        Principal p = null;
        if (principalClassName == null) {
            p = new SimplePrincipal(username);
        } else {
            ClassLoader loader = Thread.currentThread().getContextClassLoader();
            Class clazz = loader.loadClass(principalClassName);
            Class[] ctorSig = {String.class};
            Constructor ctor = clazz.getConstructor(ctorSig);
            Object[] ctorArgs = {username};
            p = (Principal) ctor.newInstance(ctorArgs);
        }

        return p;
    }
}

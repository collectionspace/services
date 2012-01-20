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
package org.collectionspace.authentication.jaas;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import java.security.acl.Group;
import javax.security.auth.Subject;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.login.LoginException;

import org.collectionspace.authentication.realm.db.CSpaceDbRealm;
import org.jboss.security.auth.spi.UsernamePasswordLoginModule;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * CollectionSpace default identity provider supporting multi-tenancy
 * @author
 */
public class CSpaceJBossDBLoginModule extends UsernamePasswordLoginModule {

    private Logger logger = LoggerFactory.getLogger(CSpaceJBossDBLoginModule.class);

    private CSpaceDbRealm realm;

    /**
     * Initialize CSpaceDBLoginModule
     *
     * @param options -
     * dsJndiName: The name of the DataSource of the database containing the
     *    Principals, Roles tables
     * principalsQuery: The prepared statement query, equivalent to:
     *    "select Password from Principals where PrincipalID=?"
     * rolesQuery: The prepared statement query, equivalent to:
     *    "select Role, RoleGroup from Roles where PrincipalID=?"
     * tenantsQuery:
     * "select TenantId, TenantName, TenantGroup from Tenants where PrincipalID=?"
     */
    public void initialize(Subject subject, CallbackHandler callbackHandler,
            Map sharedState, Map options) {
        super.initialize(subject, callbackHandler, sharedState, options);
        realm = new CSpaceDbRealm(options);
    }

    protected String getUsersPassword() throws LoginException {

        String username = getUsername();
        String password = null;
        try {
            password = realm.getUsersPassword(username);
            password = convertRawPassword(password);
            if (logger.isDebugEnabled()) {
            	logger.debug("Obtained user password for: " + username);
            }
        } catch (LoginException lex) {
            throw lex;
        } catch (Exception ex) {
            LoginException le = new LoginException("Unknown Exception");
            le.initCause(ex);
            throw le;
        }
        return password;
    }
    
    @Override
    public boolean commit() throws LoginException {
    	boolean result;
    	result = super.commit();
    	return result;
    }
    
    @Override
    public boolean abort() throws LoginException {
    	boolean result;
    	result = super.abort();
    	return result;
    }

    /** Execute the rolesQuery against the dsJndiName to obtain the roles for
    the authenticated user.

    @return Group[] containing the sets of roles
     */
    protected Group[] getRoleSets() throws LoginException {
        String username = getUsername();

        Collection<Group> roles = realm.getRoles(username,
                "org.collectionspace.authentication.CSpacePrincipal",
                "org.jboss.security.SimpleGroup");

        Collection<Group> tenants = realm.getTenants(username,
                "org.jboss.security.SimpleGroup");

        List<Group> all = new ArrayList<Group>();
        all.addAll(roles);
        all.addAll(tenants);
        Group[] roleSets = new Group[all.size()];
        all.toArray(roleSets);
        return roleSets;
    }

    /** A hook to allow subclasses to convert a password from the database
    into a plain text string or whatever form is used for matching against
    the user input. It is called from within the getUsersPassword() method.
    @param rawPassword - the password as obtained from the database
    @return the argument rawPassword
     */
    protected String convertRawPassword(String rawPassword) {
        return rawPassword;
    }
}

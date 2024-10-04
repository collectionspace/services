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
/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.collectionspace.authentication.realm;

import java.util.Set;

import javax.security.auth.login.AccountException;

import org.collectionspace.authentication.CSpaceTenant;

/**
 * Interface for the CollectionSpace realm.
 */
public interface CSpaceRealm {

	/**
	 * Retrieves the username of the user with the given SSO ID.
	 * @param ssoId
	 * @return
	 * @throws AccountException
	 */
	public String getUsernameForSsoId(String ssoId) throws AccountException;

	/**
	 * Retrieves the "salt" used to encrypt the user's password
	 * @param username
	 * @return
	 * @throws AccountException
	 */
	public String getSalt(String username) throws AccountException;

    /**
     * Retrieves the hashed password used to authenticate a user.
     *
     * @param username
     * @return the password
     * @throws AccountNotFoundException if the user is not found
     * @throws AccountException if the password could not be retrieved
     */
    public String getPassword(String username) throws AccountException;

    /**
     * Retrieves the roles for a user.
     *
     * @param username
     * @return a collection of roles
     * @throws AccountException if the roles could not be retrieved
     */
    public Set<String> getRoles(String username) throws AccountException;

    /**
     * Retrieves the enabled tenants associated with a user.
     *
     * @param username
     * @return a collection of tenants
     * @throws AccountException if the tenants could not be retrieved
     */
    public Set<CSpaceTenant> getTenants(String username) throws AccountException;

    /**
     * Retrieves the tenants associated with a user, optionally including disabled tenants.
     *
     * @param username
     * @param includeDisabledTenants if true, include disabled tenants
     * @return a collection of tenants
     * @throws AccountException if the tenants could not be retrieved
     */
    public Set<CSpaceTenant> getTenants(String username, boolean includeDisabledTenants) throws AccountException;

    /**
     * Retrieves the ID from the SSO provider, if the user is associated with one.
     *
     * @param username
     * @return the ID from the SSO provider, or null
     * @throws AccountException
     */
    public String getSsoId(String username) throws AccountException;

    /**
     * Determines if the user is required to login using single sign-on.
     *
     * @param username
     * @return true if SSO is required, false otherwise
     * @throws AccountException
     */
    public boolean isRequireSSO(String username) throws AccountException;
}

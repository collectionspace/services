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

import java.security.acl.Group;
import java.util.Collection;
import javax.security.auth.login.LoginException;

/**
 * CSpaceRealm defines interface for CollectionSpace Realm
 */
public interface CSpaceRealm {

        /**
     * Obtain password for the given user
     * @param username
     * @return
     * @throws LoginException
     */
    public String getUsersPassword(String username) throws LoginException;

    /**
     * Obtain the roles for the authenticated user.
     * @return collection containing the roles
     */
    public Collection<Group> getRoles(String username, String principalClassName, String groupClassName) throws LoginException;

    /**
     * Obtain the tenants for the authenticated user.
     * @return collection containing the roles
     */
    public Collection<Group> getTenants(String username, String groupClassName) throws LoginException;

}

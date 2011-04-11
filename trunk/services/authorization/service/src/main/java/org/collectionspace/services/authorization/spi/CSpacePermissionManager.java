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
package org.collectionspace.services.authorization.spi;

import org.collectionspace.services.authorization.CSpaceResource;
import org.collectionspace.services.authorization.CSpaceAction;
import org.collectionspace.services.authorization.PermissionException;
import org.collectionspace.services.authorization.PermissionNotFoundException;

/**
 * Permission management interface for CSpace services
 * @author 
 */
public interface CSpacePermissionManager {

    /**
     * addPermisison adds permission for given action on given resource for given principals
     * @param res resource
     * @param principals an array of principal names
     * @action action on the resource
     * @grant true to grant, false to deny
     * @throws PermissionException
     * @see CSpaceResource
     * @see CSpaceAction
     */
    public void addPermissions(CSpaceResource res, CSpaceAction action, String[] principals, boolean grant)
            throws PermissionException;

    /**
     * removePermission removes permission(s) for given action on given resource involving given principals
     * @param res
     * @param action
     * @param principals
     * @throws PermissionNotFoundException
     * @throws PermissionException
     * @see CSpaceResource
     * @see CSpaceAction
     */
    public void deletePermissions(CSpaceResource res, CSpaceAction action, String[] principals)
            throws PermissionNotFoundException, PermissionException;

    /**
     * deletePermissions delete all permissions for given action on given resource
     * @param res
     * @param action
     * @throws PermissionNotFoundException
     * @throws PermissionException
     * @see CSpaceResource
     * @see CSpaceAction
     */
    public void deletePermissions(CSpaceResource res, CSpaceAction action)
            throws PermissionNotFoundException, PermissionException;

    /**
     * deletePermissions delete all permissions for given resource
     * @param res
     * @throws PermissionNotFoundException
     * @throws PermissionException
     * @see CSpaceResource
     */
    public void deletePermissions(CSpaceResource res)
            throws PermissionNotFoundException, PermissionException;
}

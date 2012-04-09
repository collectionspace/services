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
package org.collectionspace.services.authorization.importer;

import java.io.FileInputStream;
import java.io.InputStream;

import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.collectionspace.services.authorization.perms.Permission;
import org.collectionspace.services.authorization.PermissionRole;
import org.collectionspace.services.authorization.perms.PermissionsList;
import org.collectionspace.services.authorization.PermissionsRolesList;
import org.collectionspace.services.common.authorization_mgt.AuthorizationCommon;

/**
 * AuthorizationSeed seeds authorizations (permission, role) into authz provider database
 * @author 
 */
public class AuthorizationSeed {

    final Logger logger = LoggerFactory.getLogger(AuthorizationSeed.class);


    /**
     * seedPermissions seed permissions from given files
     * @param permFileName permisison file name
     * @param permRoleFileName permission role file name
     * @throws Exception
     */
    public void seedPermissions(String permFileName, String permRoleFileName) throws Exception {
        PermissionsRolesList permRoleList =
                (PermissionsRolesList) fromFile(PermissionsRolesList.class,
                permRoleFileName);
        if (logger.isDebugEnabled()) {
            logger.debug("read permissions-roles from " + permRoleFileName);
        }
        PermissionsList permList =
            (PermissionsList) fromFile(PermissionsList.class,
            permFileName);
	    if (logger.isDebugEnabled()) {
	        logger.debug("read permissions from " + permFileName);
	    }

        seedPermissions(permList, permRoleList);
    }

    /**
     * seedPermissions seed permissions from given permisison and permission role lists
     * @param permList
     * @param permRoleList
     * @throws Exception
     */
    public void seedPermissions(PermissionsList permList, PermissionsRolesList permRoleList)
            throws Exception {
    	
    	seedPermissions(permList.getPermission(), permRoleList.getPermissionRole());
    }
    
    /**
     * seedPermissions seed permissions from given permisison and permission role lists
     * @param permList
     * @param permRoleList
     * @throws Exception
     */
    public void seedPermissions(List<Permission> permList, List<PermissionRole> permRoleList)
            throws Exception {
        for (Permission p : permList) {
            if (logger.isTraceEnabled()) {
                logger.trace("adding permission for res=" + p.getResourceName() +
                        " for tenant=" + p.getTenantId());
            }
            for (PermissionRole pr : permRoleList) {
                if (pr.getPermission().get(0).getPermissionId().equals(p.getCsid())) {
                	AuthorizationCommon.addPermissionsForUri(p, pr);
                }
            }
        }
    }
    
    
    /**
     * getAction is a convenience method to get corresponding action for
     * given ActionType
     * @param action
     * @return
     *
    private CSpaceAction getAction(ActionType action) {
        if (ActionType.CREATE.equals(action)) {
            return CSpaceAction.CREATE;
        } else if (ActionType.READ.equals(action)) {
            return CSpaceAction.READ;
        } else if (ActionType.UPDATE.equals(action)) {
            return CSpaceAction.UPDATE;
        } else if (ActionType.DELETE.equals(action)) {
            return CSpaceAction.DELETE;
        } else if (ActionType.SEARCH.equals(action)) {
            return CSpaceAction.SEARCH;
        } else if (ActionType.ADMIN.equals(action)) {
            return CSpaceAction.ADMIN;
        } else if (ActionType.START.equals(action)) {
            return CSpaceAction.START;
        } else if (ActionType.STOP.equals(action)) {
            return CSpaceAction.STOP;
        }
        throw new IllegalArgumentException("action = " + action.toString());
    }
    */

    static Object fromFile(Class jaxbClass, String fileName) throws Exception {
        InputStream is = new FileInputStream(fileName);
        try {
            JAXBContext context = JAXBContext.newInstance(jaxbClass);
            Unmarshaller unmarshaller = context.createUnmarshaller();
            //note: setting schema to null will turn validator off
            unmarshaller.setSchema(null);
            return jaxbClass.cast(unmarshaller.unmarshal(is));
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (Exception e) {
                }
            }
        }
    }
}

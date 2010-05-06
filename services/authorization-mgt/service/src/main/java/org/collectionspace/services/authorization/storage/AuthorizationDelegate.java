/**
 *  This document is a part of the source code and related artifacts
 *  for CollectionSpace, an open source collections management system
 *  for museums and related institutions:

 *  http://www.collectionspace.org
 *  http://wiki.collectionspace.org

 *  Copyright 2010 University of California at Berkeley

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
package org.collectionspace.services.authorization.storage;

import java.util.ArrayList;
import java.util.List;
import org.collectionspace.services.authorization.ActionType;
import org.collectionspace.services.authorization.AuthZ;
import org.collectionspace.services.authorization.CSpaceAction;
import org.collectionspace.services.authorization.CSpaceResource;
import org.collectionspace.services.authorization.Permission;
import org.collectionspace.services.authorization.PermissionAction;
import org.collectionspace.services.authorization.PermissionException;
import org.collectionspace.services.authorization.PermissionRole;
import org.collectionspace.services.authorization.PermissionValue;
import org.collectionspace.services.authorization.RoleValue;
import org.collectionspace.services.authorization.SubjectType;
import org.collectionspace.services.authorization.URIResourceImpl;
import org.collectionspace.services.common.context.ServiceContext;
import org.collectionspace.services.common.storage.jpa.JpaStorageUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * AuthorizationDelegate delegates permissions management to the authorization
 * service from the RESTful service
 * @author
 */
public class AuthorizationDelegate {

    private final Logger logger = LoggerFactory.getLogger(AuthorizationDelegate.class);

    static void addPermissions(ServiceContext ctx, PermissionRole pr) throws Exception {
        SubjectType subject = PermissionRoleUtil.getRelationSubject(ctx, pr);
        AuthZ authz = AuthZ.get();
        if (subject.equals(SubjectType.ROLE)) {
            PermissionValue pv = pr.getPermissions().get(0);
            CSpaceResource[] resources = getResources(pv);
            String[] roles = getRoles(pr.getRoles());
            for (CSpaceResource res : resources) {
                authz.addPermissions(res, roles);
            }
        } else if (SubjectType.PERMISSION.equals(subject)) {
            RoleValue rv = pr.getRoles().get(0);
            String[] roles = {rv.getRoleName()};
            for (PermissionValue pv : pr.getPermissions()) {
                CSpaceResource[] resources = getResources(pv);
                for (CSpaceResource res : resources) {
                    authz.addPermissions(res, roles);
                }
            }
        }
    }

    static void deletePermissions(ServiceContext ctx, PermissionRole pr)
            throws Exception {
        PermissionValue pv = pr.getPermissions().get(0);
        deletePermissions(pv);
    }

    static void deletePermissions(PermissionValue pv)
            throws Exception {
        CSpaceResource[] resources = getResources(pv);
        AuthZ authz = AuthZ.get();
        for (CSpaceResource res : resources) {
            authz.deletePermissions(res);
        }
    }


    /**
     * addPermissionsForUri add permissions from given permission configuration
     * with assumption that resource is of type URI
     * @param permission configuration
     */
    //FIXME this method should be in the restful web service resource of authz
    public void addPermissionsForUri(Permission perm,
            PermissionRole permRole) throws PermissionException {
        List<String> principals = new ArrayList<String>();
        if (!perm.getCsid().equals(permRole.getPermissions().get(0).getPermissionId())) {
            throw new IllegalArgumentException("permission ids do not"
                    + " match for role=" + permRole.getRoles().get(0).getRoleName()
                    + " with permissionId=" + permRole.getPermissions().get(0).getPermissionId()
                    + " for permission with csid=" + perm.getCsid());
        }
        for (RoleValue roleValue : permRole.getRoles()) {
            principals.add(roleValue.getRoleName());
        }
        List<PermissionAction> permActions = perm.getActions();
        for (PermissionAction permAction : permActions) {
            CSpaceAction action = getAction(permAction.getName());
            URIResourceImpl uriRes = new URIResourceImpl(perm.getTenantId(),
                    perm.getResourceName(), action);
            AuthZ.get().addPermissions(uriRes, principals.toArray(new String[0]));
        }
    }

    /**
     * getRoles get roles (string) array from given RoleValue list
     * @param rvl rolevalue list
     * @return string array with role names
     * @see RoleValue
     */
    private static String[] getRoles(List<RoleValue> rvl) {
        List<String> rvls = new ArrayList<String>();
        for (RoleValue rv : rvl) {
            //assumption: rolename is relationship metadata is mandatory
            if (rv.getRoleName() != null) {
                rvls.add(rv.getRoleName());
            }
        }
        return rvls.toArray(new String[0]);
    }

    /**
     * getResources from given PermissionValue
     * @param pv permission value
     * @return array of CSpaceResource
     * @see PermissionValue
     * @see CSpaceResource
     */
    private static CSpaceResource[] getResources(PermissionValue pv) {
        List<CSpaceResource> rl = new ArrayList<CSpaceResource>();
        Permission p = (Permission) JpaStorageUtils.getEntity(pv.getPermissionId(),
                Permission.class);
        if (p != null) {
            for (PermissionAction pa : p.getActions()) {

                CSpaceResource res = new URIResourceImpl(pv.getResourceName(),
                        getAction(pa.getName()));
                rl.add(res);
            }
        }
        return rl.toArray(new CSpaceResource[0]);
    }


    /**
     * getAction is a convenience method to get corresponding action for
     * given ActionType
     * @param action
     * @return
     */
    public static CSpaceAction getAction(ActionType action) {
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

}

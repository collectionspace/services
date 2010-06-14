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
import org.collectionspace.services.authorization.EffectType;
import org.collectionspace.services.authorization.Permission;
import org.collectionspace.services.authorization.PermissionAction;
import org.collectionspace.services.authorization.PermissionException;
import org.collectionspace.services.authorization.PermissionRole;
import org.collectionspace.services.authorization.PermissionValue;
import org.collectionspace.services.authorization.Role;
import org.collectionspace.services.authorization.RoleValue;
import org.collectionspace.services.authorization.SubjectType;
import org.collectionspace.services.authorization.URIResourceImpl;
import org.collectionspace.services.common.context.ServiceContext;
import org.collectionspace.services.common.document.DocumentNotFoundException;
import org.collectionspace.services.common.storage.jpa.JpaStorageUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * AuthorizationDelegate delegates permissions management to the underlying authorization
 * service from the RESTful service layer. The authorization service for example
 * might manage permissions with the help of a provider (e.g. Spring Security ACL)
 * @author
 */
public class AuthorizationDelegate {

    private static final Logger logger = LoggerFactory.getLogger(AuthorizationDelegate.class);

    /**
     * addPermissions add permissions represented given PermissionRole
     * @param ctx
     * @param pr permission role
     * @throws Exception
     * @see PermissionRole
     */
    static void addPermissions(ServiceContext ctx, PermissionRole pr) throws Exception {
        SubjectType subject = PermissionRoleUtil.getRelationSubject(ctx, pr);
        AuthZ authz = AuthZ.get();
        if (subject.equals(SubjectType.ROLE)) {
            PermissionValue pv = pr.getPermissions().get(0);
            Permission p = getPermission(pv.getPermissionId());
            if (p == null) {
                String msg = "addPermissions: No permission found for id=" + pv.getPermissionId();
                logger.error(msg);
                throw new DocumentNotFoundException(msg);
            }
            CSpaceResource[] resources = getResources(p);
            String[] roles = getRoles(pr.getRoles());
            for (CSpaceResource res : resources) {
                boolean grant = p.getEffect().equals(EffectType.PERMIT) ? true : false;
                authz.addPermissions(res, roles, grant);
            }
        } else if (SubjectType.PERMISSION.equals(subject)) {
            RoleValue rv = pr.getRoles().get(0);
            Role r = getRole(rv.getRoleId());
            if (r == null) {
                String msg = "addPermissions: No role found for id=" + rv.getRoleId();
                logger.error(msg);
                throw new DocumentNotFoundException(msg);
            }
            String[] roles = {rv.getRoleName()};
            for (PermissionValue pv : pr.getPermissions()) {
                Permission p = getPermission(pv.getPermissionId());
                if (p == null) {
                    String msg = "addPermissions: No permission found for id=" + pv.getPermissionId();
                    logger.error(msg);
                    //TODO: would be nice contiue to still send 400 back
                    continue;
                }
                CSpaceResource[] resources = getResources(p);
                for (CSpaceResource res : resources) {
                    boolean grant = p.getEffect().equals(EffectType.PERMIT) ? true : false;
                    authz.addPermissions(res, roles, grant);
                }
            }
        }
    }

    /**
     * deletePermissions delete all permissions associated with given permission role
     * @param ctx
     * @param pr permissionrole
     * @throws Exception
     */
    static void deletePermissions(ServiceContext ctx, PermissionRole pr)
            throws Exception {
        SubjectType subject = PermissionRoleUtil.getRelationSubject(ctx, pr);
        AuthZ authz = AuthZ.get();
        if (subject.equals(SubjectType.ROLE)) {
            PermissionValue pv = pr.getPermissions().get(0);
            Permission p = getPermission(pv.getPermissionId());
            if (p == null) {
                String msg = "deletePermissions: No permission found for id=" + pv.getPermissionId();
                logger.error(msg);
                throw new DocumentNotFoundException(msg);
            }
            CSpaceResource[] resources = getResources(p);
            String[] roles = getRoles(pr.getRoles());
            for (CSpaceResource res : resources) {
                authz.deletePermissions(res, roles);
            }
        } else if (SubjectType.PERMISSION.equals(subject)) {
            RoleValue rv = pr.getRoles().get(0);
            Role r = getRole(rv.getRoleId());
            if (r == null) {
                String msg = "deletePermissions: No role found for id=" + rv.getRoleId();
                logger.error(msg);
                throw new DocumentNotFoundException(msg);
            }
            String[] roles = {rv.getRoleName()};
            for (PermissionValue pv : pr.getPermissions()) {
                Permission p = getPermission(pv.getPermissionId());
                if (p == null) {
                    String msg = "deletePermissions: No permission found for id=" + pv.getPermissionId();
                    logger.error(msg);
                    //TODO: would be nice contiue to still send 400 back
                    continue;
                }
                CSpaceResource[] resources = getResources(p);
                for (CSpaceResource res : resources) {
                    authz.deletePermissions(res, roles);
                }
            }
        }
    }

    /**
     * deletePermissions delete permissions associated with given permission id
     * @param permCsid
     * @throws Exception
     */
    static public void deletePermissions(String permCsid) throws Exception {
        Permission p = getPermission(permCsid);
        if (p == null) {
            String msg = "deletePermissions: No permission found for id=" + permCsid;
            logger.error(msg);
            throw new DocumentNotFoundException(msg);
        }
        CSpaceResource[] resources = getResources(p);
        AuthZ authz = AuthZ.get();

        for (CSpaceResource res : resources) {
            try {
                authz.deletePermissions(res);
            } catch (PermissionException pe) {
                //perms are created downthere only if roles are related to the permissions
                logger.info("no permissions found in authz service provider for "
                        + "permCsid=" + permCsid + " res=" + res.getId());
            }
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
            Role r = getRole(rv.getRoleId());
            if (r == null) {
                String msg = "getRoles: No role found for id=" + rv.getRoleId();
                logger.error(msg);
                //TODO: would be nice contiue to still send 400 back
                continue;
            }
            rvls.add(r.getRoleName());
        }
        return rvls.toArray(new String[0]);
    }

    /**
     * getResources from given PermissionValue
     * @param permisison csid
     * @return array of CSpaceResource
     * @see PermissionValue
     * @see CSpaceResource
     */
    private static CSpaceResource[] getResources(Permission p) {
        List<CSpaceResource> rl = new ArrayList<CSpaceResource>();

        for (PermissionAction pa : p.getActions()) {
            CSpaceResource res = null;
            if (p.getTenantId() == null) {
                res = new URIResourceImpl(p.getResourceName(),
                        getAction(pa.getName()));
            } else {
                res = new URIResourceImpl(p.getTenantId(), p.getResourceName(),
                        getAction(pa.getName()));
            }
            rl.add(res);
        }
        return rl.toArray(new CSpaceResource[0]);
    }

    private static Permission getPermission(String permCsid) {
        Permission p = (Permission) JpaStorageUtils.getEntity(permCsid,
                Permission.class);
        return p;
    }

    private static Role getRole(String roleCsid) {
        Role r = (Role) JpaStorageUtils.getEntity(roleCsid,
                Role.class);
        return r;
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

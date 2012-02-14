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
package org.collectionspace.services.authorization.spring;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.collectionspace.services.authorization.CSpaceAction;
import org.collectionspace.services.authorization.spi.CSpacePermissionManager;
import org.collectionspace.services.authorization.CSpaceResource;
import org.collectionspace.services.authorization.PermissionException;
import org.collectionspace.services.authorization.PermissionNotFoundException;

import org.springframework.security.acls.model.AccessControlEntry;
import org.springframework.security.acls.model.AclDataAccessException;
import org.springframework.security.acls.model.AlreadyExistsException;
import org.springframework.security.acls.model.MutableAcl;
import org.springframework.security.acls.model.NotFoundException;
import org.springframework.security.acls.model.ObjectIdentity;
import org.springframework.security.acls.model.Permission;
import org.springframework.security.acls.model.Sid;
import org.springframework.transaction.TransactionStatus;

/**
 * Manages permissions in Spring Security
 * @author 
 */
public class SpringPermissionManager implements CSpacePermissionManager {

    final Log log = LogFactory.getLog(SpringPermissionManager.class);
    private SpringAuthorizationProvider provider;

    SpringPermissionManager(SpringAuthorizationProvider provider) {
        this.provider = provider;
    }

    /**
     * addPermissions adds permissions according to the given grant for given
     * resource#action for each given principal
     * @param res
     * @param action
     * @param principals
     * @param grant
     * @throws PermissionException
     */
    @Override
    public void addPermissions(CSpaceResource res, CSpaceAction action, String[] principals, boolean grant)
            throws PermissionException {
        ObjectIdentity oid = SpringAuthorizationProvider.getObjectIdentity(res);
        Sid[] sids = SpringAuthorizationProvider.getSids(principals);
        Permission p = SpringAuthorizationProvider.getPermission(action);
        TransactionStatus status = provider.beginTransaction("addPermssions");

        //add permission for each sid
        for (Sid sid : sids) {
            try {
                addPermission(oid, p, sid, grant);
                if (log.isDebugEnabled()) {
                    log.debug("addpermissions(res,action,prin[], grant), success for "
                            + " res=" + res.toString()
                            + " action=" + action.toString()
                            + " grant=" + grant
                            + " oid=" + oid.toString()
                            + " perm=" + p.toString()
                            + " sid=" + sid.toString());
                }

            } catch (AlreadyExistsException aex) {
                if (log.isWarnEnabled()) {
                    log.warn("addpermissions(res,action,prin[], grant) failed,"
                            + " oid=" + oid.toString()
                            + " res=" + res.toString()
                            + " grant=" + grant
                            + " action=" + action.toString()
                            + " oid=" + oid.toString()
                            + " perm=" + p.toString(), aex);
                }
                throw new PermissionException("Permission already exists", aex);
            } catch (Exception ex) {
                String msg = "addpermissions(res,action,prin[], grant) failed,"
                        + " oid=" + oid.toString()
                        + " res=" + res.toString()
                        + " action=" + action.toString()
                        + " oid=" + oid.toString()
                        + " perm=" + p.toString()
                        + " grant=" + grant;
                if (log.isDebugEnabled()) {
                    log.debug(msg, ex);
                }
                //don't know what might be wrong...stop
                provider.rollbackTransaction(status);
                if (ex instanceof PermissionException) {
                    throw (PermissionException) ex;
                }
                throw new PermissionException(msg, ex);
            }
        }//rof
        provider.commitTransaction(status);
        if (log.isDebugEnabled()) {
            log.debug("addpermissions(res,action,prin[], grant), success for "
                    + " res=" + res.toString()
                    + " action=" + action.toString()
                    + " grant=" + grant
                    + " oid=" + oid.toString()
                    + " perm=" + p.toString()
                    + " sids=" + sids.toString());
        }
    }

    /**
     * deletePermissions removes permisions for given resource#action for each given principal
     * @param res
     * @param action
     * @param principals
     * @throws PermissionNotFoundException
     * @throws PermissionException
     */
    @Override
    public void deletePermissions(CSpaceResource res, CSpaceAction action, String[] principals)
            throws PermissionNotFoundException, PermissionException {
        ObjectIdentity oid = SpringAuthorizationProvider.getObjectIdentity(res);
        Sid[] sids = SpringAuthorizationProvider.getSids(principals);
        Permission p = SpringAuthorizationProvider.getPermission(action);
        TransactionStatus status = provider.beginTransaction("deletePermssions");
        //delete permission for each sid
        for (Sid sid : sids) {
            try {
                deletePermissions(oid, p, sid);
                if (log.isDebugEnabled()) {
                    log.debug("deletedpermissions(res,action,prin[]), success for "
                            + " res=" + res.toString()
                            + " action=" + action.toString()
                            + " oid=" + oid.toString()
                            + " perm=" + p.toString()
                            + " sid=" + sid.toString());
                }
            } catch (AclDataAccessException aex) {
                if (log.isWarnEnabled()) {
                    log.debug("deletepermissions(res,action,prin[]) failed, "
                            + " oid=" + oid.toString()
                            + " res=" + res.toString()
                            + " action=" + action.toString()
                            + " oid=" + oid.toString()
                            + " perm=" + p.toString(), aex);
                }
                //keep going
            } catch (Exception ex) {
                String msg = "deletepermissions(res,action,prin[]) failed,"
                        + " oid=" + oid.toString()
                        + " res=" + res.toString()
                        + " action=" + action.toString()
                        + " oid=" + oid.toString()
                        + " perm=" + p.toString();
                if (log.isDebugEnabled()) {
                    log.debug(msg, ex);
                }
                //don't know what might be wrong...stop
                provider.rollbackTransaction(status);
                if (ex instanceof PermissionException) {
                    throw (PermissionException) ex;
                }
                throw new PermissionException(msg, ex);
            }
        }
        provider.commitTransaction(status);
        if (log.isDebugEnabled()) {
            log.debug("deletedpermissions(res,action,prin[]), success for "
                    + " res=" + res.toString()
                    + " action=" + action.toString()
                    + " oid=" + oid.toString()
                    + " perm=" + p.toString()
                    + " sids=" + sids.toString());
        }
    }

    /**
     * deletePermissions
     * @param res
     * @param action
     * @throws PermissionNotFoundException
     * @throws PermissionException
     */
    //non-javadoc NOTE: this is a very destructive operation. it would remove all permissions
    //to access given resource#action for ANY role including administrators
    @Override
    public void deletePermissions(CSpaceResource res, CSpaceAction action)
            throws PermissionNotFoundException, PermissionException {
        ObjectIdentity oid = SpringAuthorizationProvider.getObjectIdentity(res);
        Permission p = SpringAuthorizationProvider.getPermission(action);
        TransactionStatus status = provider.beginTransaction("deletePermssions");
        try {
            deletePermissions(oid, p, null);
            provider.commitTransaction(status);
            if (log.isDebugEnabled()) {
                log.debug("deletepermissions(res,action) success, "
                        + " res=" + res.toString()
                        + " action=" + action.toString()
                        + " oid=" + oid.toString()
                        + " perm=" + p.toString());
            }
        } catch (AclDataAccessException aex) {
            provider.rollbackTransaction(status);
            log.debug("deletepermissions(res,action) failed,"
                    + " oid=" + oid.toString()
                    + " res=" + res.toString()
                    + " action=" + action.toString()
                    + " oid=" + oid.toString()
                    + " perm=" + p.toString(), aex);
            throw new PermissionException(aex);
        } catch (Exception ex) {
            provider.rollbackTransaction(status);
            String msg = "deletepermissions(res,action,prin[]) failed,"
                    + " oid=" + oid.toString()
                    + " res=" + res.toString()
                    + " action=" + action.toString()
                    + " oid=" + oid.toString()
                    + " perm=" + p.toString();
            if (log.isDebugEnabled()) {
                log.debug(msg, ex);
            }
            if (ex instanceof PermissionException) {
                throw (PermissionException) ex;
            }
            throw new PermissionException(msg, ex);
        }

    }

    /**
     * deletePermissions
     * @param res
     * @throws PermissionNotFoundException
     * @throws PermissionException
     */
    //non-javadoc NOTE: this is a very very destructive operation. it would remove all permissions
    //to access given resource for ANY action for ANY role including administrators
    @Override
    public void deletePermissions(CSpaceResource res)
            throws PermissionNotFoundException, PermissionException {
        ObjectIdentity oid = SpringAuthorizationProvider.getObjectIdentity(res);
        TransactionStatus status = provider.beginTransaction("deletePermssion");
        try {
            provider.getProviderAclService().deleteAcl(oid, true);
            provider.commitTransaction(status);
            if (log.isDebugEnabled()) {
                log.debug("deletepermissions(res) success, "
                        + " res=" + res.toString()
                        + " oid=" + oid.toString());
            }
        } catch (AclDataAccessException aex) {
            provider.rollbackTransaction(status);
            log.debug("deletepermissions(res) failed,"
                    + " oid=" + oid.toString()
                    + " res=" + res.toString()
                    + " oid=" + oid.toString(), aex);
            throw new PermissionException(aex);
        } catch (Exception ex) {
            provider.rollbackTransaction(status);
            String msg = "deletepermissions(res) failed,"
                    + " oid=" + oid.toString()
                    + " res=" + res.toString()
                    + " oid=" + oid.toString();
            if (log.isDebugEnabled()) {
                log.debug(msg, ex);
            }
            if (ex instanceof PermissionException) {
                throw (PermissionException) ex;
            }
            throw new PermissionException(msg, ex);
        }
    }

    /**
     * addPermission adds permission grant for given object identity for given permission
     * for given sid
     * @param oid
     * @param permission
     * @param sid
     * @param grant
     * @throws PermissionException
     */
    private void addPermission(ObjectIdentity oid, Permission permission,
            Sid sid, boolean grant) throws PermissionException {
        MutableAcl acl;

        try {
            acl = getAcl(oid);
        } catch (NotFoundException nfe) {
            if (log.isDebugEnabled()) {
                log.debug("addPermission: acl not found for oid=" + oid.toString()
                        + " perm=" + permission.toString()
                        + " sid=" + sid.toString()
                        + " grant=" + grant
                        + " adding...");
            }
            acl = provider.getProviderAclService().createAcl(oid);
        }
        acl.insertAce(acl.getEntries().size(), permission, sid, grant);
        provider.getProviderAclService().updateAcl(acl);

        if (log.isDebugEnabled()) {
            log.debug("addPermission: added acl for oid=" + oid.toString()
                    + " perm=" + permission.toString()
                    + " sid=" + sid.toString()
                    + " grant=" + grant);
        }
    }

    /**
     * deletePermissions deletes given permission on given object id for given sid
     * @param oid
     * @param permission
     * @param sid
     */
    //non-javadoc NOTE: if sid is null it would remove ACEs for all sid(s)
    private void deletePermissions(ObjectIdentity oid, Permission permission, Sid sid) /** throws AclDataAccessException */
    {
        int i = 0;
        MutableAcl acl = getAcl(oid);
        List<AccessControlEntry> acel = acl.getEntries();
        int aces = acel.size();
        if (log.isDebugEnabled()) {
            log.debug("deletePermissions: for acl oid=" + oid.toString()
                    + " found " + aces + " aces");
        }
        ArrayList<Integer> foundAces = new ArrayList<Integer>();
        Iterator iter = acel.listIterator();
        //not possible to delete while iterating
        while (iter.hasNext()) {
            AccessControlEntry ace = (AccessControlEntry) iter.next();
            if (sid != null) {
                if (ace.getSid().equals(sid)
                        && ace.getPermission().equals(permission)) {
                    foundAces.add(i);
                }
            } else {
                if (ace.getPermission().equals(permission)) {
                    foundAces.add(i);
                }
            }
            i++;
        }
        for (int j = foundAces.size() - 1; j >= 0; j--) {
            //the following operation does not work while iterating in the while loop
            acl.deleteAce(foundAces.get(j)); //autobox
        }
        provider.getProviderAclService().updateAcl(acl);

        if (log.isDebugEnabled()) {
            log.debug("deletePermissions: for acl oid=" + oid.toString()
                    + " deleted " + i + " aces");
        }
    }

    private MutableAcl getAcl(ObjectIdentity oid) throws NotFoundException {
        MutableAcl acl = null;
        acl = (MutableAcl) provider.getProviderAclService().readAclById(oid);
        if (log.isDebugEnabled()) {
            log.debug("found acl for oid=" + oid.toString());
        }
        return acl;
    }
}

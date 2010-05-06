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

import java.util.List;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.collectionspace.services.authorization.CSpaceAction;
import org.collectionspace.services.authorization.spi.CSpacePermissionManager;
import org.collectionspace.services.authorization.CSpaceResource;
import org.collectionspace.services.authorization.PermissionException;
import org.collectionspace.services.authorization.PermissionNotFoundException;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.security.acls.model.AccessControlEntry;
import org.springframework.security.acls.model.MutableAcl;
import org.springframework.security.acls.model.NotFoundException;
import org.springframework.security.acls.model.ObjectIdentity;
import org.springframework.security.acls.model.Permission;
import org.springframework.security.acls.model.Sid;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

/**
 * Manages permissions in Spring Security
 * @author 
 */
public class SpringPermissionManager implements CSpacePermissionManager {

    final Log log = LogFactory.getLog(SpringPermissionEvaluator.class);
    private SpringAuthorizationProvider provider;

    SpringPermissionManager(SpringAuthorizationProvider provider) {
        this.provider = provider;
    }

    @Override
    public void addPermissions(CSpaceResource res, CSpaceAction action, String[] principals)
            throws PermissionException {
        ObjectIdentity oid = SpringAuthorizationProvider.mapResource(res);
        Sid[] sids = SpringAuthorizationProvider.mapPrincipal(principals);
        Permission p = SpringAuthorizationProvider.mapPermssion(action);
        TransactionStatus status = provider.beginTransaction("addPermssions");
        try {
            for (Sid sid : sids) {
                addPermission(oid, p, sid);
                if (log.isDebugEnabled()) {
                    log.debug("added permission "
                            + " res=" + res.toString()
                            + " action=" + action.toString()
                            + " oid=" + oid.toString()
                            + " perm=" + p.toString()
                            + " sid=" + sids.toString());
                }
            }
        } catch (Exception ex) {
            provider.rollbackTransaction(status);
            if (log.isDebugEnabled()) {
                ex.printStackTrace();
            }
            throw new PermissionException(ex);
        }
        provider.commitTransaction(status);

    }

    @Override
    public void deletePermissions(CSpaceResource res, CSpaceAction action, String[] principals)
            throws PermissionNotFoundException, PermissionException {
        ObjectIdentity oid = SpringAuthorizationProvider.mapResource(res);
        Sid[] sids = SpringAuthorizationProvider.mapPrincipal(principals);
        Permission p = SpringAuthorizationProvider.mapPermssion(action);
        TransactionStatus status = provider.beginTransaction("deletePermssions");
        try {
            for (Sid sid : sids) {
                deletePermissions(oid, p, sid);
                if (log.isDebugEnabled()) {
                    log.debug("deleted permission "
                            + " res=" + res.toString()
                            + " action=" + action.toString()
                            + " oid=" + oid.toString()
                            + " perm=" + p.toString()
                            + " sid=" + sids.toString());
                }
            }
        } catch (Exception ex) {
            provider.rollbackTransaction(status);
            if (log.isDebugEnabled()) {
                ex.printStackTrace();
            }
            throw new PermissionException(ex);
        }
        provider.commitTransaction(status);

    }

    @Override
    public void deletePermissions(CSpaceResource res, CSpaceAction action)
            throws PermissionNotFoundException, PermissionException {
        ObjectIdentity oid = SpringAuthorizationProvider.mapResource(res);
        Permission p = SpringAuthorizationProvider.mapPermssion(action);
        TransactionStatus status = provider.beginTransaction("deletePermssions");
        try {
            deletePermissions(oid, p, null);
            if (log.isDebugEnabled()) {
                log.debug("deleted permissions "
                        + " res=" + res.toString()
                        + " action=" + action.toString()
                        + " oid=" + oid.toString()
                        + " perm=" + p.toString());
            }
        } catch (Exception ex) {
            provider.rollbackTransaction(status);
            if (log.isDebugEnabled()) {
                ex.printStackTrace();
            }
            throw new PermissionException(ex);
        }
        provider.commitTransaction(status);


    }

    @Override
    public void deletePermissions(CSpaceResource res)
            throws PermissionNotFoundException, PermissionException {
        ObjectIdentity oid = SpringAuthorizationProvider.mapResource(res);
        TransactionStatus status = provider.beginTransaction("addPermssion");
        try {
            provider.getProviderAclService().deleteAcl(oid, true);
        } catch (Exception ex) {
            provider.rollbackTransaction(status);
            if (log.isDebugEnabled()) {
                ex.printStackTrace();
            }
            throw new PermissionException(ex);
        }
        provider.commitTransaction(status);

        if (log.isDebugEnabled()) {
            log.debug("deleted permissions "
                    + " res=" + res.toString()
                    + " oid=" + oid.toString());
        }
    }

    private void addPermission(ObjectIdentity oid, Permission permission,
            Sid recipient) throws PermissionException {
        MutableAcl acl;

        try {
            acl = getAcl(oid);
        } catch (PermissionException pnfe) {
            acl = provider.getProviderAclService().createAcl(oid);
        }
        acl.insertAce(acl.getEntries().size(), permission, recipient, true);
        provider.getProviderAclService().updateAcl(acl);

        if (log.isDebugEnabled()) {
            log.debug("addPermission: added acl for oid=" + oid.toString()
                    + " perm=" + permission.toString()
                    + " sid=" + recipient.toString());
        }
    }

    private void deletePermissions(ObjectIdentity oid, Permission permission, Sid recipient)
            throws PermissionException {

        int j = 0;
        MutableAcl acl = getAcl(oid);
        List<AccessControlEntry> entries = acl.getEntries();
        if (log.isDebugEnabled()) {
            log.debug("deletePermissions: for acl oid=" + oid.toString()
                    + " found " + entries.size() + " aces");
        }

        for (int i = 0; i < entries.size(); i++) {
            AccessControlEntry ace = entries.get(i);
            if (recipient != null) {
                if (ace.getSid().equals(recipient)
                        && ace.getPermission().equals(permission)) {
                    acl.deleteAce(i);
                    j++;
                }
            } else {
                if (ace.getPermission().equals(permission)) {
                    acl.deleteAce(i);
                    j++;
                }
            }
        }
        provider.getProviderAclService().updateAcl(acl);

        if (log.isDebugEnabled()) {
            log.debug("deletePermissions: for acl oid=" + oid.toString()
                    + " deleted " + j + " aces");
        }
    }

    private MutableAcl getAcl(ObjectIdentity oid) throws PermissionNotFoundException {
        MutableAcl acl = null;
        try {
            acl = (MutableAcl) provider.getProviderAclService().readAclById(oid);
            if (log.isDebugEnabled()) {
                log.debug("found acl for oid=" + oid.toString());
            }
        } catch (NotFoundException nfe) {
            String msg = "Cound not find acl for oid=" + oid.toString();
            log.error(msg);
            throw new PermissionNotFoundException(msg);
        }
        return acl;
    }
}

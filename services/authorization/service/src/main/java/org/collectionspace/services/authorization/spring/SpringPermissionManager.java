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
import org.springframework.security.acls.model.AccessControlEntry;
import org.springframework.security.acls.model.MutableAcl;
import org.springframework.security.acls.model.MutableAclService;
import org.springframework.security.acls.model.NotFoundException;
import org.springframework.security.acls.model.ObjectIdentity;
import org.springframework.security.acls.model.Permission;
import org.springframework.security.acls.model.Sid;

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
    public void addPermission(CSpaceResource res, String[] principals, CSpaceAction perm)
            throws PermissionException {
        ObjectIdentity oid = SpringAuthorizationProvider.mapResource(res);
        Sid[] sids = SpringAuthorizationProvider.mapPrincipal(principals);
        Permission p = SpringAuthorizationProvider.mapPermssion(perm);
        for (Sid sid : sids) {
            addPermission(oid, sid, p);
            if (log.isDebugEnabled()) {
                log.debug("added permission "
                        + " res=" + res.toString()
                        + " cperm=" + perm.toString()
                        + convertToString(principals)
                        + " oid=" + oid.toString()
                        + " perm=" + p.toString()
                        + " sid=" + sids.toString());
            }
        }
    }

    private void addPermission(ObjectIdentity oid, Sid recipient, Permission permission) {
        MutableAcl acl;
        MutableAclService mutableAclService = provider.getProviderAclService();
        try {
            acl = (MutableAcl) mutableAclService.readAclById(oid);
            if (log.isDebugEnabled()) {
                log.debug("addPermission: found acl for oid=" + oid.toString());
            }
        } catch (NotFoundException nfe) {
            acl = mutableAclService.createAcl(oid);
        }

        acl.insertAce(acl.getEntries().size(), permission, recipient, true);
        mutableAclService.updateAcl(acl);
        if (log.isDebugEnabled()) {
            log.debug("addPermission: added acl for oid=" + oid.toString()
                    + " perm=" + permission.toString()
                    + " sid=" + recipient.toString());
        }

    }

    @Override
    public void deletePermission(CSpaceResource res, String[] principals, CSpaceAction perm)
            throws PermissionNotFoundException, PermissionException {
        ObjectIdentity oid = SpringAuthorizationProvider.mapResource(res);
        Sid[] sids = SpringAuthorizationProvider.mapPrincipal(principals);
        Permission p = SpringAuthorizationProvider.mapPermssion(perm);
        for (Sid sid : sids) {
            deletePermission(oid, sid, p);
            if (log.isDebugEnabled()) {
                log.debug("deleted permission "
                        + " res=" + res.toString()
                        + " cperm=" + perm.toString()
                        + convertToString(principals)
                        + " oid=" + oid.toString()
                        + " perm=" + p.toString()
                        + " sid=" + sids.toString());
            }
        }
    }

    private void deletePermission(ObjectIdentity oid, Sid recipient, Permission permission)
            throws PermissionException {

        MutableAclService mutableAclService = provider.getProviderAclService();
        MutableAcl acl = (MutableAcl) mutableAclService.readAclById(oid);
        if (log.isDebugEnabled()) {
            log.debug("deletePermission: found acl for oid=" + oid.toString());
        }
        if (acl == null) {
            String msg = "Cound not find acl for oid=" + oid.toString();
            log.error(msg);
            throw new PermissionNotFoundException(msg);
        }
        // Remove all permissions associated with this particular recipient (string equality to KISS)
        List<AccessControlEntry> entries = acl.getEntries();
        if (log.isDebugEnabled()) {
            log.debug("deletePermission: for acl oid=" + oid.toString()
                    + " found " + entries.size() + " aces");
        }
        for (int i = 0; i < entries.size(); i++) {
            if (entries.get(i).getSid().equals(recipient)
                    && entries.get(i).getPermission().equals(permission)) {
                acl.deleteAce(i);
            }
        }
        mutableAclService.updateAcl(acl);
        if (log.isDebugEnabled()) {
            log.debug("deletePermission: for acl oid=" + oid.toString()
                    + " deleted " + entries.size() + " aces");
        }
    }

    private String convertToString(String[] stra) {
        StringBuilder builder = new StringBuilder();
        for (String s : stra) {
            builder.append(s);
            builder.append(" ");
        }
        return builder.toString();
    }
}

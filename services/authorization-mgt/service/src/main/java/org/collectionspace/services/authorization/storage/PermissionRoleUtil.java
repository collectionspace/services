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
package org.collectionspace.services.authorization.storage;

import java.util.HashMap;
import java.util.List;
import org.collectionspace.services.authorization.PermissionRole;
import org.collectionspace.services.authorization.PermissionRoleRel;
import org.collectionspace.services.authorization.PermissionValue;
import org.collectionspace.services.authorization.RoleValue;
import org.collectionspace.services.authorization.SubjectType;
import org.collectionspace.services.common.context.ServiceContext;
import org.collectionspace.services.common.context.ServiceContextProperties;
import org.collectionspace.services.common.storage.jpa.JpaStorageUtils;

// TODO: Auto-generated Javadoc
/**
 * The Class PermissionRoleUtil.
 *
 * @author
 */
public class PermissionRoleUtil {

    /**
     * Gets the relation subject.
     *
     * @param ctx the ctx
     * @return the relation subject
     */
    static SubjectType getRelationSubject(ServiceContext ctx) {
        Object o = ctx.getProperty(ServiceContextProperties.SUBJECT);
        if (o == null) {
            throw new IllegalArgumentException(ServiceContextProperties.SUBJECT
                    + " property is missing in context "
                    + ctx.toString());
        }
        return (SubjectType) o;
    }

    /**
     * Gets the relation subject.
     *
     * @param ctx the ctx
     * @param pr the pr
     * @return the relation subject
     */
    static SubjectType getRelationSubject(ServiceContext ctx, PermissionRole pr) {
        SubjectType subject = pr.getSubject();
        if (subject == null) {
            //it is not required to give subject as URI determines the subject
            subject = getRelationSubject(ctx);
        }
        return subject;
    }

    /**
     * buildPermissionRoleRel builds persistent relationship entities from given
     * permissionrole.
     *
     * @param pr permissionrole
     * @param subject the subject
     * @param prrl persistent entities built are inserted into this list
     * @param toDelete the to delete
     */
    static public void buildPermissionRoleRel(PermissionRole pr,
    		SubjectType subject,
    		List<PermissionRoleRel> prrl,
    		boolean handleDelete) {
        if (subject.equals(SubjectType.ROLE)) {
            //FIXME: potential index out of bounds exception...negative test needed
            PermissionValue pv = pr.getPermissions().get(0);
            for (RoleValue rv : pr.getRoles()) {
                PermissionRoleRel prr = buildPermissonRoleRel(pv, rv, subject, handleDelete);
                prrl.add(prr);
            }
        } else if (SubjectType.PERMISSION.equals(subject)) {
            //FIXME: potential index out of bounds exception...negative test needed
            RoleValue rv = pr.getRoles().get(0);
            for (PermissionValue pv : pr.getPermissions()) {
                PermissionRoleRel prr = buildPermissonRoleRel(pv, rv, subject, handleDelete);
                prrl.add(prr);
            }
        }
    }

    /**
     * Builds the permisson role rel.
     *
     * @param pv the pv
     * @param rv the rv
     * @param handleDelete the handle delete
     * @return the permission role rel
     */
    static private PermissionRoleRel buildPermissonRoleRel(PermissionValue pv,
    		RoleValue rv,
    		SubjectType subject,
    		boolean handleDelete) {
        PermissionRoleRel prr = new PermissionRoleRel();
        prr.setPermissionId(pv.getPermissionId());
        prr.setPermissionResource(pv.getResourceName());
        prr.setActionGroup(pv.getActionGroup());
        prr.setRoleId(rv.getRoleId());
        prr.setRoleName(rv.getRoleName());
        
        String relationshipId = null;
        if (subject.equals(SubjectType.ROLE) == true) {
        	relationshipId = rv.getRoleRelationshipId();
        } else if (subject.equals(SubjectType.PERMISSION) == true) {
        	relationshipId = pv.getPermRelationshipId();
        }
        if (relationshipId != null && handleDelete == true) {
        	prr.setHjid(Long.parseLong(relationshipId));  // set this so we can convince JPA to del the relation
        }        
        return prr;
    }

    /**
     * Checks if is invalid tenant.
     *
     * @param tenantId the tenant id
     * @param msgBldr the msg bldr
     * @return true, if is invalid tenant
     */
    static boolean isInvalidTenant(String tenantId, StringBuilder msgBldr) {
        boolean invalid = false;

        if (tenantId == null || tenantId.isEmpty()) {
            invalid = true;
            msgBldr.append("\n tenant : tenantId is missing");
        }
        String whereClause = "where id = :id";
        HashMap<String, Object> params = new HashMap<String, Object>();
        params.put("id", tenantId);

        Object tenantFound = JpaStorageUtils.getEntity(
                "org.collectionspace.services.account.Tenant", whereClause, params);
        if (tenantFound == null) {
            invalid = true;
            msgBldr.append("\n tenant : tenantId=" + tenantId
                    + " not found");
        }
        return invalid;
    }
}

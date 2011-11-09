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

import org.collectionspace.services.common.document.DocumentNotFoundException;
import org.collectionspace.services.authorization.perms.Permission;
import org.collectionspace.services.authorization.PermissionRole;
import org.collectionspace.services.authorization.PermissionValue;
import org.collectionspace.services.authorization.Role;
import org.collectionspace.services.authorization.RoleValue;
import org.collectionspace.services.common.ServiceMessages;
import org.collectionspace.services.common.context.ServiceContext;
import org.collectionspace.services.common.document.DocumentHandler.Action;
import org.collectionspace.services.common.document.InvalidDocumentException;
import org.collectionspace.services.common.document.ValidatorHandler;
import org.collectionspace.services.common.storage.jpa.JpaStorageUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * PermissionRoleValidatorHandler executes validation rules for permRole permission permRole
 * @author 
 */
public class PermissionRoleValidatorHandler implements ValidatorHandler {

    final Logger logger = LoggerFactory.getLogger(PermissionRoleValidatorHandler.class);

    @Override
    public void validate(Action action, ServiceContext ctx)
            throws InvalidDocumentException {
        if (logger.isDebugEnabled()) {
            logger.debug("validate() action=" + action.name());
        }
        try {
            PermissionRole permRole = (PermissionRole) ctx.getInput();
            StringBuilder msgBldr = new StringBuilder(ServiceMessages.VALIDATION_FAILURE);
            boolean invalid = false;

            if (action.equals(Action.CREATE)) {

                for (PermissionValue pv : permRole.getPermission()) {
                    if (isPermissionInvalid(pv.getPermissionId(), msgBldr)) {
                        invalid = true;
                    }
                }
                for (RoleValue rv : permRole.getRole()) {
                    if (isRoleInvalid(rv.getRoleId(), msgBldr)) {
                        invalid = true;
                    }
                }
                    }
            if (invalid) {
                String msg = msgBldr.toString();
                logger.error(msg);
                throw new InvalidDocumentException(msg);
            }
        } catch (InvalidDocumentException ide) {
            throw ide;
        } catch (Exception e) {
            throw new InvalidDocumentException(e);
        }
    }

    private boolean isPermissionInvalid(String id, StringBuilder msgBldr)
    		throws DocumentNotFoundException {
        boolean invalid = false;

        if (id == null || id.isEmpty()) {
            invalid = true;
            msgBldr.append("\n permissionId : permissionId is missing");
            return invalid;
        }
        Object permissionFound = JpaStorageUtils.getEntity(id, Permission.class);
        if (permissionFound == null) {
            invalid = true;
            msgBldr.append("\n permissionId : permission for permissionId=" + id
                    + " not found");
        }

        return invalid;
    }

    private boolean isRoleInvalid(String id, StringBuilder msgBldr)
    		throws DocumentNotFoundException {
        boolean invalid = false;

        if (id == null || id.isEmpty()) {
            invalid = true;
            msgBldr.append("\n roleId : roleId is missing");
            return invalid;
        }
        Object roleFound = JpaStorageUtils.getEntity(id, Role.class);
        if (roleFound == null) {
            invalid = true;
            msgBldr.append("\n roleId : role for roleId=" + id
                    + " not found");
        }

        return invalid;
    }
}

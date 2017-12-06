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

import java.util.List;

import org.collectionspace.services.authorization.perms.Permission;
import org.collectionspace.services.authorization.perms.PermissionAction;
import org.collectionspace.services.client.PermissionClient;
import org.collectionspace.services.common.ServiceMessages;
import org.collectionspace.services.common.context.ServiceContext;
import org.collectionspace.services.common.document.DocumentHandler.Action;
import org.collectionspace.services.common.document.InvalidDocumentException;
import org.collectionspace.services.common.document.ValidatorHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * PermissionValidatorHandler executes validation rules for permission
 * @author 
 */
public class PermissionValidatorHandler implements ValidatorHandler {

    final Logger logger = LoggerFactory.getLogger(PermissionValidatorHandler.class);

    @Override
    public void validate(Action action, ServiceContext ctx)
            throws InvalidDocumentException {
        if (logger.isDebugEnabled()) {
            logger.debug("validate() action=" + action.name());
        }
        try {
            Permission permission = (Permission) ctx.getInput();
            StringBuilder msgBldr = new StringBuilder(ServiceMessages.VALIDATION_FAILURE);
            boolean invalid = false;

            if (action.equals(Action.CREATE)) {
                //create specific validation here
                if (permission.getResourceName() == null || permission.getResourceName().isEmpty()) {
                    invalid = true;
                    msgBldr.append("\nThe resource name for creating a new permission resource is missing or empty.");
                } else {
                	invalid = !validateActionFields(permission);
                }
            } else if (action.equals(Action.UPDATE)) {
                //update specific validation here
                if (permission.getResourceName() == null || permission.getResourceName().isEmpty()) {
                    invalid = true;
                    msgBldr.append("\nThe resource name for updating an existing permission is missing or empty.");
                } else {
                	invalid = !validateActionFields(permission);
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

	private boolean validateActionFields(Permission permission) {
		boolean result = true;
		
		List<PermissionAction> permActionList = permission.getAction();
		boolean isPermActionListSet = (permActionList != null && permActionList.size() > 0);
		
		String permActionGroup = permission.getActionGroup();
		boolean isPermActionGroupSet = (permActionGroup != null && !permActionGroup.trim().isEmpty());
		
		if (isPermActionListSet && isPermActionGroupSet) {
			// the two action fields need to match
			String derivedActionGroup = PermissionClient.getActionGroup(permActionList);
			result = derivedActionGroup.equalsIgnoreCase(permActionGroup);
		} else if (isPermActionListSet && !isPermActionGroupSet) {
			// if Action list field is set but actionGroup field is not set then set the actionGroup by deriving it from the Action list
			permission.setActionGroup(PermissionClient.getActionGroup(permActionList));
		} else if (!isPermActionListSet && isPermActionGroupSet) {
			// if the action list field is not set, but the action group is set then set the action actionL
			permission.setAction(PermissionClient.getActionList(permActionGroup));
		} else {
			// both action fields are not set, we don't care.
		}
		
		return result;
	}

}

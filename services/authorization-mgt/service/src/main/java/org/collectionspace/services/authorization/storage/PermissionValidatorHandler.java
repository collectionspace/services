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

import javax.xml.bind.JAXBElement;

import org.collectionspace.services.authorization.perms.Permission;
import org.collectionspace.services.authorization.perms.PermissionAction;
import org.collectionspace.services.client.PermissionClient;
import org.collectionspace.services.common.ServiceMessages;
import org.collectionspace.services.common.context.ServiceContext;
import org.collectionspace.services.common.document.DocumentHandler.Action;
import org.collectionspace.services.common.document.InvalidDocumentException;
import org.collectionspace.services.common.document.JaxbUtils;
import org.collectionspace.services.common.document.ValidatorHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * PermissionValidatorHandler executes validation rules for permission
 * @author 
 */
public class PermissionValidatorHandler implements ValidatorHandler<Permission, Permission> {

    final Logger logger = LoggerFactory.getLogger(PermissionValidatorHandler.class);

    @Override
    public void validate(Action action, ServiceContext<Permission, Permission> ctx)
            throws InvalidDocumentException {
        if (logger.isDebugEnabled()) {
            logger.debug("validate() action=" + action.name());
        }
        try {
            Permission permission = (Permission) ctx.getInput();
            StringBuilder msgBldr = new StringBuilder(ServiceMessages.VALIDATION_FAILURE);
            boolean invalid = false;

            if (action.equals(Action.CREATE)) {
                if (permission.getResourceName() == null || permission.getResourceName().isEmpty()) {
                    invalid = true;
                    msgBldr.append("\nThe resource name for creating a new permission resource is missing or empty.");
                }
            	if (validateActionFields(action, permission) == false) {
                	invalid = true;
                    msgBldr.append("\nAction info is missing or inconsistent.");
                }
            	if (permission.getEffect() == null) {
            		invalid = true;
                    msgBldr.append("\n'effect' elment is missing from the payload or is not set to either PERMIT or DENY.");
            	}
            } else if (action.equals(Action.UPDATE)) {
                if (permission.getResourceName() == null || permission.getResourceName().isEmpty()) {
                    invalid = true;
                    msgBldr.append("\nThe resource name for updating an existing permission is missing or empty.");
                }
            	if (validateActionFields(action, permission) == false) {
                	invalid = true;
                    msgBldr.append("\nAction info is missing or inconsistent.");
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

	private boolean validateActionFields(Action action, Permission permission) {
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
			if (action.equals(Action.CREATE)) {
				result = false;
				org.collectionspace.services.authorization.perms.ObjectFactory objectFactory = 
						new org.collectionspace.services.authorization.perms.ObjectFactory();
				JAXBElement<Permission> permJaxbElement = objectFactory.createPermission(permission);
				String msg = String.format("Either (or both) the 'action' or 'actiongroup' element needs to be set: %s",
						JaxbUtils.toString(permJaxbElement, Permission.class));			
				logger.error(msg);
			}
		}
		
		return result;
	}

}

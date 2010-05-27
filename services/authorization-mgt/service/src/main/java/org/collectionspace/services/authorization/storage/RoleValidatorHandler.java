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

import org.collectionspace.services.authorization.Role;
import org.collectionspace.services.common.ServiceMessages;
import org.collectionspace.services.common.context.ServiceContext;
import org.collectionspace.services.common.document.DocumentHandler.Action;
import org.collectionspace.services.common.document.InvalidDocumentException;
import org.collectionspace.services.common.document.ValidatorHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * RoleValidatorHandler executes validation rules for role
 * @author 
 */
public class RoleValidatorHandler implements ValidatorHandler {

    final Logger logger = LoggerFactory.getLogger(RoleValidatorHandler.class);

    @Override
    public void validate(Action action, ServiceContext ctx)
            throws InvalidDocumentException {
        if (logger.isDebugEnabled()) {
            logger.debug("validate() action=" + action.name());
        }
        try {
            Role role = (Role) ctx.getInput();
            StringBuilder msgBldr = new StringBuilder(ServiceMessages.VALIDATION_FAILURE);
            boolean invalid = false;

            if (action.equals(Action.CREATE)) {

                //create specific validation here
                if (role.getRoleName() == null || role.getRoleName().isEmpty()) {
                    invalid = true;
                    msgBldr.append("\nroleName : missing or empty");
                }
            } else if (action.equals(Action.UPDATE)) {
                //update specific validation here
                if (role.getRoleName() == null || role.getRoleName().isEmpty()) {
                    invalid = true;
                    msgBldr.append("\nroleName : cannot be missing or empty");
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

}

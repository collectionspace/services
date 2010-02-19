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
 *//**
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
/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.collectionspace.services.account.storage;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.collectionspace.services.account.AccountsCommon;
import org.collectionspace.services.common.context.ServiceContext;
import org.collectionspace.services.common.document.DocumentHandler.Action;
import org.collectionspace.services.common.document.InvalidDocumentException;
import org.collectionspace.services.common.document.ValidatorHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author 
 */
public class AccountValidatorHandler implements ValidatorHandler {

    final Logger logger = LoggerFactory.getLogger(AccountValidatorHandler.class);

    @Override
    public void validate(Action action, ServiceContext ctx)
            throws InvalidDocumentException {
        if (logger.isDebugEnabled()) {
            logger.debug("validate() action=" + action.name());
        }
        try {
            AccountsCommon account = (AccountsCommon) ctx.getInput();
            StringBuilder msgBldr = new StringBuilder("validate() ");
            boolean invalid = false;

            if (action.equals(Action.CREATE)) {
                //FIXME tenant would be retrieved from security context once
                //authentication is made mandatory, no need for validation
                List<AccountsCommon.Tenant> tl = account.getTenant();
                if (tl == null || tl.size() == 0) {
                    msgBldr.append("\ntenant : missing information!");
                    invalid = true;
                }
                //create specific validation here
                if (account.getScreenName() == null || account.getScreenName().isEmpty()) {
                    invalid = true;
                    msgBldr.append("\nscreenName : missing");
                }
                if (account.getUserId() == null || account.getUserId().isEmpty()) {
                    invalid = true;
                    msgBldr.append("\nuserId : missing");
                }
                if (account.getEmail() == null || account.getEmail().isEmpty()) {
                    invalid = true;
                    msgBldr.append("\nemail : missing");
                } else {
                    if (invalidEmail(account.getEmail(), msgBldr)) {
                        invalid = true;
                    }
                }
            } else if (action.equals(Action.UPDATE)) {
                //update specific validation here
                if (account.getScreenName() != null && account.getScreenName().isEmpty()) {
                    invalid = true;
                    msgBldr.append("\nscreenName : cannot be changed!");
                }
                if (account.getPassword() != null
                        && (account.getUserId() == null || account.getUserId().isEmpty())) {
                    invalid = true;
                    msgBldr.append("\npassword : userId is needed");
                }
                if (account.getEmail() != null) {
                    if (invalidEmail(account.getEmail(), msgBldr)) {
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

    private boolean invalidEmail(String email, StringBuilder msgBldr) {
        boolean invalid = false;
        Pattern p = Pattern.compile("^[_A-Za-z0-9-]+(\\.[_A-Za-z0-9-]+)*@[A-Za-z0-9-]+(\\.[A-Za-z0-9-]+)*(\\.[_A-Za-z0-9-]+)");
        Matcher m = p.matcher(email);
        if (!m.find()) {
            invalid = true;
            msgBldr.append("\nemail : invalid " + email);
        }
        return invalid;
    }
}

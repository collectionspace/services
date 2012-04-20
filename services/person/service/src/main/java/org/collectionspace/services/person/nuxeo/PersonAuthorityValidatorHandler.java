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
package org.collectionspace.services.person.nuxeo;

import java.util.regex.Pattern;

import org.collectionspace.services.person.PersonauthoritiesCommon;
import org.collectionspace.services.common.context.MultipartServiceContext;
import org.collectionspace.services.common.context.ServiceContext;
import org.collectionspace.services.common.document.DocumentHandler.Action;
import org.collectionspace.services.common.document.InvalidDocumentException;
import org.collectionspace.services.common.document.ValidatorHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * PersonAuthorityValidatorHandler
 * 
 * Validates data supplied when attempting to create and/or update PersonAuthority records.
 * 
 * $LastChangedRevision: $
 * $LastChangedDate: $
 */
public class PersonAuthorityValidatorHandler implements ValidatorHandler {

    final Logger logger = LoggerFactory.getLogger(PersonAuthorityValidatorHandler.class);
    private static final Pattern shortIdBadPattern = Pattern.compile("[\\W]"); //.matcher(input).matches()

    @Override
    public void validate(Action action, ServiceContext ctx)
            throws InvalidDocumentException {
        if (logger.isDebugEnabled()) {
            logger.debug("validate() action=" + action.name());
        }
        
        // Bail out if the validation action is for delete.
        if (action.equals(Action.DELETE)) {
        	return;
        }
        
        try {
            MultipartServiceContext mctx = (MultipartServiceContext) ctx;
            PersonauthoritiesCommon personAuth =
                    (PersonauthoritiesCommon) mctx.getInputPart(mctx.getCommonPartLabel(),
                    PersonauthoritiesCommon.class);
            String msg = "";
            boolean invalid = false;

            if(personAuth != null) {	// No guarantee that there is a common part in every post/update.
	            // Validation specific to creates or updates
	            if (action.equals(Action.CREATE)) {
	                String shortId = personAuth.getShortIdentifier();
	                // Per CSPACE-2215, shortIdentifier values that are null (missing)
	                // oe the empty string are now legally accepted in create payloads.
	                // In either of those cases, a short identifier will be synthesized from
	                // a display name or supplied in another manner.
	                if ((shortId != null) && (shortIdBadPattern.matcher(shortId).find())) {
	                    invalid = true;
	                    msg += "shortIdentifier must only contain standard word characters";
	                }
	            } else if (action.equals(Action.UPDATE)) {
	            }
            }

            if (invalid) {
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

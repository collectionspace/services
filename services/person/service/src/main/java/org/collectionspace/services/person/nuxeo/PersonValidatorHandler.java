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
import org.collectionspace.services.person.PersonsCommon;
import org.collectionspace.services.common.context.MultipartServiceContext;
import org.collectionspace.services.common.context.ServiceContext;
import org.collectionspace.services.common.document.DocumentHandler.Action;
import org.collectionspace.services.common.document.InvalidDocumentException;
import org.collectionspace.services.common.document.ValidatorHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * PersonValidatorHandler
 * 
 * Validates data supplied when attempting to create and/or update Person records.
 * 
 * $LastChangedRevision: $
 * $LastChangedDate: $
 */
public class PersonValidatorHandler implements ValidatorHandler {

    final Logger logger = LoggerFactory.getLogger(PersonValidatorHandler.class);
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
            PersonsCommon person = (PersonsCommon) mctx.getInputPart(mctx.getCommonPartLabel(),
                    PersonsCommon.class);
            String msg = "";
            boolean invalid = false;
            
            if(person != null) {	// No guarantee that there is a common part in every post/update.
                
	            // Validation occurring on both creates and updates
                
                    // FIXME Add validation logic here to ensure that every term info group must contain
                    // any required data elements.  (Potential example: every term info group must contain
                    // a non-null, non-whitespace-only value in either of the term or displayName fields.)
                
                    /*
	            String displayName = person.getDisplayName();
	            if (!person.isDisplayNameComputed() && ((displayName == null) || displayName.trim().isEmpty())) {
	                invalid = true;
	                msg += "displayName must be non-null and non-blank if displayNameComputed is false!";
	            }
                    * 
                    */
	            
	            // Validation specific to creates or updates
	            if (action.equals(Action.CREATE)) {
	                String shortId = person.getShortIdentifier();
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

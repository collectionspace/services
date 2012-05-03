/**
 * This document is a part of the source code and related artifacts for
 * CollectionSpace, an open source collections management system for museums and
 * related institutions:
 *
 * http://www.collectionspace.org http://wiki.collectionspace.org
 *
 * Copyright 2009 University of California at Berkeley
 *
 * Licensed under the Educational Community License (ECL), Version 2.0. You may
 * not use this file except in compliance with this License.
 *
 * You may obtain a copy of the ECL 2.0 License at
 *
 * https://source.collectionspace.org/collection-space/LICENSE.txt
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package org.collectionspace.services.person.nuxeo;

import java.util.List;
import java.util.regex.Pattern;
import org.collectionspace.services.common.api.Tools;
import org.collectionspace.services.person.PersonsCommon;
import org.collectionspace.services.common.context.MultipartServiceContext;
import org.collectionspace.services.common.context.ServiceContext;
import org.collectionspace.services.common.document.DocumentHandler.Action;
import org.collectionspace.services.common.document.InvalidDocumentException;
import org.collectionspace.services.common.document.ValidatorHandler;
import org.collectionspace.services.person.PersonTermGroup;
import org.collectionspace.services.person.PersonTermGroupList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * PersonValidatorHandler
 *
 * Validates data supplied when attempting to create and/or update Person
 * records.
 *
 * $LastChangedRevision: $ $LastChangedDate: $
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

            if (person != null) {	// No guarantee that there is a common part in every post/update.

                // Validation occurring on both creates and updates

                /*
                 * String displayName = person.getDisplayName(); if
                 * (!person.isDisplayNameComputed() && ((displayName == null) ||
                 * displayName.trim().isEmpty())) { invalid = true; msg +=
                 * "displayName must be non-null and non-blank if
                 * displayNameComputed is false!"; }
                 *
                 */

                if (!containsAtLeastOneTerm(person)) {
                    invalid = true;
                    msg += "Authority items must contain at least one term.";
                }

                if (!allTermsContainNameOrDisplayName(person)) {
                    invalid = true;
                    msg += "Each term group in an authority item must contain "
                            + "a non-empty term name or "
                            + "a non-empty term display name.";
                }

                // Validation specific to creates or updates
                if (action.equals(Action.CREATE)) {

                    // shortIdentifier value must contain only word characters
                    String shortId = person.getShortIdentifier();
                    if ((shortId != null) && (shortIdBadPattern.matcher(shortId).find())) {
                        invalid = true;
                        msg += "shortIdentifier must only contain standard word characters";
                    }

                    // Note: Per CSPACE-2215, shortIdentifier values that are null (missing)
                    // or the empty string are now legally accepted in create payloads.
                    // In either of those cases, a short identifier will be synthesized from
                    // a display name or supplied in another manner.

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

    private boolean containsAtLeastOneTerm(PersonsCommon person) {
        PersonTermGroupList termGroupList = person.getPersonTermGroupList();
        if (termGroupList == null) {
            return false;
        }
        List<PersonTermGroup> termGroups = termGroupList.getPersonTermGroup();
        if ((termGroups == null) || (termGroups.size() == 0)) {
            return false;
        }
        return true;
    }

    private boolean allTermsContainNameOrDisplayName(PersonsCommon person) {
        PersonTermGroupList termGroupList = person.getPersonTermGroupList();
        List<PersonTermGroup> termGroups = termGroupList.getPersonTermGroup();
        for (PersonTermGroup termGroup : termGroups) {
            if (Tools.isBlank(termGroup.getTermName()) || Tools.isBlank(termGroup.getTermDisplayName()) ){
                return false;
            }
        }
        return true;
    }
    
}

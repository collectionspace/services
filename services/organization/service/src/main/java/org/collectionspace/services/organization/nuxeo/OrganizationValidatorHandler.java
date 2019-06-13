/**
 *  This document is a part of the source code and related artifacts
 *  for CollectionSpace, an open source collections management system
 *  for museums and related institutions:

 *  http://www.collectionspace.org
 *  http://wiki.collectionspace.org

 *  Copyright 2009 University of California, Berkeley

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
package org.collectionspace.services.organization.nuxeo;

import java.util.List;
import java.util.regex.Pattern;
import org.collectionspace.services.common.api.Tools;
import org.collectionspace.services.common.document.InvalidDocumentException;
import org.collectionspace.services.common.document.ValidatorHandlerImpl;
import org.collectionspace.services.organization.OrgTermGroup;
import org.collectionspace.services.organization.OrgTermGroupList;
import org.collectionspace.services.organization.OrganizationsCommon;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * OrganizationValidatorHandler
 * 
 * Performs validation when making requests related to Organization records.
 * As an example, you can modify this class to customize validation of
 * payloads supplied in requests to create and/or update records.
 */
public class OrganizationValidatorHandler extends ValidatorHandlerImpl {

    final Logger logger = LoggerFactory.getLogger(OrganizationValidatorHandler.class);
    // 'Bad pattern' for shortIdentifiers matches any non-word characters
    private static final Pattern SHORT_ID_BAD_PATTERN = Pattern.compile("[\\W]");
    private static final String SHORT_ID_BAD_CHARS_ERROR =
            "shortIdentifier must only contain standard word characters";
    private static final String HAS_NO_TERMS_ERROR =
            "Authority items must contain at least one term.";
    private static final String TERM_HAS_EMPTY_DISPLAYNAME_ERROR =
            "Each term group in an authority item must contain "
            + "a non-empty display name.";

    @Override
    protected Class getCommonPartClass() {
        return OrganizationsCommon.class;
    }

    @Override
    protected void handleCreate() throws InvalidDocumentException {
        OrganizationsCommon organization = (OrganizationsCommon) getCommonPart();
        // No guarantee that there is a common part in every post/update.
        if (organization != null) {
            try {
                String shortId = organization.getShortIdentifier();
                if (shortId != null) {
                    String msg = String.format("%s: %s", SHORT_ID_BAD_CHARS_ERROR, shortId);
                    CS_ASSERT(shortIdentifierContainsOnlyValidChars(shortId), msg);
                }
                CS_ASSERT(containsAtLeastOneTerm(organization), HAS_NO_TERMS_ERROR);
                CS_ASSERT(allTermsContainDisplayName(organization), TERM_HAS_EMPTY_DISPLAYNAME_ERROR);
            } catch (AssertionError e) {
                if (logger.isErrorEnabled()) {
                    logger.error(e.getMessage(), e);
                }
                throw new InvalidDocumentException(e.getMessage(), e);
            }
        }
    }

    @Override
    protected void handleGet() throws InvalidDocumentException {
    }

    @Override
    protected void handleGetAll() throws InvalidDocumentException {
    }

    @Override
    protected void handleUpdate() throws InvalidDocumentException {
        OrganizationsCommon organization = (OrganizationsCommon) getCommonPart();
        // No guarantee that there is a common part in every post/update.
        if (organization != null) {
            try {
                // shortIdentifier is among a set of fields that are
                // prevented from being changed on an update, and thus
                // we don't need to check its value here.
                CS_ASSERT(containsAtLeastOneTerm(organization), HAS_NO_TERMS_ERROR);
                CS_ASSERT(allTermsContainDisplayName(organization), TERM_HAS_EMPTY_DISPLAYNAME_ERROR);
            } catch (AssertionError e) {
                if (logger.isErrorEnabled()) {
                    logger.error(e.getMessage(), e);
                }
                throw new InvalidDocumentException(e.getMessage(), e);
            }
        }
    }

    @Override
    protected void handleDelete() throws InvalidDocumentException {
    }

    private boolean shortIdentifierContainsOnlyValidChars(String shortId) {
        // Check whether any characters match the 'bad' pattern
        if (SHORT_ID_BAD_PATTERN.matcher(shortId).find()) {
            return false;
        }
        return true;
    }

    private boolean containsAtLeastOneTerm(OrganizationsCommon organization) {
        OrgTermGroupList termGroupList = organization.getOrgTermGroupList();
        if (termGroupList == null) {
            return false;
        }
        List<OrgTermGroup> termGroups = termGroupList.getOrgTermGroup();
        if ((termGroups == null) || (termGroups.isEmpty())){ 
            return false;
        }
        return true;
    }

    private boolean allTermsContainDisplayName(OrganizationsCommon organization) {
        OrgTermGroupList termGroupList = organization.getOrgTermGroupList();
        List<OrgTermGroup> termGroups = termGroupList.getOrgTermGroup();
        for (OrgTermGroup termGroup : termGroups) {
            if (Tools.isBlank(termGroup.getTermDisplayName())) {
                return false;
            }
        }
        return true;
    }
}

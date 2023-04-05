/**
 * This document is a part of the source code and related artifacts
 * for CollectionSpace, an open source collections management system
 * for museums and related institutions:
 *
 * http://www.collectionspace.org
 * http://wiki.collectionspace.org
 *
 * Licensed under the Educational Community License (ECL), Version 2.0.
 * You may not use this file except in compliance with this License.
 *
 * You may obtain a copy of the ECL 2.0 License at
 * https://source.collectionspace.org/collection-space/LICENSE.txt
 */
package org.collectionspace.services.chronology.nuxeo;

import java.util.List;
import java.util.regex.Pattern;

import org.collectionspace.services.chronology.ChronologiesCommon;
import org.collectionspace.services.chronology.ChronologyTermGroup;
import org.collectionspace.services.chronology.ChronologyTermGroupList;
import org.collectionspace.services.common.api.Tools;
import org.collectionspace.services.common.document.InvalidDocumentException;
import org.collectionspace.services.common.document.ValidatorHandlerImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * ChronologyValidatorHandler
 */
public class ChronologyValidatorHandler<IT, OT> extends ValidatorHandlerImpl<IT, OT> {

    final Logger logger = LoggerFactory.getLogger(ChronologyValidatorHandler.class);

    /**
     * 'Bad pattern' for shortIdentifiers matches any non-word characters
     */
    private static final Pattern SHORT_ID_BAD_PATTERN = Pattern.compile("[\\W]");
    private static final String SHORT_ID_BAD_CHARS_ERROR =
        "shortIdentifier must only contain standard word characters";
    private static final String HAS_NO_TERMS_ERROR =
        "Authority items must contain at least one term.";
    private static final String TERM_HAS_EMPTY_DISPLAYNAME_ERROR =
        "Each term group in an authority item must contain "
        + "a non-empty display name.";


    @Override
    protected Class<?> getCommonPartClass() {
        return null;
    }

    @Override
    protected void handleCreate() throws InvalidDocumentException {
        final ChronologiesCommon chronology = (ChronologiesCommon) getCommonPart();
        // No guarantee that there is a common part in every post/update.
        if (chronology != null) {
            try {
                final String shortId = chronology.getShortIdentifier();
                if (shortId != null) {
                    CS_ASSERT(shortIdentifierContainsOnlyValidChars(shortId), SHORT_ID_BAD_CHARS_ERROR);
                }
                CS_ASSERT(containsAtLeastOneTerm(chronology), HAS_NO_TERMS_ERROR);
                CS_ASSERT(allTermsContainDisplayName(chronology), TERM_HAS_EMPTY_DISPLAYNAME_ERROR);
            } catch (AssertionError e) {
                logger.error(e.getMessage(), e);
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
        final ChronologiesCommon chronology = (ChronologiesCommon) getCommonPart();
        // No guarantee that there is a common part in every post/update.
        if (chronology != null) {
            try {
                // shortIdentifier is among a set of fields that are
                // prevented from being changed on an update, and thus
                // we don't need to check its value here.
                CS_ASSERT(containsAtLeastOneTerm(chronology), HAS_NO_TERMS_ERROR);
                CS_ASSERT(allTermsContainDisplayName(chronology), TERM_HAS_EMPTY_DISPLAYNAME_ERROR);
            } catch (AssertionError e) {
                logger.error(e.getMessage(), e);
                throw new InvalidDocumentException(e.getMessage(), e);
            }
        }
    }

    @Override
    protected void handleDelete() throws InvalidDocumentException {
    }


    private boolean shortIdentifierContainsOnlyValidChars(final String shortId) {
        // Check whether any characters match the 'bad' pattern
        return !SHORT_ID_BAD_PATTERN.matcher(shortId).find();
    }

    private boolean containsAtLeastOneTerm(final ChronologiesCommon chronology) {
        final ChronologyTermGroupList termGroupList = chronology.getChronologyTermGroupList();
        if (termGroupList == null) {
            return false;
        }
        final List<ChronologyTermGroup> termGroups = termGroupList.getChronologyTermGroup();
        return termGroups != null && !termGroups.isEmpty();
    }

    private boolean allTermsContainDisplayName(final ChronologiesCommon chronology) {
        final ChronologyTermGroupList termGroupList = chronology.getChronologyTermGroupList();
        final List<ChronologyTermGroup> termGroups = termGroupList.getChronologyTermGroup();
        for (ChronologyTermGroup termGroup : termGroups) {
            if (Tools.isBlank(termGroup.getTermDisplayName())) {
                return false;
            }
        }
        return true;
    }
}

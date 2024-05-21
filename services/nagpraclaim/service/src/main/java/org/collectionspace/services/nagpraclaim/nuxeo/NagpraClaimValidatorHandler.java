/*
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
package org.collectionspace.services.nagpraclaim.nuxeo;

import org.collectionspace.services.client.PoxPayloadIn;
import org.collectionspace.services.client.PoxPayloadOut;
import org.collectionspace.services.common.document.InvalidDocumentException;
import org.collectionspace.services.common.document.ValidatorHandlerImpl;
import org.collectionspace.services.nagpraclaim.NagpraclaimsCommon;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Validation handler for NagpraClaim. Checks for the common part and claimNumber on create.
 */
public class NagpraClaimValidatorHandler extends ValidatorHandlerImpl<PoxPayloadIn, PoxPayloadOut> {

    private static final String COMMON_PART_MISSING = "Validation exception: nagpraclaims_common part is missing";
    private static final String CLAIM_NUMBER_MISSING =
            "Validation exception: The nagpra claim field \"claimNumber\" cannot be empty or missing";

    private final Logger logger = LoggerFactory.getLogger(NagpraClaimValidatorHandler.class);

    @Override
    protected Class<?> getCommonPartClass() {
        return NagpraclaimsCommon.class;
    }

    @Override
    protected void handleCreate() throws InvalidDocumentException {
        final NagpraclaimsCommon claim = (NagpraclaimsCommon) getCommonPart();
        if (claim == null) {
            logger.error(COMMON_PART_MISSING);
            throw new InvalidDocumentException(COMMON_PART_MISSING);
        }

        final String claimNumber = claim.getClaimNumber();
        if (claimNumber == null || claimNumber.isEmpty()) {
            logger.error(CLAIM_NUMBER_MISSING);
            throw new InvalidDocumentException(CLAIM_NUMBER_MISSING);
        }
    }

    @Override
    protected void handleGet() {}

    @Override
    protected void handleGetAll() {}

    @Override
    protected void handleUpdate() {}

    @Override
    protected void handleDelete() {}
}

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
package org.collectionspace.services.heldintrust.nuxeo;

import org.collectionspace.services.client.PoxPayloadIn;
import org.collectionspace.services.client.PoxPayloadOut;
import org.collectionspace.services.common.document.InvalidDocumentException;
import org.collectionspace.services.common.document.ValidatorHandlerImpl;
import org.collectionspace.services.heldintrust.HeldInTrustsCommon;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HeldInTrustValidatorHandler extends ValidatorHandlerImpl<PoxPayloadIn, PoxPayloadOut> {

    private static final String COMMON_PART_MISSING = "Validation exception: heldintrusts_common part is missing";
    private static final String HELDINTRUST_NUMBER_MISSING =
            "Validation exception: The heldintrust field \"heldInTrustNumber\" cannot be empty or missing";

    private final Logger logger = LoggerFactory.getLogger(HeldInTrustValidatorHandler.class);

    @Override
    protected Class<?> getCommonPartClass() {
        return HeldInTrustsCommon.class;
    }

    @Override
    protected void handleCreate() throws InvalidDocumentException {
        final HeldInTrustsCommon heldInTrust = (HeldInTrustsCommon) getCommonPart();
        if (heldInTrust == null) {
            logger.error(COMMON_PART_MISSING);
            throw new InvalidDocumentException(COMMON_PART_MISSING);
        }

        final String heldInTrustNumber = heldInTrust.getHeldInTrustNumber();
        if (heldInTrustNumber == null || heldInTrustNumber.isEmpty()) {
            logger.error(HELDINTRUST_NUMBER_MISSING);
            throw new InvalidDocumentException(HELDINTRUST_NUMBER_MISSING);
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

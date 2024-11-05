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
package org.collectionspace.services.repatriationrequest.nuxeo;

import org.collectionspace.services.client.PoxPayloadIn;
import org.collectionspace.services.client.PoxPayloadOut;
import org.collectionspace.services.common.document.InvalidDocumentException;
import org.collectionspace.services.common.document.ValidatorHandlerImpl;
import org.collectionspace.services.repatriationrequest.RepatriationRequestsCommon;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Validation handler for a RepatriationRequest. Checks for the common part and requestNumber on create.
 */
public class RepatriationRequestValidatorHandler extends ValidatorHandlerImpl<PoxPayloadIn, PoxPayloadOut> {

    private static final String COMMON_PART_MISSING = "Validation exception: repatriationrequests_common part is missing";
    private static final String CLAIM_NUMBER_MISSING =
            "Validation exception: The repatriation request field \"requestNumber\" cannot be empty or missing";

    private final Logger logger = LoggerFactory.getLogger(RepatriationRequestValidatorHandler.class);

    @Override
    protected Class<?> getCommonPartClass() {
        return RepatriationRequestsCommon.class;
    }

    @Override
    protected void handleCreate() throws InvalidDocumentException {
        final RepatriationRequestsCommon request = (RepatriationRequestsCommon) getCommonPart();
        if (request == null) {
            logger.error(COMMON_PART_MISSING);
            throw new InvalidDocumentException(COMMON_PART_MISSING);
        }

        final String requestNumber = request.getRequestNumber();
        if (requestNumber == null || requestNumber.isEmpty()) {
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

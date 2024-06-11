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
package org.collectionspace.services.summarydocumentation.nuxeo;

import org.collectionspace.services.client.PoxPayloadIn;
import org.collectionspace.services.client.PoxPayloadOut;
import org.collectionspace.services.common.document.InvalidDocumentException;
import org.collectionspace.services.common.document.ValidatorHandlerImpl;
import org.collectionspace.services.summarydocumentation.SummaryDocumentationsCommon;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SummaryDocumentationValidatorHandler extends ValidatorHandlerImpl<PoxPayloadIn, PoxPayloadOut> {

    private static final String COMMON_PART_MISSING =
            "Validation exception: summarydocumentations_common part is missing";
    private static final String DOCUMENTATION_NUMBER_MISSING =
            "Validation exception: The summary documentation field \"documentationNumber\" cannot be empty or missing";

    private final Logger logger = LoggerFactory.getLogger(SummaryDocumentationValidatorHandler.class);

    @Override
    protected Class<?> getCommonPartClass() {
        return SummaryDocumentationsCommon.class;
    }

    @Override
    protected void handleCreate() throws InvalidDocumentException {
        final SummaryDocumentationsCommon summary = (SummaryDocumentationsCommon) getCommonPart();
        if (summary == null) {
            logger.error(COMMON_PART_MISSING);
            throw new InvalidDocumentException(COMMON_PART_MISSING);
        }

        final String documentationNumber = summary.getDocumentationNumber();
        if (documentationNumber == null || documentationNumber.isEmpty()) {
            logger.error(DOCUMENTATION_NUMBER_MISSING);
            throw new InvalidDocumentException(DOCUMENTATION_NUMBER_MISSING);
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

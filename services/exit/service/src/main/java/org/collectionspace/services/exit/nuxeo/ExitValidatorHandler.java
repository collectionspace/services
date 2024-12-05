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
package org.collectionspace.services.exit.nuxeo;

import org.collectionspace.services.client.PoxPayloadIn;
import org.collectionspace.services.client.PoxPayloadOut;
import org.collectionspace.services.common.document.InvalidDocumentException;
import org.collectionspace.services.common.document.ValidatorHandlerImpl;
import org.collectionspace.services.exit.ExitsCommon;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Validator Handler for the new Object Exit procedure. Only handles CREATE operations, checking that the common part
 * exists and the exitNumber is set.
 */
public class ExitValidatorHandler extends ValidatorHandlerImpl<PoxPayloadIn, PoxPayloadOut> {

    private static final String COMMON_PART_MISSING = "Validation exception: exits_common part is missing";
    private static final String REFERENCE_NUMBER_MISSING =
            "Validation exception: The exit field \"exitNumber\" cannot be empty or missing";

    private final Logger logger = LoggerFactory.getLogger(ExitValidatorHandler.class);

    @Override
    protected Class<?> getCommonPartClass() {
        return ExitsCommon.class;
    }

    @Override
    protected void handleCreate() throws InvalidDocumentException {
        final ExitsCommon common = (ExitsCommon) getCommonPart();
        if (common == null) {
            logger.error(COMMON_PART_MISSING);
            throw new InvalidDocumentException(COMMON_PART_MISSING);
        }

        final String exitNumber = common.getExitNumber();
        if (exitNumber == null || exitNumber.isEmpty()) {
            logger.error(REFERENCE_NUMBER_MISSING);
            throw new InvalidDocumentException(REFERENCE_NUMBER_MISSING);
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

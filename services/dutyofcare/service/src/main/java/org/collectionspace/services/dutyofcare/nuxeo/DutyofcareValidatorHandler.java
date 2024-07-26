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
package org.collectionspace.services.dutyofcare.nuxeo;

import org.collectionspace.services.client.PoxPayloadIn;
import org.collectionspace.services.client.PoxPayloadOut;
import org.collectionspace.services.common.document.InvalidDocumentException;
import org.collectionspace.services.common.document.ValidatorHandlerImpl;
import org.collectionspace.services.dutyofcare.DutiesOfCareCommon;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DutyofcareValidatorHandler extends ValidatorHandlerImpl<PoxPayloadIn, PoxPayloadOut> {

    private final Logger logger = LoggerFactory.getLogger(DutyofcareValidatorHandler.class);

    private static final String COMMON_PART_MISSING = "Validation exception: dutiesofcare_common part is missing";
    private static final String DOCUMENTATION_NUMBER_MISSING =
            "Validation exception: The duty of care field \"dutyOfCareNumber\" cannot be empty or missing";

    @Override
    protected Class<?> getCommonPartClass() {
        return DutiesOfCareCommon.class;
    }

    @Override
    protected void handleCreate() throws InvalidDocumentException {
        final DutiesOfCareCommon common = (DutiesOfCareCommon) getCommonPart();
        if (common == null) {
            logger.error(COMMON_PART_MISSING);
            throw new InvalidDocumentException(COMMON_PART_MISSING);
        }

        final String dutyOfCareNumber = common.getDutyOfCareNumber();
        if (dutyOfCareNumber == null || dutyOfCareNumber.isEmpty()) {
            logger.error(DOCUMENTATION_NUMBER_MISSING);
            throw new InvalidDocumentException(DOCUMENTATION_NUMBER_MISSING);
        }
    }

    @Override
    protected void handleGet() throws InvalidDocumentException {}

    @Override
    protected void handleGetAll() throws InvalidDocumentException {}

    @Override
    protected void handleUpdate() throws InvalidDocumentException {}

    @Override
    protected void handleDelete() throws InvalidDocumentException {}
}

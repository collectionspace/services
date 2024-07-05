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
package org.collectionspace.services.consultation.nuxeo;

import org.collectionspace.services.client.PoxPayloadIn;
import org.collectionspace.services.client.PoxPayloadOut;
import org.collectionspace.services.common.document.InvalidDocumentException;
import org.collectionspace.services.common.document.ValidatorHandlerImpl;
import org.collectionspace.services.consultation.ConsultationsCommon;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConsultationValidatorHandler extends ValidatorHandlerImpl<PoxPayloadIn, PoxPayloadOut> {

    private static final Logger logger = LoggerFactory.getLogger(ConsultationValidatorHandler.class);

    private static final String COMMON_PART_MISSING = "Validation exception: nagprainventories_common part is missing";
    private static final String CONSULTATION_NUMBER_MISSING =
            "Validation exception: The consultation field \"consultationNumber\" cannot be empty or missing";

    @Override
    protected Class<?> getCommonPartClass() {
        return ConsultationsCommon.class;
    }

    @Override
    protected void handleCreate() throws InvalidDocumentException {
        final ConsultationsCommon consultation = (ConsultationsCommon) getCommonPart();
        if (consultation == null) {
            logger.error(COMMON_PART_MISSING);
            throw new InvalidDocumentException(COMMON_PART_MISSING);
        }

        final String consultationNumber = consultation.getConsultationNumber();
        if (consultationNumber == null || consultationNumber.isEmpty()) {
            logger.error(CONSULTATION_NUMBER_MISSING);
            throw new InvalidDocumentException(CONSULTATION_NUMBER_MISSING);
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

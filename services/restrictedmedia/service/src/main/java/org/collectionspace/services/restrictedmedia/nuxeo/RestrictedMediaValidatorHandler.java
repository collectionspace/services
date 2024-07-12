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
package org.collectionspace.services.restrictedmedia.nuxeo;

import org.collectionspace.services.client.PoxPayloadIn;
import org.collectionspace.services.client.PoxPayloadOut;
import org.collectionspace.services.common.document.InvalidDocumentException;
import org.collectionspace.services.common.document.ValidatorHandlerImpl;
import org.collectionspace.services.restrictedmedia.RestrictedMediaCommon;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * ValidatorHandler for RestrictedMedia
 *
 * Checks for presence of restrictedmedia_common and restrictedmedia_common/identificationNumber
 */
public class RestrictedMediaValidatorHandler extends ValidatorHandlerImpl<PoxPayloadIn, PoxPayloadOut> {
    private final Logger logger = LoggerFactory.getLogger(RestrictedMediaValidatorHandler.class);

    private static final String COMMON_PART_MISSING =
        "Validation exception: restrictedmedia_common part is missing";
    private static final String IDENTIFICATION_NUMBER_MISSING =
        "Validation exception: The restricted media field \"identificationNumber\" cannot be empty or missing";

    @Override
    protected Class<?> getCommonPartClass() {
        return RestrictedMediaCommon.class;
    }

    @Override
    protected void handleCreate() throws InvalidDocumentException {
        final RestrictedMediaCommon restrictedMedia = (RestrictedMediaCommon) getCommonPart();
        if (restrictedMedia == null) {
            logger.error(COMMON_PART_MISSING);
            throw new InvalidDocumentException(COMMON_PART_MISSING);
        }

        final String identificationNumber = restrictedMedia.getIdentificationNumber();
        if (identificationNumber == null || identificationNumber.isEmpty()) {
            logger.error(IDENTIFICATION_NUMBER_MISSING);
            throw new InvalidDocumentException(IDENTIFICATION_NUMBER_MISSING);
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

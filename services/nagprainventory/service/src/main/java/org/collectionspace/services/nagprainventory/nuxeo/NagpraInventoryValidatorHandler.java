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
package org.collectionspace.services.nagprainventory.nuxeo;

import org.collectionspace.services.client.PoxPayloadIn;
import org.collectionspace.services.client.PoxPayloadOut;
import org.collectionspace.services.common.document.InvalidDocumentException;
import org.collectionspace.services.common.document.ValidatorHandlerImpl;
import org.collectionspace.services.nagprainventory.NagpraInventoriesCommon;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Validation handler for NagpraInventory. Checks for the common part and inventoryNumber on create.
 */
public class NagpraInventoryValidatorHandler extends ValidatorHandlerImpl<PoxPayloadIn, PoxPayloadOut> {

    private static final Logger logger = LoggerFactory.getLogger(NagpraInventoryValidatorHandler.class);

    private static final String COMMON_PART_MISSING = "Validation exception: nagprainventories_common part is missing";
    private static final String INVENTORY_NUMBER_MISSING =
            "Validation exception: The nagpra inventory field \"inventoryNumber\" cannot be empty or missing";

    @Override
    protected Class<?> getCommonPartClass() {
        return NagpraInventoriesCommon.class;
    }

    @Override
    protected void handleCreate() throws InvalidDocumentException {
        final NagpraInventoriesCommon inventory = (NagpraInventoriesCommon) getCommonPart();
        if (inventory == null) {
            logger.error(COMMON_PART_MISSING);
            throw new InvalidDocumentException(COMMON_PART_MISSING);
        }

        final String inventoryNumber = inventory.getInventoryNumber();
        if (inventoryNumber == null || inventoryNumber.isEmpty()) {
            logger.error(INVENTORY_NUMBER_MISSING);
            throw new InvalidDocumentException(INVENTORY_NUMBER_MISSING);
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

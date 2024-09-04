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
package org.collectionspace.services.deaccession.nuxeo;

import org.collectionspace.services.client.PoxPayloadIn;
import org.collectionspace.services.client.PoxPayloadOut;
import org.collectionspace.services.common.document.InvalidDocumentException;
import org.collectionspace.services.common.document.ValidatorHandlerImpl;

public class DeaccessionValidatorHandler extends ValidatorHandlerImpl<PoxPayloadIn, PoxPayloadOut> {

    @Override
    protected Class<?> getCommonPartClass() {
        return null;
    }

    @Override
    protected void handleCreate() throws InvalidDocumentException {

    }

    @Override
    protected void handleGet() throws InvalidDocumentException {

    }

    @Override
    protected void handleGetAll() throws InvalidDocumentException {

    }

    @Override
    protected void handleUpdate() throws InvalidDocumentException {

    }

    @Override
    protected void handleDelete() throws InvalidDocumentException {

    }
}

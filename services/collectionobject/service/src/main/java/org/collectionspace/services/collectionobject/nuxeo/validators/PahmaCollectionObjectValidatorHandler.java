package org.collectionspace.services.collectionobject.nuxeo;

import org.collectionspace.services.common.document.InvalidDocumentException;

public class PahmaCollectionObjectValidatorHandler extends CollectionObjectValidatorHandler {

	@Override
	protected void handleUpdate() throws InvalidDocumentException {
        // PAHMA-473: Disable non-empty objectNumber requirement, so that updates don't need to retrieve the current objectNumber.

	}
}

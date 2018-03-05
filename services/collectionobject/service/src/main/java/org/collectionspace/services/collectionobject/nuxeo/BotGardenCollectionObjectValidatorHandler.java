package org.collectionspace.services.collectionobject.nuxeo;

import org.collectionspace.services.common.document.InvalidDocumentException;

public class BotGardenCollectionObjectValidatorHandler extends CollectionObjectValidatorHandler {

	@Override
	protected void handleUpdate() throws InvalidDocumentException {
		// Disable non-empty objectNumber requirement, so that updates don't need to retrieve the current objectNumber.

	}
}
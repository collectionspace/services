package org.collectionspace.services.collectionobject.nuxeo.validators;

import org.collectionspace.services.collectionobject.nuxeo.CollectionObjectValidatorHandler;
import org.collectionspace.services.common.document.InvalidDocumentException;

public class UCJepsCollectionObjectValidatorHandler extends CollectionObjectValidatorHandler {

	@Override
	protected void handleUpdate() throws InvalidDocumentException {
		// Allow an empty object number.

	}

	@Override
	protected void handleCreate() throws InvalidDocumentException {
		// Allow an empty object number.

	}
}

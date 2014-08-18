package org.collectionspace.services.collectionobject.nuxeo;

import org.collectionspace.services.common.document.InvalidDocumentException;

public class BAMPFACollectionObjectValidatorHandler extends CollectionObjectValidatorHandler {

	@Override
	protected void handleUpdate() throws InvalidDocumentException {
		// Allow an empty object number.

	}
	
	@Override
	protected void handleCreate() throws InvalidDocumentException {
		// Allow an empty object number.

	}
}
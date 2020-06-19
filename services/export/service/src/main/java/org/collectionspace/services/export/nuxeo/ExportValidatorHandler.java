package org.collectionspace.services.export.nuxeo;

import org.collectionspace.services.common.context.ServiceContext;
import org.collectionspace.services.common.document.InvalidDocumentException;
import org.collectionspace.services.common.document.ValidatorHandler;
import org.collectionspace.services.common.document.DocumentHandler.Action;

public class ExportValidatorHandler implements ValidatorHandler {

	@Override
	public void validate(Action action, ServiceContext ctx)
			throws InvalidDocumentException {
		// TODO Auto-generated method stub
		System.out.println("ExportValidatorHandler executed.");

	}

}

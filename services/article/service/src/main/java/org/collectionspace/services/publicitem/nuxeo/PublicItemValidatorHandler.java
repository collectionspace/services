package org.collectionspace.services.publicitem.nuxeo;

import org.collectionspace.services.common.context.ServiceContext;
import org.collectionspace.services.common.document.InvalidDocumentException;
import org.collectionspace.services.common.document.ValidatorHandler;
import org.collectionspace.services.common.document.DocumentHandler.Action;
import org.collectionspace.services.client.PoxPayloadIn;
import org.collectionspace.services.client.PoxPayloadOut;

public class PublicItemValidatorHandler implements ValidatorHandler<PoxPayloadIn, PoxPayloadOut> {

	@Override
	public void validate(Action action, ServiceContext<PoxPayloadIn, PoxPayloadOut> ctx)
			throws InvalidDocumentException {
		// TODO Auto-generated method stub
		System.out.println("PublicItemValidatorHandler executed.");

	}

}

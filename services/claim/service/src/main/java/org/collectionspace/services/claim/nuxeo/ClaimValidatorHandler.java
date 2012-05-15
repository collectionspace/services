package org.collectionspace.services.claim.nuxeo;

import org.collectionspace.services.common.context.ServiceContext;
import org.collectionspace.services.common.document.InvalidDocumentException;
import org.collectionspace.services.common.document.ValidatorHandler;
import org.collectionspace.services.common.document.DocumentHandler.Action;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ClaimValidatorHandler implements ValidatorHandler {

        final Logger logger = LoggerFactory.getLogger(ClaimValidatorHandler.class);

	@Override
	public void validate(Action action, ServiceContext ctx)
			throws InvalidDocumentException {

                if(logger.isDebugEnabled()) {
                    logger.debug("validate() action=" + action.name());
                }

	}

}

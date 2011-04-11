package org.collectionspace.services.movement.nuxeo;

import org.collectionspace.services.common.context.ServiceContext;
import org.collectionspace.services.common.document.InvalidDocumentException;
import org.collectionspace.services.common.document.ValidatorHandler;
import org.collectionspace.services.common.document.DocumentHandler.Action;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MovementValidatorHandler implements ValidatorHandler {

        final Logger logger = LoggerFactory.getLogger(MovementValidatorHandler.class);

	@Override
	public void validate(Action action, ServiceContext ctx)
			throws InvalidDocumentException {

                if(logger.isDebugEnabled()) {
                    logger.debug("validate() action=" + action.name());
                }

	}

}

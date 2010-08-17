package org.collectionspace.services.movement.nuxeo;

import java.util.List;

import org.collectionspace.services.common.context.ServiceContext;
import org.collectionspace.services.common.context.MultipartServiceContext;
import org.collectionspace.services.common.datetime.DateTimeFormatUtils;
import org.collectionspace.services.common.document.InvalidDocumentException;
import org.collectionspace.services.common.document.ValidatorHandler;
import org.collectionspace.services.common.document.DocumentHandler.Action;
import org.collectionspace.services.movement.MovementsCommon;

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
                try {
                    MultipartServiceContext mctx = (MultipartServiceContext) ctx;
                    MovementsCommon mc = (MovementsCommon) mctx.getInputPart(mctx.getCommonPartLabel(),
                            MovementsCommon.class);
                    StringBuilder msgBldr = new StringBuilder("validate()");
                    boolean invalid = false;

                    List<String> patterns = DateTimeFormatUtils.getDateFormatPatternsForTenant(ctx);

                    // FIXME: This is an early proof-of-concept.
                    //
                    // We need a better way of determining which fields
                    // in the incoming payload are date fields whose values we
                    // might wish to validate, and of extracting their values,
                    // than hard-coding them here.

                    /*
                    boolean validDateFormat = false;
                    String locDate = mc.getLocationDate();
                    for (String pattern : patterns) {
                        if (DateTimeFormatUtils.isParseableByDatePattern(locDate, pattern)) {
                            validDateFormat = true;
                        }
                    }
                    if (! validDateFormat) {
                        invalid = true;
                        msgBldr.append("\nlocationDate : unrecognized date format '" + locDate + "'");
                    }
                    *
                    */

                    if(action.equals(Action.CREATE)) {
                        //create specific validation here
                    } else if(action.equals(Action.UPDATE)) {
                        //update specific validation here
                    }

                    if (invalid) {
                        String msg = msgBldr.toString();
                        logger.error(msg);
                        throw new InvalidDocumentException(msg);
                    }
                } catch (InvalidDocumentException ide) {
                    throw ide;
                } catch (Exception e) {
                    throw new InvalidDocumentException(e);
                }

	}

}

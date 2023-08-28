package org.collectionspace.services.hit.nuxeo;

import org.collectionspace.services.client.PoxPayloadIn;
import org.collectionspace.services.client.PoxPayloadOut;
import org.collectionspace.services.common.document.InvalidDocumentException;
import org.collectionspace.services.common.document.ValidatorHandlerImpl;
import org.collectionspace.services.hit.HitsCommon;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HitValidatorHandler extends ValidatorHandlerImpl<PoxPayloadIn, PoxPayloadOut> {

    /**
     * Error Messages
     **/
    private static final String VALIDATION_ERROR = "The hit record payload was invalid. See log file for more details.";

    /**
     * The logger.
     */
    private final Logger logger = LoggerFactory.getLogger(HitValidatorHandler.class);

    @Override
    protected Class<?> getCommonPartClass() {
        return HitsCommon.class;
    }

    @Override
    protected void handleCreate() throws InvalidDocumentException {
        try {
            HitsCommon hitsCommon = (HitsCommon) getCommonPart();
            assert (hitsCommon != null);
        } catch (AssertionError e) {
            logger.error(e.getMessage(), e);
            throw new InvalidDocumentException(VALIDATION_ERROR, e);
        }
    }

    @Override
    protected void handleGet() {
        // TODO Auto-generated method stub
    }

    @Override
    protected void handleGetAll() {
        // TODO Auto-generated method stub
    }

    @Override
    protected void handleUpdate() {
        // TODO Auto-generated method stub
    }

    @Override
    protected void handleDelete() {
        // TODO Auto-generated method stub
    }

}

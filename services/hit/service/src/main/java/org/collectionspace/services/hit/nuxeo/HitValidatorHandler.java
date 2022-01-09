package org.collectionspace.services.hit.nuxeo;

import org.collectionspace.services.client.PoxPayloadIn;
import org.collectionspace.services.client.PoxPayloadOut;
import org.collectionspace.services.common.document.InvalidDocumentException;
import org.collectionspace.services.common.document.ValidatorHandlerImpl;
import org.collectionspace.services.hit.HitsCommon;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HitValidatorHandler extends ValidatorHandlerImpl<PoxPayloadIn, PoxPayloadOut> {

    /** The logger. */
    private final Logger logger = LoggerFactory.getLogger(HitValidatorHandler.class);
    
    /** Error Messages **/
    private static final String VALIDATION_ERROR = "The hit record payload was invalid. See log file for more details.";
    
	
    @Override
    protected Class<?> getCommonPartClass() {
    	return HitsCommon.class;
    }
	
	@Override
	protected void handleCreate() throws InvalidDocumentException {
		try {
			HitsCommon hitsCommon = (HitsCommon)getCommonPart();
			assert(hitsCommon != null);
    	} catch (AssertionError e) {
    		if (logger.isErrorEnabled() == true) {
    			logger.error(e.getMessage(), e);
    		}
    		throw new InvalidDocumentException(VALIDATION_ERROR, e);
    	}
	}

	@Override
	protected void handleGet() throws InvalidDocumentException {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void handleGetAll() throws InvalidDocumentException {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void handleUpdate() throws InvalidDocumentException {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void handleDelete() throws InvalidDocumentException {
		// TODO Auto-generated method stub
		
	}

}

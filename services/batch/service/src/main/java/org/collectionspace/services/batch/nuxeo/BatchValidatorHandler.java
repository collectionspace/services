package org.collectionspace.services.batch.nuxeo;

import org.collectionspace.services.batch.BatchCommon;
import org.collectionspace.services.batch.BatchInvocable;
import org.collectionspace.services.client.PoxPayloadIn;
import org.collectionspace.services.client.PoxPayloadOut;
import org.collectionspace.services.common.document.InvalidDocumentException;
import org.collectionspace.services.common.document.ValidatorHandlerImpl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BatchValidatorHandler extends ValidatorHandlerImpl<PoxPayloadIn, PoxPayloadOut> {

    final Logger logger = LoggerFactory.getLogger(BatchValidatorHandler.class);

    //
    // Error Strings
    //
    private static final String VALIDATION_ERROR = "The batch record payload was invalid. See log file for more details.";
    private static final String NAME_NULL_ERROR = "The batch record field \"name\" cannot be empty or missing.";
    private static final String CLASSNAME_NULL_ERROR = "The batch record field \"className\" cannot be empty or missing.";
	private static final String MISSING_CLASS_ERROR = "The Java class '%s' (fully qualified with package name) for the batch job named '%s' cannot be found.";

	@Override
	protected Class<?> getCommonPartClass() {
		return BatchCommon.class;
	}

	@Override
	protected void handleCreate() throws InvalidDocumentException {
    	try {
    		BatchCommon batchCommon = (BatchCommon) getCommonPart();
    		validateBatchCommon(batchCommon);                        
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
    	try {
    		BatchCommon batchCommon = (BatchCommon) getCommonPart();
    		validateBatchCommon(batchCommon);                        
    	} catch (AssertionError e) {
    		if (logger.isErrorEnabled() == true) {
    			logger.error(e.getMessage(), e);
    		}
    		throw new InvalidDocumentException(VALIDATION_ERROR, e);
    	}
    }

	@Override
	protected void handleDelete() throws InvalidDocumentException {
		// TODO Auto-generated method stub
		
	}

    //
    // Private Methods
    //
	private boolean canFindClass(String className) {
		boolean result = false;
		
		try {
			className = className.trim();
			ClassLoader tccl = Thread.currentThread().getContextClassLoader();
			Class<?> c = tccl.loadClass(className);
			tccl.setClassAssertionStatus(className, true);
			if (!BatchInvocable.class.isAssignableFrom(c)) {
				throw new RuntimeException("BatchResource: Class: " + className + " does not implement BatchInvocable!");
			}
			result = true;
		} catch (Exception e) {
			String msg = String.format("Could not find load batch class named '%s'",
					className);
			logger.debug(msg, e);
		}

		return result;
	}
	
	private void validateBatchCommon(BatchCommon batchCommon) {
    	CS_ASSERT(batchCommon != null);
    	
    	//
    	// Ensure a batch name
        String batchName = batchCommon.getName();
        CS_ASSERT(batchName != null, NAME_NULL_ERROR);
        CS_ASSERT(batchName.isEmpty() == false, NAME_NULL_ERROR);
        
    	//
    	// Ensure a batch class
        String batchClassName = batchCommon.getClassName();
        CS_ASSERT(batchClassName != null, CLASSNAME_NULL_ERROR);
        CS_ASSERT(batchClassName.isEmpty() == false, CLASSNAME_NULL_ERROR);
        
        //
        // Ensure we can find and load the batch Java class
        if (canFindClass(batchClassName) == false) {
        	String msg = String.format(MISSING_CLASS_ERROR, batchClassName, batchCommon.getName());
        	CS_ASSERT(false, msg);
        }
    }
	
}

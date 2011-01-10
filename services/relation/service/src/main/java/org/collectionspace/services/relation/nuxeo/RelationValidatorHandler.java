package org.collectionspace.services.relation.nuxeo;

//import junit.framework.Assert;

import org.collectionspace.services.common.document.InvalidDocumentException;
import org.collectionspace.services.common.document.ValidatorHandlerImpl;
import org.collectionspace.services.relation.RelationsCommon;

import org.jboss.resteasy.plugins.providers.multipart.MultipartInput;
import org.jboss.resteasy.plugins.providers.multipart.MultipartOutput;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
//import org.testng.Assert;

public class RelationValidatorHandler extends ValidatorHandlerImpl<MultipartInput, MultipartOutput>	 {

    /** The logger. */
    private final Logger logger = LoggerFactory.getLogger(RelationValidatorHandler.class);
    
    /* Error messages 
     */
    private static final String VALIDATION_ERROR = "The relation record payload was invalid. See log file for more details.";
    private static final String SUBJECT_EQUALS_PREDICATE_ERROR = "The subject ID and object ID cannot be the same.";
    
    @Override
    protected Class<?> getCommonPartClass() {
    	return RelationsCommon.class;
    }
    
    @Override
    protected void handleCreate()
    		throws InvalidDocumentException{
    	try {
	    	RelationsCommon relationsCommon = (RelationsCommon)getCommonPart();
	    	assert(relationsCommon != null);
    		if (logger.isTraceEnabled() == true) {
    			logger.trace(relationsCommon.toString());
    		}
	    	
	    	assert(relationsCommon.getDocumentId1() != null);
	    	assert(relationsCommon.getDocumentId1().length() != 0);
	    	
	    	assert(relationsCommon.getDocumentId2() != null);
	    	assert(relationsCommon.getDocumentId2().length() != 0);
	    	
	    	assert(relationsCommon.getRelationshipType() != null);
	    	//
	    	// Assert that the Subject ID and Predicate ID are not the same
	    	//
	    	assert(relationsCommon.getDocumentId1().equalsIgnoreCase(relationsCommon.getDocumentId2()) == false) :
	    		SUBJECT_EQUALS_PREDICATE_ERROR;
    	} catch (AssertionError e) {
    		if (logger.isErrorEnabled() == true) {
    			logger.error(e.getMessage(), e);
    		}
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

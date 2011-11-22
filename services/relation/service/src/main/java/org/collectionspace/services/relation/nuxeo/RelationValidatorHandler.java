package org.collectionspace.services.relation.nuxeo;

//import junit.framework.Assert;
import org.collectionspace.services.client.PoxPayloadIn;
import org.collectionspace.services.client.PoxPayloadOut;
import org.collectionspace.services.common.document.InvalidDocumentException;
import org.collectionspace.services.common.document.ValidatorHandlerImpl;
import org.collectionspace.services.common.api.RefName.Authority;
import org.collectionspace.services.common.api.RefName.AuthorityItem;
import org.collectionspace.services.common.api.Tools;
import org.collectionspace.services.relation.RelationsCommon;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
//import org.testng.Assert;

public class RelationValidatorHandler extends ValidatorHandlerImpl<PoxPayloadIn, PoxPayloadOut>	 {

    /** The logger. */
    private final Logger logger = LoggerFactory.getLogger(RelationValidatorHandler.class);
    /* Error messages 
     */
    private static final String VALIDATION_ERROR = "The relation record payload was invalid. See log file for more details.";
    private static final String SUBJECT_EQUALS_OBJECT_ERROR = "The subject ID and object ID cannot be the same.";
    
    @Override
    protected Class<?> getCommonPartClass() {
    	return RelationsCommon.class;
    }
    
    @Override
    protected void handleCreate()
    		throws InvalidDocumentException{
    	try {
	    	RelationsCommon relationsCommon = (RelationsCommon)getCommonPart();
	    	CS_ASSERT(relationsCommon != null);
    		if (logger.isTraceEnabled() == true) {
    			logger.trace(relationsCommon.toString());
    		}
	    	
            String subjectCsid = relationsCommon.getSubjectCsid();
            String objectCsid = relationsCommon.getObjectCsid();
	    	
            // If no CSID for a subject or object is included in the create payload,
            // a refName must be provided for that subject or object as an alternate identifier.
            CS_ASSERT(hasCsid(subjectCsid) || hasSubjectRefname(relationsCommon));
            CS_ASSERT(hasCsid(objectCsid) || hasObjectRefname(relationsCommon));
	    	
            // The Subject identifier and Object ID must not be identical:
            // that is, a resource cannot be related to itself.
            if (hasCsid(subjectCsid) && hasCsid(objectCsid)) {
            	CS_ASSERT (subjectCsid.trim().equalsIgnoreCase(objectCsid.trim()) == false,
                        SUBJECT_EQUALS_OBJECT_ERROR);
            }

            // A relationship type must be provided.
            CS_ASSERT(relationsCommon.getRelationshipType() != null);

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

    private boolean hasCsid(String csid) {
        boolean hasCsid = false;
        if (Tools.notBlank(csid)) {
            hasCsid = true;
        }
        return hasCsid;
    }

    private boolean hasSubjectRefname(RelationsCommon relationsCommon) {
        String subjectRefName = relationsCommon.getSubjectRefName();
        return hasRefName(subjectRefName);
    }

    private boolean hasObjectRefname(RelationsCommon relationsCommon) {
        String objectRefName = relationsCommon.getObjectRefName();
        return hasRefName(objectRefName);
    }

    private boolean hasRefName(String refName) {
        boolean hasRefname = false;
        if (Tools.isBlank(refName)) {
            return hasRefname;
        } else {
            Authority authority = Authority.parse(refName);
            AuthorityItem authItem = AuthorityItem.parse(refName);
            if (authority != null || authItem != null) {
                hasRefname = true;
            }
            return hasRefname;
        }

    }
}

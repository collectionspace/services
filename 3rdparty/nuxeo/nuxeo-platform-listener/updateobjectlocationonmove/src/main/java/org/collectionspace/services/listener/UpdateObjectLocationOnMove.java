package org.collectionspace.services.listener;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.collectionspace.services.common.api.RefNameUtils;
import org.collectionspace.services.common.api.Tools;

import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.core.api.DocumentModel;

public class UpdateObjectLocationOnMove extends AbstractUpdateObjectLocationValues {

    private final Log logger = LogFactory.getLog(UpdateObjectLocationOnMove.class);

    @Override
    protected boolean updateCollectionObjectLocation(DocumentModel collectionObjectDocModel,
    		DocumentModel movementDocModel,
    		String movementRecordsLocation) throws ClientException {
    	boolean result = false;

        // Check that the location value returned, which is expected to be a reference (refName) to an authority term (such as a storage
        // location or organization term):
        // 	* Ensure it is not blank.
        // 	* Ensure it is successfully parsed by the authority item parser.
        //
        if (Tools.isBlank(movementRecordsLocation)) {
            return result;
        } else if (RefNameUtils.parseAuthorityTermInfo(movementRecordsLocation) == null) {
            logger.warn(String.format("Ignoring movment record.  Could not parse the location field's refName '%s'.",
            		movementRecordsLocation));
            return result;
        }
        
        // Get the computed current location value of the CollectionObject.
        String existingComputedCurrentLocation = (String) collectionObjectDocModel.getProperty(COLLECTIONOBJECTS_COMMON_SCHEMA,
                COMPUTED_CURRENT_LOCATION_PROPERTY);

        // If the movement record's location is different than the existing catalog's then update it and return 'true'
        if (movementRecordsLocation.equalsIgnoreCase(existingComputedCurrentLocation) == false) {
            collectionObjectDocModel.setProperty(COLLECTIONOBJECTS_COMMON_SCHEMA,
            		COMPUTED_CURRENT_LOCATION_PROPERTY, movementRecordsLocation);
            result = true; // We've updated the location field.
        }
        
        return result;
    }

    @Override
    public Log getLogger() {
    	return logger;
    }
}
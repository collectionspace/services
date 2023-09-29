package org.collectionspace.services.listener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.collectionspace.services.common.api.RefNameUtils;
import org.collectionspace.services.common.api.Tools;

import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.core.api.DocumentModel;

public class UpdateObjectLocationOnMove extends AbstractUpdateObjectLocationValues {

    private static final Logger logger = LoggerFactory.getLogger(UpdateObjectLocationOnMove.class);

    @Override
    protected boolean updateCollectionObjectLocation(DocumentModel collectionObjectDocModel,
                                                     DocumentModel movementDocModel, //FIXME: Not needed?
                                                     DocumentModel mostRecentMovementDocumentModel) throws ClientException {
        boolean result = false;

        //
        // The the most recent movement record is null, that means the CollectionObject has
        // been unrelated to all movement records.  Therefore, we need to clear the 'computedCurrentLocation' field.
        //
        if (mostRecentMovementDocumentModel == null) {
            collectionObjectDocModel.setProperty(COLLECTIONOBJECTS_COMMON_SCHEMA,
                                                 COMPUTED_CURRENT_LOCATION_PROPERTY, null);
            return true;
        }

        // Check that the location value returned, which is expected to be a reference (refName) to an authority term (such as a storage
        // location or organization term):
        // 	* Ensure it is not blank.
        // 	* Ensure it is successfully parsed by the authority item parser.
        //
        String movementRecordsLocation = (String) mostRecentMovementDocumentModel.getProperty(
            MOVEMENTS_COMMON_SCHEMA, CURRENT_LOCATION_ELEMENT_NAME);
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
    public Logger getLogger() {
    	return logger;
    }
}